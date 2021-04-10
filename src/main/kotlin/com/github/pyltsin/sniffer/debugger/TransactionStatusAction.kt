package com.github.pyltsin.sniffer.debugger

import com.github.pyltsin.sniffer.ui.TransactionDebugUpdateListener.TransactionDebugUpdate
import com.intellij.debugger.actions.DebuggerAction
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.pom.Navigatable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.sun.jdi.BooleanValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.StringReference
import com.sun.jdi.Value
import icons.SnifferIcons
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

const val APPLICATION_CLASS: String = "org.springframework.transaction.support.TransactionSynchronizationManager"
const val TRANSACTION_ACTIVE: String =
    "org.springframework.transaction.support.TransactionSynchronizationManager.actualTransactionActive.get()==Boolean.TRUE"

val PROPERTIES: List<Pair<String, String>> = listOf(
    Pair(
        "active",
        "org.springframework.transaction.support.TransactionSynchronizationManager.actualTransactionActive.get()==Boolean.TRUE"
    ),
    Pair(
        "name",
        "org.springframework.transaction.support.TransactionSynchronizationManager.currentTransactionName.get()"
    ),
    Pair(
        "readOnly",
        "org.springframework.transaction.support.TransactionSynchronizationManager.currentTransactionReadOnly.get()==Boolean.TRUE"
    ),
    Pair(
        "IsolationLevel",
        "org.springframework.transaction.support.TransactionSynchronizationManager.currentTransactionIsolationLevel.get()==null?" +
                "null:org.springframework.transaction.support.TransactionSynchronizationManager.currentTransactionIsolationLevel.get().toString()"
    ),
)

class TransactionStatusAction : DebuggerAction() {

    private var previousSourcePosition: AtomicReference<XSourcePosition> = AtomicReference()
    private var previousSession: AtomicReference<XDebugSession> = AtomicReference()
    private var foundedSpring: AtomicBoolean = AtomicBoolean(false)
    private var previousState: AtomicReference<((Presentation) -> Unit)?> = AtomicReference(null)
    override fun actionPerformed(e: AnActionEvent) {
        if (!checkAllowed(e)) {
            return
        }
        val project = e.project ?: return

        val toolWindow: ToolWindow =
            ToolWindowManager.getInstance(project).getToolWindow("TransactionView") ?: return
        toolWindow.show()
        prepareAndSendResult(e)
    }

    private fun prepareAndSendResult(e: AnActionEvent) {
        val project = e.project ?: return
        val currentSession = getCurrentSession(e) ?: return
        val currentSourcePosition = currentSession.currentStackFrame?.sourcePosition ?: return
        val guid = UUID.randomUUID().toString()
        val createNavigable: Navigatable = currentSourcePosition.createNavigatable(project)
        project.messageBus.syncPublisher(TransactionDebugUpdate)
            .createRow(guid, createNavigable, currentSourcePosition.file.name + ":" + currentSourcePosition.line)

        PROPERTIES.forEachIndexed { index, property ->
            currentSession.debugProcess.evaluator?.evaluate(
                property.second,
                object : XDebuggerEvaluator.XEvaluationCallback {
                    override fun errorOccurred(errorMessage: String) {
                    }

                    override fun evaluated(result: XValue) {
                        if (result !is JavaValue) {
                            return
                        }
                        val descriptorValue: Value? = result.descriptor.value
                        if (descriptorValue == null) {
                            project.messageBus.syncPublisher(TransactionDebugUpdate)
                                .addData(guid, index, property.first, "null")
                        }
                        if (descriptorValue is BooleanValue) {
                            val booleanValue = descriptorValue.value()
                            project.messageBus.syncPublisher(TransactionDebugUpdate)
                                .addData(guid, index, property.first, booleanValue.toString())
                        }
                        if (descriptorValue is StringReference) {
                            val stringValue = descriptorValue.value()
                            project.messageBus.syncPublisher(TransactionDebugUpdate)
                                .addData(guid, index, property.first, stringValue)
                        }
                    }
                }, currentSourcePosition
            )
        }
    }

    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val currentSession: XDebugSession? = getCurrentSession(e)
        if (currentSession == null) {
            setDisabled(presentation)
            return
        }
        val currentSourcePosition: XSourcePosition? = currentSession.currentStackFrame?.sourcePosition

        if (previousSession.get() == currentSession && (previousSourcePosition.get() == currentSourcePosition)) {
            //last result
            val state = previousState.get()
            if (state != null) {
                state.invoke(presentation)
                return
            }
        } else {
            previousSession.set(currentSession)
            previousSourcePosition.set(currentSourcePosition)
            previousState.set(null)
        }


        if (!checkAllowed(e)) {
            setDisabled(presentation)
            previousState.set(::setDisabled)
            return
        }

        currentSession.debugProcess.evaluator?.evaluate(
            TRANSACTION_ACTIVE,
            object : XDebuggerEvaluator.XEvaluationCallback {
                override fun errorOccurred(errorMessage: String) {
                    previousState.set { presentation -> setError(presentation, errorMessage) }
                }

                override fun evaluated(result: XValue) {
                    if (result !is JavaValue) {
                        return
                    }
                    val descriptorValue: Value = result.descriptor.value
                    if (descriptorValue !is BooleanValue) {
                        return
                    }
                    val transactionIsActive = descriptorValue.value()
                    if (transactionIsActive) {
                        previousState.set(::setActive)
                    } else {
                        previousState.set(::setStop)
                    }
                }
            }, previousSourcePosition.get()
        )
    }

    private fun checkAllowed(e: AnActionEvent): Boolean {
        val currentSession: XDebugSession = getCurrentSession(e) ?: return false

        if (!currentSession.isSuspended) {
            return false
        }

        val project = e.project ?: return false

        if (previousSession == currentSession && !foundedSpring.get()) {
            return false
        }

        currentSession.currentStackFrame?.sourcePosition ?: return false

        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val projectScope = GlobalSearchScope.allScope(project)

        foundedSpring.set(javaPsiFacade.findClass(APPLICATION_CLASS, projectScope) != null)
        if (!foundedSpring.get()) {
            return false
        }
        return true
    }

    private fun setDisabled(presentation: Presentation) {
        presentation.isVisible = true
        presentation.isEnabled = false
    }

    private fun setError(presentation: Presentation, errorMessage: String) {
        presentation.isVisible = true
        presentation.isEnabled = true
        presentation.icon = SnifferIcons.STOP
        presentation.description = errorMessage
    }

    private fun setActive(presentation: Presentation) {
        presentation.isVisible = true
        presentation.isEnabled = true
        presentation.icon = SnifferIcons.RUNNING
        presentation.description = "Active transaction"
    }

    private fun setStop(presentation: Presentation) {
        presentation.isVisible = true
        presentation.isEnabled = true
        presentation.icon = SnifferIcons.STOP
        presentation.description = "Active transaction not found"
    }

    private fun getCurrentSession(e: AnActionEvent): XDebugSession? {
        val project = e.project
        return if (project == null) null else XDebuggerManager.getInstance(project).currentSession
    }
}