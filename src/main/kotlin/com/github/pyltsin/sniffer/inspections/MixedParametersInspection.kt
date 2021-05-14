package com.github.pyltsin.sniffer.inspections

import com.github.pyltsin.sniffer.utils.OptimalResult
import com.github.pyltsin.sniffer.utils.SnifferInspectionBundle
import com.github.pyltsin.sniffer.utils.optimalPair
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*

const val GET_PREFIX = "get"
const val IS_PREFIX = "get"
const val MIXED_MESSAGE = "mixed.arguments.error"

class MixedParametersInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression?) {
                super.visitMethodCallExpression(expression)
                if (expression == null || !expression.isValid) {
                    return
                }
                val arguments: List<Argument> = getArguments(expression)
                if (arguments.size <= 1) {
                    return
                }
                val methodParameters: List<Argument> = getParameters(expression)
                val parametersByType: Map<PsiType?, List<Argument>> = methodParameters.groupBy { it.type }
                val argumentByIndex: Map<Int, List<Argument>> = arguments.groupBy { it.index }
                for (parametersInType: List<Argument> in parametersByType.values) {
                    val argumentsInType: List<Argument> = parametersInType
                        .mapNotNull { argumentByIndex[it.index] }
                        .filter { it.size == 1 }
                        .map { it[0] }
                    if (argumentsInType.size != parametersInType.size) {
                        continue
                    }

                    val s1 = argumentsInType.mapNotNull { it.name }
                    val s2 = parametersInType.mapNotNull { it.name }
                    val optimalPair = optimalPair(s1, s2)
                    if (optimalPair == null || optimalPair.savedOrder) {
                        continue
                    }
                    registerProblem(expression, optimalPair)
                }
            }

            private fun registerProblem(expression: PsiMethodCallExpression, optimalPair: OptimalResult) {
                holder.registerProblem(
                    expression.argumentList,
                    SnifferInspectionBundle.message(
                        MIXED_MESSAGE, optimalPair.newOrder.joinToString(separator = ",",
                            transform = { pair -> "[argument = " + pair.first + ", parameter = " + pair.second + "]" })
                    )
                )
            }

            private fun getParameters(expression: PsiMethodCallExpression?): List<Argument> {
                if (expression == null) {
                    return listOf()
                }
                val parameters = arrayListOf<Argument>()

                val psiMethod = expression.methodExpression.advancedResolve(true).element as? PsiMethod
                psiMethod?.parameterList?.parameters?.forEachIndexed { index, psiParameter ->
                    run {
                        parameters.add(Argument(index, psiParameter.name, psiParameter.type))
                    }
                }
                parameters.removeIf { it.type == null || it.name == null }
                return parameters
            }

            private fun getArguments(expression: PsiMethodCallExpression?): List<Argument> {
                if (expression == null) {
                    return listOf()
                }
                val arguments = arrayListOf<Argument>()
                for ((index, argumentExpression) in expression.argumentList.expressions.withIndex()) {
                    arguments.add(Argument(index, getArgumentName(argumentExpression), getArgumentType(expression)))
                }
                arguments.removeIf { it.type == null || it.name == null }
                return arguments
            }

            private fun getArgumentName(expression: PsiExpression): String? {
                return when (expression) {
                    is PsiReferenceExpression -> expression.canonicalText
                    is PsiMethodCallExpression -> getNameFromGetter(expression)
                    else -> null
                }
            }

            private fun getNameFromGetter(expression: PsiMethodCallExpression): String {
                var result = expression.methodExpression.canonicalText
                for (prefix in setOf(GET_PREFIX, IS_PREFIX)) {
                    val length = prefix.length
                    if (result.startsWith(prefix) && result.length > length && result[length].isUpperCase()) {
                        result = result.substring(length).decapitalize()
                    }
                }
                return result
            }

            private fun getArgumentType(expression: PsiExpression): PsiType? {
                return expression.type
            }
        }
    }

    data class Argument(
        val index: Int,
        val name: String?,
        val type: PsiType?
    )
}

