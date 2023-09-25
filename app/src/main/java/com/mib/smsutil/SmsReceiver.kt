package com.mib.smsutil

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/**
 *  author : cengyimou
 *  date : 2022/7/15 10:46 上午
 *  description : 自定义短信接收广播接收器
 */
class SmsReceiver : BroadcastReceiver() {

    @SuppressLint("SimpleDateFormat", "MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        Logger.d(TAG, "onReceive")

    }



    companion object {
        const val TAG = "SmsReceiver"
    }
}