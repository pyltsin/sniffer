package com.github.pyltsin.sniffer.utils

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

//todo test
fun main() {
    println(
        optimalPair(
            listOf("xa", "xb"),
            listOf("b", "a")
        )
    )
}

fun <T> List<T>.allPermutations() = sequence<List<T>> {
    val source = this@allPermutations
    val next = ArrayList(source)
    val n = source.size
    val c = IntArray(n) { 0 }

    yield(ArrayList(next))

    var i = 1
    while (i < n) {
        if (c[i] < i) {
            if (i % 2 == 0) {
                swap(next, 0, i)
            } else {
                swap(next, c[i], i)
            }
            yield(ArrayList(next))
            c[i]++
            i = 1
        } else {
            c[i] = 0
            i++
        }
    }
}

private fun <T> swap(list: java.util.ArrayList<T>, i: Int, j: Int) {
    val temp = list[i]
    list[i] = list[j]
    list[j] = temp
}

fun optimalPair(
    s1: List<String>,
    s2: List<String>,
    maxSize: Int = 4,
    maxLength: Int = 3,
    ignoreCase: Boolean = true
): OptimalResult? {
    if (s1.size != s2.size) {
        return null
    }
    val previousDistance = s1.zip(s2)
        .asSequence()
        .map { levenshteinDistanceWithLimit(it.first, it.second, ignoreCase) }
        .sum()

    if (s1.size > maxSize) {
        return findGreedy(s1, s2, maxLength, previousDistance)
    }

    return s1.allPermutations().iterator()
        .asSequence()
        .map { nextPermutation ->
            val zip = nextPermutation.zip(s2)
            val levenshtein = zip.asSequence()
                .map { levenshteinDistanceWithLimit(it.first, it.second, ignoreCase) }
                .sum()
            OptimalResult(
                savedOrder = previousDistance <= levenshtein,
                previousDistance = previousDistance,
                nextDistance = levenshtein,
                newOrder = zip
            )
        }
        .minByOrNull { it.nextDistance }
}

private fun findGreedy(
    s1: List<String>,
    s2: List<String>,
    maxLength: Int,
    previousDistance: Int
): OptimalResult {
    val iter1 = s1.toMutableSet()
    val iter2 = s2.toMutableSet()
    val pairs = mutableListOf<Pair<String, String>>()
    for (j in 0..1) {
        for (first in iter1) {
            if (first.length < maxLength && j == 0) {
                continue
            }
            val second: Pair<String, Int> = iter2.asSequence()
                .filter { it.length >= maxLength && j == 0 }
                .map { Pair(it, levenshteinDistance(first, it)) }
                .minByOrNull { it.second } ?: continue
            val findPair = Pair(first, second.first)
            pairs.add(findPair)
        }
        pairs.forEach {
            iter1.remove(it.first)
            iter2.remove(it.second)
        }
    }
    val levenshtein = pairs.map {
        levenshteinDistance(it.first, it.second)
    }.sum()
    val savedOrder = previousDistance <= levenshtein
    return OptimalResult(
        savedOrder = savedOrder,
        previousDistance = previousDistance,
        nextDistance = levenshtein,
        newOrder = if (savedOrder) s1.zip(s2) else pairs
    )
}

data class OptimalResult(
    val savedOrder: Boolean,
    val previousDistance: Int,
    val nextDistance: Int,
    val newOrder: List<Pair<String, String>>
)

fun levenshteinDistanceWithLimit(
    t1: String,
    t2: String,
    ignoreCase: Boolean = true,
    insertPrice: Int = 1,
    removePrice: Int = 1,
    replacePrice: Int = 2,
    minLength: Int = 0,
): Int {
    val s1 = if (ignoreCase) t1.toLowerCase() else t1
    val s2 = if (ignoreCase) t2.toLowerCase() else t2
    if (s1.length <= minLength && s2.length < minLength) {
        return 0
    }
    if (s1.length <= minLength) {
        return (s2.length - s1.length) * insertPrice
    }
    if (s2.length <= minLength) {
        return (-s2.length + s1.length) * removePrice
    }
    return levenshteinDistance(s1, s2, insertPrice, removePrice, replacePrice)
}

fun levenshteinDistance(
    s1: String,
    s2: String,
    insertPrice: Int = 1,
    removePrice: Int = 1,
    replacePrice: Int = 2
): Int {
    // memorize only previous line of distance matrix
    var prev = IntArray(s2.length + 1)

    for (j in 0 until s2.length + 1) {
        prev[j] = j * insertPrice
    }
    for (i in 1 until s1.length + 1) {
        // calculate current line of distance matrix
        val curr = IntArray(s2.length + 1)
        curr[0] = i * removePrice
        for (j in 1 until s2.length + 1) {
            val d1 = prev[j] + 1 * removePrice
            val d2 = curr[j - 1] + 1 * insertPrice
            var d3 = prev[j - 1]
            if (s1[i - 1] != s2[j - 1]) {
                d3 += 1 * replacePrice
            }
            curr[j] = min(min(d1, d2), d3)
        }
        prev = curr
    }
    return prev[s2.length]
}
