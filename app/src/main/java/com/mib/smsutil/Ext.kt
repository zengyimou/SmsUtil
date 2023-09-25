package com.mib.smsutil

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.TypedValue
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes

/**
 *  author : cengyimou
 *  date : 2023/7/5 11:41
 *  description :
 */
/**
 * 显示Toast
 */
fun Context.toast(
	message: String,
	duration: Int = Toast.LENGTH_SHORT,
	gravity: Int = Gravity.BOTTOM
) {
	Toast.makeText(this.applicationContext, message, duration).apply {
		setGravity(gravity, 0, dp2px(48F).toInt())
	}.show()
}

/**
 * 显示Toast
 */
fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
	toast(getString(message), duration)
}

fun Context.dp2px(dpValue: Float) =
	TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, resources.displayMetrics)

/**
 * sp转px
 */
fun Context.sp2px(spValue: Float) =
	TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, resources.displayMetrics)

/**
 * px转dp
 */
fun Context.px2dp(pxValue: Float): Int {
	val scale = resources.displayMetrics.density
	return (pxValue / scale + 0.5f).toInt()
}

/**
 * px转sp
 */
fun Context.px2Sp(pxValue: Float): Int {
	val scale = resources.displayMetrics.scaledDensity
	return (pxValue / scale + 0.5f).toInt()
}

/**
 * 判断当前应用是否为默认短信应用
 * @receiver ComponentActivity
 * @return Boolean
 */
fun ComponentActivity.isDefaultSmsApp(): Boolean{
	val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this)//获取手机当前设置的默认短信应用的包名
	return defaultSmsApp != null && defaultSmsApp.equals(this.packageName)
}

/**
 * 打开设置默认短信应用界面
 * @receiver ComponentActivity
 */
fun ComponentActivity.openDefaultSmsPage(defaultSmsApps: ActivityResultLauncher<Intent>){
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
		val rm: RoleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
		defaultSmsApps.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
	}else{
		val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
		intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
		defaultSmsApps.launch(intent)
	}
}