package com.github.pyltsin.sniffer.inspections

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.util.lang.JavaVersion
import com.siyeh.ig.LightJavaInspectionTestCase
import java.io.File

class ObjectUsedAsArgumentInspectionTest : LightJavaInspectionTestCase() {

    fun test() {
        myFixture.configureByFile("ObjectAsArgument.java")
        myFixture.testHighlighting(false, false, true)
    }

    override fun getInspection(): InspectionProfileEntry {
        return ObjectUsedAsArgumentInspection()
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/objectUsedAsArgument"
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : ProjectDescriptor(LanguageLevel.JDK_11) {
            override fun getSdk(): Sdk {
                return IdeaTestUtil.createMockJdk(
                    JavaVersion.compose(8).toString(),
                    File("src/test/mockJDK-11").toString()
                )
            }
        }
    }
}