package com.github.pyltsin.sniffer.utils

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.SnifferInspectionsBundle"

class SnifferInspectionBundle : DynamicBundle(BUNDLE) {

    companion object {
        private val INSTANCE: SnifferInspectionBundle = SnifferInspectionBundle()

        fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
            return INSTANCE.getMessage(key, *params)
        }
    }
}