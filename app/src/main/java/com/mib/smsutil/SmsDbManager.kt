package com.mib.smsutil

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.mib.smsutil.ContextHolder.context
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Date


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
        type: String = "1",
        time: String = "0",
        callback :(progress: Float, count: Int) -> Unit = { fl: Float, i: Int -> }) {
        if(insertSmsJobList.size > 0) {
            ContextHolder.context.toast("当前已有上传任务")
            return
        }
        var count = safeToInt(smsCount)
        var currentTime = safeToLong(time)
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
                    val date = if(currentTime != 0L && currentTime.toString().length == 13){
                        currentTime
                    }else{
                        System.currentTimeMillis()
                    }
                    val values = ContentValues()
                    values.put("address", phoneNumber)
                    values.put("type", type)
                    values.put("date", date)
                    values.put("body", "${messageContent}")
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


    fun customInsert(callback :(progress: Float, count: Int) -> Unit = { fl: Float, i: Int -> }) {
        if(insertSmsJobList.size > 0) {
            context.toast("当前已有上传任务")
            return
        }
        val jsonList = loadJsonFromAssets("patamoney_sms_list.json", context)
        val jsonArray = Gson().fromJson(jsonList, JsonArray::class.java)
        val job = CoroutineScope(Dispatchers.Main).launch(CoroutineExceptionHandler { _, throwable ->
            Logger.e(TAG, throwable.message, throwable)
        }) {
            withContext(Dispatchers.IO){
                val url = Uri.parse("content://sms/")
                jsonArray.forEachIndexed{ index, jsonElement ->
                    val jsonObj = jsonElement.asJsonObject
                    val values = ContentValues()
                    values.put("address", jsonObj["phone"].asString)
                    values.put("type", jsonObj["type"].asString)
                    values.put("date", jsonObj["msgTimeStamp"].asLong)
                    values.put("body", jsonObj["msg"].asString)
                    context.contentResolver.insert(url, values)
                    val percent = (index + 1) * 1F / jsonArray.size()
                    withContext(Dispatchers.Main){ callback.invoke(percent, index) }
                    if(percent == 1F) insertSmsJobList.clear()

                }
            }
        }
        insertSmsJobList.add(job)
    }

    private fun loadJsonFromAssets(fileName: String?, context: Context): String {
        try {
            val inputStream = context.assets.open(fileName!!)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            return String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            return "{}"
        }
    }

}