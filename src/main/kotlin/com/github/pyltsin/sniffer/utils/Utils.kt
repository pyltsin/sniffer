package com.github.pyltsin.sniffer.utils

import java.util.*
import kotlin.math.min


class Utils {
    companion object {
//        fun permute(arr: List<Any>, k: Int = 0) {
//            for (i in k until arr.size) {
//                Collections.swap(arr, i, k)
//                permute(arr, k + 1)
//                Collections.swap(arr, k, i)
//            }
//            if (k == arr.size - 1) {
//                println(Arrays.toString(arr.toTypedArray()))
//            }
//        }

        fun optimalPair(s1: List<String>, s2: List<String>, maxSize: Int = 4): OptimalResult? {
            if (s1.size != s2.size) {
                return null
            }
            val previous = s1.zip(s2)
                .asSequence()
                .map { levenshteinDistanceWithLimit(it.first, it.second) }
                .sum()

            if (s1.size > maxSize) {
                return null
            //                return findGreedy(s1, s2, previous)
            }

            //todo
            return null
        }

        data class OptimalResult(
            val savedOrder: Boolean,
            val previousDistance: Int,
            val nextDistance: Int,
            val newOrder: List<Pair<String, String>>
        )

        fun levenshteinDistanceWithLimit(
            s1: String,
            s2: String,
            insertPrice: Int = 1,
            removePrice: Int = 1,
            replacePrice: Int = 3,
            minLength: Int = 3
        ): Int {
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
            replacePrice: Int = 3
        ): Int {
            // memoize only previous line of distance matrix
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

                // define current line of distance matrix as previous
                prev = curr
            }
            return prev[s2.length]
        }
    }
}