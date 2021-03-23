package com.github.pyltsin.sniffer

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.util.lang.JavaVersion
import com.siyeh.ig.LightJavaInspectionTestCase
import java.io.File

class GetReturnOptionalInspectionTest : LightJavaInspectionTestCase() {

    fun test() {
        myFixture.configureByFile("GetReturnOptional.java")
        myFixture.testHighlighting(false, false, true)
    }

    override fun getInspection(): InspectionProfileEntry {
        return GetReturnOptionalInspection()
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/getReturnOptional"
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