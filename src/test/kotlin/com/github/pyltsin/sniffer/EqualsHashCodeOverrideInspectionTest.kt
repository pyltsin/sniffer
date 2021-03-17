package com.github.pyltsin.sniffer

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.util.lang.JavaVersion
import com.siyeh.ig.LightJavaInspectionTestCase
import java.io.File

class EqualsHashCodeOverrideInspectionTest : LightJavaInspectionTestCase() {

    fun testEqualsHashCode() {
        myFixture.configureByFile("HashCode.java")
        myFixture.enableInspections(EqualsHashCodeOverrideInspection().also { it.m_allowedSuperHashMap = true })
        myFixture.testHighlighting(false, false, true)
    }

    fun testEqualsHashCodeOverride() {
        myFixture.configureByFile("HashCodeOverride.java")
        myFixture.enableInspections(EqualsHashCodeOverrideInspection().also { it.m_allowedSuperHashMap = true })
        myFixture.testHighlighting(false, false, true)
    }


    override fun getInspection(): InspectionProfileEntry? {
        return null
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/hashCodeOverride"
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