package com.github.pyltsin.sniffer

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import com.intellij.psi.CommonClassNames.*
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.ui.CheckBox
import com.siyeh.HardcodedMethodConstants
import com.siyeh.ig.callMatcher.CallMatcher
import com.siyeh.ig.psiutils.MethodUtils
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

private const val HASH_CODE_NOT_OVERRIDE_MESSAGE = "hashcode.override.allowed.error"


class HashCodeOverrideInspection : AbstractBaseJavaLocalInspectionTool() {

    private val hashMapClasses: Set<String> = setOf(JAVA_UTIL_HASH_MAP, JAVA_UTIL_CONCURRENT_HASH_MAP)

    var m_allowedSuperHashMap: Boolean = true
    private val streamCollectMatcher: CallMatcher = CallMatcher.instanceCall(JAVA_UTIL_STREAM_STREAM, "collect")
    private val collectorsHashMapMatchers: CallMatcher = CallMatcher.anyOf(
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "groupingBy"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "groupingByConcurrent"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toMap"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toConcurrentMap"),
    )

    private val mapOf: CallMatcher = CallMatcher.staticCall(JAVA_UTIL_MAP, "of")

    override fun createOptionsPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val checkBox = CheckBox(
            SnifferInspectionBundle.message("hashcode.override.allowed.super"),
            this,
            "m_allowedSuperHashMap"
        )
        panel.add(checkBox, BorderLayout.CENTER)

        return panel
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitNewExpression(expression: PsiNewExpression?) {
                super.visitNewExpression(expression)
                if (expression == null) {
                    return
                }
                if (expression.classOrAnonymousClassReference?.qualifiedName !in hashMapClasses) {
                    return
                }
                val typeArguments = expression.classOrAnonymousClassReference?.parameterList?.typeArguments ?: return
                if (typeArguments.size != 2) {
                    return
                }
                val keyType = typeArguments[0]
                if (keyType == null || hasOverrideHashCode(keyType)) {
                    return
                }

                holder.registerProblem(
                    expression,
                    SnifferInspectionBundle.message(HASH_CODE_NOT_OVERRIDE_MESSAGE, keyType.canonicalText)
                )
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression?) {
                super.visitMethodCallExpression(expression)

                if (expression == null) {
                    return
                }

                //Map.of
                if (mapOf.matches(expression)) {
                    val hashMapKey = getKeyTypeMethodParameter(expression, setOf(JAVA_UTIL_MAP))
                    if (hashMapKey != null && !hasOverrideHashCode(hashMapKey)) {
                        holder.registerProblem(
                            expression,
                            SnifferInspectionBundle.message(HASH_CODE_NOT_OVERRIDE_MESSAGE, hashMapKey.canonicalText)
                        )
                        return
                    }
                }

                //Stream.collect
                if (!streamCollectMatcher.matches(expression)) {
                    return
                }

                val hashMapKey = getKeyTypeMethodParameter(expression, hashMapClasses)
                //if collect return HashMap
                if (hashMapKey != null && !hasOverrideHashCode(hashMapKey)
                ) {
                    holder.registerProblem(
                        expression,
                        SnifferInspectionBundle.message(HASH_CODE_NOT_OVERRIDE_MESSAGE, hashMapKey.canonicalText)
                    )
                    return
                }

                //check known Collectors
                val expressions = expression.argumentList.expressions
                if (expressions.size != 1) {
                    return
                }

                val collectorExpression = expressions[0]
                if (!collectorsHashMapMatchers.matches(collectorExpression)) {
                    return
                }

                val collectionKey = getKeyTypeMethodParameter(expression, setOf(JAVA_UTIL_MAP))
                if (collectionKey != null && !hasOverrideHashCode(collectionKey)) {
                    holder.registerProblem(
                        expression,
                        SnifferInspectionBundle.message(HASH_CODE_NOT_OVERRIDE_MESSAGE, collectionKey.canonicalText)
                    )
                    return
                } else {
                    return
                }
            }
        }
    }

    private fun getKeyTypeMethodParameter(expression: PsiExpression?, expectedClass: Set<String>): PsiType? {
        if (expression == null) {
            return null
        }
        if (expression !is PsiMethodCallExpression) {
            return null
        }

        val typeMethod = expression.methodExpression.type
        return if (typeMethod is PsiClassType
            && typeMethod.resolve()?.qualifiedName in expectedClass
            && typeMethod.parameterCount == 2
        ) {
            typeMethod.parameters[0]
        } else {
            null
        }
    }

    private fun hasOverrideHashCode(psiType: PsiType): Boolean {
        val psiClass = PsiTypesUtil.getPsiClass(psiType)
        val methods: Array<PsiMethod> =
            psiClass?.findMethodsByName(HardcodedMethodConstants.HASH_CODE, m_allowedSuperHashMap) ?: arrayOf()
        return methods
            .asSequence()
            .filter { MethodUtils.isHashCode(it) }
            .filter { it.containingClass?.qualifiedName != JAVA_LANG_OBJECT }
            .any()
    }
}