package com.github.pyltsin.sniffer.inspections

import com.github.pyltsin.sniffer.utils.SnifferInspectionBundle
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*

private const val OBJECT_IS_USED_AS_ARGUMENT = "object.as.argument"


class ObjectUsedAsArgumentInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {


            override fun visitMethodCallExpression(expression: PsiMethodCallExpression?) {
                super.visitMethodCallExpression(expression)
                val qualifier = (expression?.methodExpression?.qualifier as? PsiReferenceExpression) ?: return
                val identifyingElement =
                    (qualifier.advancedResolve(true).element as? PsiParameter)
                        ?: return

                val foundProblem = expression.argumentList.expressions
                    .asSequence()
                    .map { it as? PsiReferenceExpression }
                    .filter { it?.textMatches(qualifier) ?: false }
                    .map { it?.advancedResolve(true)?.element as? PsiParameter }
                    .filter { it?.isEquivalentTo(identifyingElement) ?: false }
                    .filterNotNull()
                    .any()

                if (foundProblem) {
                    holder.registerProblem(
                        expression,
                        SnifferInspectionBundle.message(OBJECT_IS_USED_AS_ARGUMENT, qualifier.canonicalText)
                    )
                }
            }
        }
    }
}