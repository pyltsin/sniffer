package com.github.pyltsin.sniffer.utils

import org.junit.Assert
import org.junit.Test


class UtilsTest {

    @Test
    fun optimalPair() {
        val optimalPair = optimalPair(
            listOf("xa", "xb"),
            listOf("b", "a")
        )
        Assert.assertEquals(optimalPair?.savedOrder, false)
        Assert.assertEquals(optimalPair?.previousDistance, 6)
        Assert.assertEquals(optimalPair?.nextDistance, 2)
        Assert.assertEquals(setOf(Pair("xa", "a"), Pair("xb", "b")), optimalPair?.newOrder?.toSet())
    }

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