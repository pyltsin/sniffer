package com.github.pyltsin.sniffer;// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.lang.JavaVersion;
import com.siyeh.ig.LightJavaInspectionTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ComparingReferencesInspectionTest extends LightJavaInspectionTestCase {

  public void testEq() {
    myFixture.configureByFile("Eq.java");
    myFixture.enableInspections(new ComparingReferencesInspection());
    myFixture.testHighlighting(true, false, false);
  }

  @Nullable
  @Override
  protected InspectionProfileEntry getInspection() {
    return new ComparingReferencesInspection();
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/testData/comparingReference";
  }

  @Override
  protected @NotNull LightProjectDescriptor getProjectDescriptor() {
    return new ProjectDescriptor(LanguageLevel.JDK_1_7) {
      @Override
      public Sdk getSdk() {
        return IdeaTestUtil.createMockJdk(JavaVersion.compose(7).toString(), new File("src/test/mockJDK-1.7").toString());
      }
    };
  }
}
