package com.mib.smsutil

import android.content.ContentValues
import android.provider.CallLog
import com.mib.smsutil.ContextHolder.context
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date


/**
 *  author : cengyimou
 *  date : 2023/10/7 10:58
 *  description :
 */
object CalllogManager {
	const val TAG = "CalllogManager"

	private val QUERY_PROJECTION_ITEMS = arrayOf(
		CallLog.Calls.CACHED_NAME,//通话记录的联系人
		CallLog.Calls.NUMBER,//通话记录电话号码
		CallLog.Calls.DATE,//通话记录的日期
		CallLog.Calls.DURATION,//通话时长
		CallLog.Calls.TYPE//通话类型
	)

	val insertJobList = mutableListOf<Job>()

	/**
	 * 插入通话记录数据
	 * @param callback Function2<[@kotlin.ParameterName] Int, [@kotlin.ParameterName] Boolean, Unit>
	 */
	fun insertCallLogData(insertCount: Int, callback: (percent: Float, size: Int, success: Boolean) -> Unit){
		if(insertCount <= 1) return
		val job = CoroutineScope(Dispatchers.Main).launch(CoroutineExceptionHandler { _, _ ->
			callback.invoke(0F,-1, false)
		}) {
			val time = Date().time
			withContext(Dispatchers.IO){
				for(i in 1 until insertCount){
					val values = ContentValues()
					values.put(CallLog.Calls.CACHED_NAME, "mib-test")
					values.put(CallLog.Calls.NUMBER, "10086")
					values.put(CallLog.Calls.DATE, time)
					values.put(CallLog.Calls.DURATION, 100)
					values.put(CallLog.Calls.TYPE, 1) //未接
					values.put(CallLog.Calls.NEW, 1) //0已看1未看
					val cursor = context.contentResolver.insert(CallLog.Calls.CONTENT_URI, values)
					val percent = i * 1F / insertCount
					Logger.d(TAG, "calllog $i  percent $percent")
					withContext(Dispatchers.Main){
						callback.invoke(percent, i, true)
					}
				}
			}
		}
		insertJobList.add(job)
	}

}