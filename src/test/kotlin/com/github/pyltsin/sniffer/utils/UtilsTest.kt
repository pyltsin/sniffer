package com.github.pyltsin.sniffer.utils

import org.junit.Assert
import org.junit.Test


class UtilsTest {

    @Test
    fun levenshteinDistance() {
        Assert.assertEquals(
            2, levenshteinDistance("1", "111", insertPrice = 1, removePrice = 2)
        )
        Assert.assertEquals(
            4, levenshteinDistance("111", "1", insertPrice = 1, removePrice = 2)
        )
        Assert.assertEquals(
            5, levenshteinDistance("111", "12", insertPrice = 1, removePrice = 3, replacePrice = 2)
        )
    }
}