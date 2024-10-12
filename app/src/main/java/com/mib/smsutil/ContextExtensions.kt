package com.mib.smsutil

import android.content.Context
import android.content.Intent
import android.os.Build

/**
 *  author : cengyimou
 *  date : 2023/3/22 10:45
 *  description :
 */

fun Context.startSafeService(intent: Intent) {
    try {
        startService(intent)
    } catch (e: Exception) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }
    }
}

/**
 * 安全转int
 * @param s String
 * @return Int
 */
fun safeToInt(s: String?): Int {
    return try {
        s?.toInt()?: 0
    } catch (ex: NumberFormatException) {
        2000
    }
}

fun safeToLong(s: String?): Long {
    return try {
        s?.toLong()?: 0L
    } catch (ex: NumberFormatException) {
        0L
    }
}