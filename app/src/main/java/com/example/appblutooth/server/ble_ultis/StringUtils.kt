package com.example.appblutooth.server.ble_ultis

import android.util.Log
import androidx.annotation.Nullable
import java.io.UnsupportedEncodingException

object StringUtils {
    private const val TAG = "StringUtils"
    private fun byteToHex(b: Byte): String {
        val char1 = Character.forDigit(b.toInt() and 0xF0 shr 4, 16)
        val char2 = Character.forDigit(b.toInt() and 0x0F, 16)
        return String.format("0x%1\$s%2\$s", char1, char2)
    }

    fun byteArrayInHexFormat(byteArray: ByteArray?): String? {
        if (byteArray == null) {
            return null
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append("{ ")
        for (i in byteArray.indices) {
            if (i > 0) {
                stringBuilder.append(", ")
            }
            val hexString = byteToHex(
                byteArray[i]
            )
            stringBuilder.append(hexString)
        }
        stringBuilder.append(" }")
        return stringBuilder.toString()
    }

    fun bytesFromString(string: String): ByteArray {
        var stringBytes = ByteArray(0)
        try {
            stringBytes = string.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Failed to convert message string to byte array")
        }
        return stringBytes
    }

    @Nullable
    fun stringFromBytes(bytes: ByteArray?): String? {
        var byteString: String? = null
        try {
            byteString = String(bytes!!, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Unable to convert message bytes to string")
        }
        return byteString
    }
}