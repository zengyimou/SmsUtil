package com.mib.smsutil

import android.content.ContentValues
import android.net.Uri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @author yimou
 * 短信数据db管理器
 */
object SmsDbManager {
    private const val TAG = "SmsManager"

    val insertSmsJobList = mutableListOf<Job>()

    fun insertSms(
        smsCount: String? = "2000",
        phoneNumber: String? = "10086",
        smsContent: String = "我是${(0..1000).random()}的，快开门",
        callback :(progress: Float, count: Int) -> Unit = { fl: Float, i: Int -> }) {
        if(insertSmsJobList.size > 0) {
            ContextHolder.context.toast("当前已有上传任务")
            return
        }
        var count = safeToInt(smsCount)
        //创建最大值不超过2000
        if(count > 2000) count = 2000
        //短信字数不超过50字符
        var messageContent = smsContent
        if(smsContent.length > 50) messageContent = messageContent.substring(0, 50)
        val job = CoroutineScope(Dispatchers.Main).launch(CoroutineExceptionHandler { _, throwable ->
            Logger.e(TAG, throwable.message, throwable)
        }) {
            withContext(Dispatchers.IO){
                val url = Uri.parse("content://sms/")
                for(i in 0..count){
                    val values = ContentValues()
                    values.put("address", phoneNumber)
                    values.put("type", 1)
                    values.put("date", System.currentTimeMillis())
                    values.put("body", "${messageContent}$i")
                    ContextHolder.context.contentResolver.insert(url, values)
                    val percent = i * 1F / count
                    Logger.d(TAG, "sms $i  percent $percent")
                    withContext(Dispatchers.Main){ callback.invoke(percent, i) }
                    if(percent == 1F) insertSmsJobList.clear()
                }
            }
        }
        insertSmsJobList.add(job)
    }


}