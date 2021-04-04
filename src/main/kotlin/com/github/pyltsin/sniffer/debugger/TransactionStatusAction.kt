package com.github.pyltsin.sniffer.debugger

import com.intellij.debugger.actions.DebuggerAction
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.sun.jdi.StringReference
import icons.SnifferIcons

const val APPLICATION_CLASS: String = "io.micronaut.runtime.Micronaut"

class TransactionStatusAction : DebuggerAction() {
    private var foundedSpring: Boolean? = null
    private var currentSourcePosition: XSourcePosition? = null
    private var text: String = ""
    override fun actionPerformed(e: AnActionEvent) {
        val currentSession = getCurrentSession(e)
        currentSession?.ui.component.
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val currentSession = getCurrentSession(e)
        if (currentSession == null) {
            disable(e)
            return
        }

        if (!currentSession.isSuspended) {
            disable(e)
        }

        val project = e.project
        if (project == null) {
            disable(e)
            return
        }
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val projectScope = GlobalSearchScope.allScope(project)
        foundedSpring = foundedSpring == true || javaPsiFacade.findClass(APPLICATION_CLASS, projectScope) != null
        if (true != foundedSpring) {
            disable(e)
            return
        }
        if (currentSession.currentStackFrame?.sourcePosition == null ||
            currentSourcePosition == currentSession.currentStackFrame?.sourcePosition) {
            if (text == "test") {
                e.presentation.icon = SnifferIcons.STOP
                text = ""
            }
            return
        }
        currentSourcePosition = currentSession.currentStackFrame?.sourcePosition

        currentSession.debugProcess.evaluator?.evaluate(
            "io.micronaut.runtime.Micronaut.get()",
            object : XDebuggerEvaluator.XEvaluationCallback {
                override fun errorOccurred(errorMessage: String) {
                    println(errorMessage)
                }

                override fun evaluated(result: XValue) {
                    if (result !is JavaValue) {
                        return
                    }
                    val descriptorValue = result.descriptor.value
                    if (descriptorValue !is StringReference) {
                        return
                    }
                    text = descriptorValue.value()
                    println(result)
                }

            }, currentSourcePosition
        )

    }

    private fun getCurrentSession(e: AnActionEvent): XDebugSession? {
        val project = e.project
        return if (project == null) null else XDebuggerManager.getInstance(project).currentSession
    }

    private fun disable(e: AnActionEvent) {
        e.presentation.isEnabled = false
        foundedSpring = false
    }
}