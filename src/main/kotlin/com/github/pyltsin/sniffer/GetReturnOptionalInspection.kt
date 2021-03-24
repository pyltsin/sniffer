package com.github.pyltsin.sniffer

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassReferenceType

const val OPTIONAL = "java.util.Optional"
const val GET_OPTIONAL_MESSAGE = "get.optional.error"

class GetReturnOptionalInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun isEnabledByDefault(): Boolean {
        return false
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitMethod(method: PsiMethod?) {
                super.visitMethod(method)
                if (method == null) {
                    return
                }
                if (!(method.name.startsWith("get") && method.name.length > 3)) {
                    return
                }

                if ((method.returnType as? PsiClassReferenceType)?.resolve()?.qualifiedName == OPTIONAL) {
                    holder.registerProblem(
                        method.nameIdentifier ?: method,
                        SnifferInspectionBundle.message(GET_OPTIONAL_MESSAGE, method.name)
                    )
                }
            }
        }
    }
}