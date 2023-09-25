package com.mib.smsutil

import android.app.Application

/**
 *  author : cengyimou
 *  date : 2023/7/5 12:01
 *  description :
 */
class App: Application() {

	override fun onCreate() {
		super.onCreate()
		ContextHolder.init(this)
	}
}