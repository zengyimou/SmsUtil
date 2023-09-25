package com.mib.smsutil

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 *  author : cengyimou
 *  date : 2023/7/5 12:02
 *  description :
 */
@SuppressLint("StaticFieldLeak")
object ContextHolder {
	lateinit var context: Context
		private set

	fun init(app: Application) {
		this.context = app.applicationContext
	}
}