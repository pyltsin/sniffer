package com.github.pyltsin.sniffer.inspections

import com.github.pyltsin.sniffer.utils.SnifferInspectionBundle
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiConditionalExpression
import com.intellij.psi.PsiElementVisitor
import com.siyeh.ig.psiutils.EquivalenceChecker

const val EQUALS_TERNARY = "ternary.equals.then.else.error"

class TernaryConstantValueInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitConditionalExpression(expression: PsiConditionalExpression?) {
                super.visitConditionalExpression(expression)
                if (expression == null) {
                    return
                }
                val thenExpression = expression.thenExpression
                val elseExpression = expression.elseExpression
                if (thenExpression == null || elseExpression == null) {
                    return
                }
                if (EquivalenceChecker.getCanonicalPsiEquivalence()
                        .expressionsMatch(thenExpression, elseExpression).isExactMatch
                ) {
                    registerProblem(expression)
                }
            }

            private fun registerProblem(expression: PsiConditionalExpression) {
                holder.registerProblem(
                    expression,
                    SnifferInspectionBundle.message(
                        EQUALS_TERNARY
                    )
                )
            }
        }
    }
}