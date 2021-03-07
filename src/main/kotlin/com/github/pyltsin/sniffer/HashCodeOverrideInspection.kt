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
import java.util.concurrent.ConcurrentMap
import javax.swing.JComponent
import javax.swing.JPanel

private const val HASH_CODE_NOT_OVERRIDE_MESSAGE = "hashcode.override.allowed.error"


class HashCodeOverrideInspection : AbstractBaseJavaLocalInspectionTool() {

    private val hashMapClasses: Set<String> =
        setOf(JAVA_UTIL_HASH_MAP, JAVA_UTIL_CONCURRENT_HASH_MAP, "java.util.LinkedHashMap")
    private val hashSetClasses: Set<String> = setOf(JAVA_UTIL_HASH_SET, JAVA_UTIL_LINKED_HASH_SET)

    var m_allowedSuperHashMap: Boolean = true
    private val streamCollectMatcher: CallMatcher = CallMatcher.instanceCall(JAVA_UTIL_STREAM_STREAM, "collect")
    private val collectorsHashMapAndHashSetMatchers: CallMatcher = CallMatcher.anyOf(
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "groupingBy"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "groupingByConcurrent"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toMap"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toConcurrentMap"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toUnmodifiableMap"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toSet"),
        CallMatcher.staticCall(JAVA_UTIL_STREAM_COLLECTORS, "toUnmodifiableSet"),
    )

    private val mapOf: CallMatcher = CallMatcher.staticCall(JAVA_UTIL_MAP, "of")
    private val setOf: CallMatcher = CallMatcher.staticCall(JAVA_UTIL_SET, "of")

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
                val problemClass = findProblemFirstKeyInClasses(expression, hashSetClasses + hashMapClasses)
                if (problemClass != null) {
                    registerHashCodeProblem(expression, problemClass)
                }
            }

            private fun findProblemFirstKeyInClasses(expression: PsiNewExpression, classes: Set<String>): PsiType? {
                if (expression.classOrAnonymousClassReference?.qualifiedName !in classes) {
                    return null
                }
                val typeArguments =
                    expression.classOrAnonymousClassReference?.parameterList?.typeArguments ?: return null
                if (typeArguments.isEmpty()) {
                    return null
                }
                val keyType: PsiType = typeArguments[0]
                if (hasOverrideHashCode(keyType)) {
                    return null
                }
                return keyType
            }


            override fun visitMethodCallExpression(expression: PsiMethodCallExpression?) {
                super.visitMethodCallExpression(expression)

                if (expression == null) {
                    return
                }

                //Map.of
                if (registerMapOfProblem(expression)) return

                //Set.of
                if (registerSetOfProblem(expression)) return

                //Stream.collect
                registerStreamCollectProblem(expression)

                return
            }

            private fun registerMapOfProblem(expression: PsiMethodCallExpression): Boolean {
                if (mapOf.matches(expression)) {
                    val hashMapKey = findFirstTypeMethodParameter(expression, setOf(JAVA_UTIL_MAP))
                    if (hashMapKey != null && !hasOverrideHashCode(hashMapKey)) {
                        registerHashCodeProblem(expression, hashMapKey)
                        return true
                    }
                }
                return false
            }

            private fun registerSetOfProblem(expression: PsiMethodCallExpression): Boolean {
                if (setOf.matches(expression)) {
                    val hashMapKey = findFirstTypeMethodParameter(expression, setOf(JAVA_UTIL_SET))
                    if (hashMapKey != null && !hasOverrideHashCode(hashMapKey)) {
                        registerHashCodeProblem(expression, hashMapKey)
                        return true
                    }
                }
                return false
            }

            private fun registerStreamCollectProblem(expression: PsiMethodCallExpression): Boolean {
                if (!streamCollectMatcher.matches(expression)) {
                    return false
                }

                //if collect return known classes: HashMap, HashSet or like this
                val problemClass = findFirstTypeMethodParameter(expression, hashMapClasses + hashSetClasses)
                if (problemClass != null && !hasOverrideHashCode(problemClass)) {
                    registerHashCodeProblem(expression, problemClass)
                    return true
                }

                //check known Collectors
                val expressions = expression.argumentList.expressions
                if (expressions.size != 1) {
                    return false
                }

                val collectorExpression = expressions[0]
                if (!collectorsHashMapAndHashSetMatchers.matches(collectorExpression)) {
                    return false
                }

                val collectionKey = findFirstTypeMethodParameter(
                    expression, setOf(
                        JAVA_UTIL_MAP,
                        "java.util.concurrent.ConcurrentMap", JAVA_UTIL_SET
                    )
                )

                if (collectionKey != null && !hasOverrideHashCode(collectionKey)) {
                    registerHashCodeProblem(expression, collectionKey)
                    return true
                }
                return false
            }

            private fun registerHashCodeProblem(
                expression: PsiExpression,
                collectionKey: PsiType
            ) {
                holder.registerProblem(
                    expression,
                    SnifferInspectionBundle.message(HASH_CODE_NOT_OVERRIDE_MESSAGE, collectionKey.canonicalText)
                )
            }

        }
    }

    private fun findFirstTypeMethodParameter(expression: PsiExpression?, expectedClass: Set<String>): PsiType? {
        if (expression == null) {
            return null
        }
        if (expression !is PsiMethodCallExpression) {
            return null
        }

        val typeMethod = expression.methodExpression.type
        return if (typeMethod is PsiClassType
            && typeMethod.resolve()?.qualifiedName in expectedClass
            && typeMethod.parameterCount > 0
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