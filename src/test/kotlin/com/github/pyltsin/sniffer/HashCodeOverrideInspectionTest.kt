package com.github.pyltsin.sniffer

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.util.lang.JavaVersion
import com.siyeh.ig.LightJavaInspectionTestCase
import java.io.File

class HashCodeOverrideInspectionTest : LightJavaInspectionTestCase() {

    fun testNewHashMap() {
        myFixture.configureByFile("NewHashMap.java")
        myFixture.enableInspections(HashCodeOverrideInspection())
        myFixture.testHighlighting(false, false, true)
    }

    fun testNewHashMapOverrided() {
        myFixture.configureByFile("NewHashMapOverrided.java")
        myFixture.enableInspections(HashCodeOverrideInspection().also { it.m_allowedSuperHashMap = false })
        myFixture.testHighlighting(false, false, true)
    }

    override fun getInspection(): InspectionProfileEntry? {
        return null
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/hashCodeOverride"
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return object : ProjectDescriptor(LanguageLevel.JDK_1_8) {
            override fun getSdk(): Sdk {
                return IdeaTestUtil.createMockJdk(
                    JavaVersion.compose(8).toString(),
                    File("src/test/mockJDK-1.8").toString()
                )
            }
        }
    }
}