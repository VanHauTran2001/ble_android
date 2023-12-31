package com.example.appblutooth.server.ble_ultis

object ByteUtils {
    fun reverse(value: ByteArray): ByteArray {
        val length = value.size
        val reversed = ByteArray(length)
        for (i in 0 until length) {
            reversed[i] = value[length - (i + 1)]
        }
        return reversed
    }
}