// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.pyltsin.sniffer;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiPrefixExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.StringTokenizer;

import static com.siyeh.ig.psiutils.ExpressionUtils.isNullLiteral;

/**
 * Implements an inspection to detect when object references are compared using 'a==b' or 'a!=b'.
 * The quick fix converts these comparisons to 'a.equals(b) or '!a.equals(b)' respectively.
 */
public class ComparingReferencesInspection extends AbstractBaseJavaLocalInspectionTool {

  @SuppressWarnings({"WeakerAccess"})
  @NonNls
  public String CHECKED_CLASSES = "java.lang.String;java.util.Date";

  /**
   * This method is called to get the panel describing the inspection.
   * It is called every time the user selects the inspection in preferences.
   * The user has the option to edit the list of {@link #CHECKED_CLASSES}.
   *
   * @return panel to display inspection information.
   */
  @Override
  public JComponent createOptionsPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    final JTextField checkedClasses = new JTextField(CHECKED_CLASSES);
    checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
      public void textChanged(@NotNull DocumentEvent event) {
        CHECKED_CLASSES = checkedClasses.getText();
      }
    });
    panel.add(checkedClasses);
    Integer i = 0;
    synchronized (i) {

    }
    return panel;
  }

  /**
   * This method is overridden to provide a custom visitor.
   * that inspects expressions with relational operators '==' and '!='.
   * The visitor must not be recursive and must be thread-safe.
   *
   * @param holder     object for visitor to register problems found.
   * @param isOnTheFly true if inspection was run in non-batch mode
   * @return non-null visitor for this inspection.
   * @see JavaElementVisitor
   */
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {

      @Override
      public void visitNewExpression(PsiNewExpression expression) {
        String canonicalText = expression.getClassOrAnonymousClassReference().getParameterList().getTypeArguments()[0].getCanonicalText();

        super.visitNewExpression(expression);
      }

      /**
       * This string defines the short message shown to a user signaling the inspection found a problem.
       * It reuses a string from the inspections bundle.
       */
      @NonNls
      private final String DESCRIPTION_TEMPLATE = "SDK " +
              InspectionsBundle.message("inspection.comparing.references.problem.descriptor");

      /**
       * Avoid defining visitors for both Reference and Binary expressions.
       *
       * @param psiReferenceExpression The expression to be evaluated.
       */
      @Override
      public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
      }

      /**
       * Evaluate binary psi expressions to see if they contain relational operators '==' and '!=', AND they contain
       * classes contained in CHECKED_CLASSES. The evaluation ignores expressions comparing an object to null.
       * IF this criteria is met, add the expression to the problems list.
       *
       * @param expression The binary expression to be evaluated.
       */
      @Override
      public void visitBinaryExpression(PsiBinaryExpression expression) {
        super.visitBinaryExpression(expression);
        IElementType opSign = expression.getOperationTokenType();
        if (opSign == JavaTokenType.EQEQ || opSign == JavaTokenType.NE) {
          // The binary expression is the correct type for this inspection
          PsiExpression lOperand = expression.getLOperand();
          PsiExpression rOperand = expression.getROperand();
          if (rOperand == null || isNullLiteral(lOperand) || isNullLiteral(rOperand)) {
            return;
          }
          // Nothing is compared to null, now check the types being compared
          PsiType lType = lOperand.getType();
          PsiType rType = rOperand.getType();
          if (isCheckedType(lType) || isCheckedType(rType)) {
            // Identified an expression with potential problems, add to list with fix object.
            holder.registerProblem(expression,
                    DESCRIPTION_TEMPLATE);
          }
        }
      }

      /**
       * Verifies the input is the correct {@code PsiType} for this inspection.
       *
       * @param type The {@code PsiType} to be examined for a match
       * @return {@code true} if input is {@code PsiClassType} and matches one of the classes
       * in the {@link ComparingReferencesInspection#CHECKED_CLASSES} list.
       */
      private boolean isCheckedType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
          return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(CHECKED_CLASSES, ";");
        while (tokenizer.hasMoreTokens()) {
          String className = tokenizer.nextToken();
          if (type.equalsToText(className)) {
            return true;
          }
        }
        return false;
      }

    };
  }
}
