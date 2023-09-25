package com.mib.smsutil

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 *  author : cengyimou
 *  date : 2023/7/5 14:18
 *  description :
 */
object PermissionUtil {
	fun hasPermission(context: Context, vararg permissions: String): Boolean {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true
		}

		for (permission in permissions) {
			if (ContextCompat.checkSelfPermission(
					context,
					permission
				) != PackageManager.PERMISSION_GRANTED
			) {
				return false
			}
		}

		return true
	}
}