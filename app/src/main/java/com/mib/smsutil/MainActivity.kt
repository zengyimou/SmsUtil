package com.mib.smsutil

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.mib.smsutil.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	var startInsertJob = false

	private val requiredCommonPermissions: Array<String> by lazy {
		val commonArray = mutableListOf(
			Manifest.permission.SEND_SMS,
			Manifest.permission.READ_SMS,
			Manifest.permission.RECEIVE_SMS,
		)
		commonArray.toTypedArray()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		checkPermissions {
			if (it) checkDefaultSmsApp()
		}
		init()
	}

	private fun init() {
		with(receiver = binding) {
			btnApplyPermissions.setOnClickListener {
				checkPermissions {
					if (it && !checkDefaultSmsApp()) openDefaultSmsPage(defaultSmsApps)
				}
			}
			btnUploadSms.setOnClickListener {
				if(startInsertJob){
					btnUploadSms.text = "上传短信"
					if(SmsDbManager.insertSmsJobList.size > 0){
						SmsDbManager.insertSmsJobList[0].cancel()
						SmsDbManager.insertSmsJobList.clear()
					}
				}else{
					SmsDbManager.insertSms(smsCount = 2000, phoneNumber = "10086") { percent, count ->
						btnUploadSms.text = "停止上传短信"
						tvUploadSmsProgress.text = "上传短信进度：${
							BigDecimal(percent.toString()).multiply(BigDecimal(100)).setScale(1, RoundingMode.HALF_UP)
						} % 已上传${count}条短信"
						tvUploadSmsProgress.visibility = View.VISIBLE
					}
				}
				startInsertJob = !startInsertJob
			}
		}
	}

	override fun onResume() {
		super.onResume()
		
	}

	private fun checkPermissions(callback: (success: Boolean) -> Unit = {}) {
		if (PermissionUtil.hasPermission(this, *requiredCommonPermissions)) {
			callback(true)
		} else {
			registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
				val grantedPermissions = mutableListOf<String>()
				val deniedPermissions = mutableListOf<String>()

				permissions.entries.forEach { entry ->
					val permission = entry.key
					val isGranted = entry.value
					if (isGranted) {
						grantedPermissions.add(permission)
					} else {
						deniedPermissions.add(permission)
					}
				}
				var allGranted = true
				// 处理授予的权限
				if (grantedPermissions.isNotEmpty()) {
					// 继续执行相关操作
					// ...
				}

				// 处理拒绝的权限
				if (deniedPermissions.isNotEmpty()) {
					// 处理拒绝情况
					toast("必须要授权权限才能使用插入短信功能")
					allGranted = false
				}
				callback(allGranted)
			}.launch(requiredCommonPermissions)
		}
	}

	/**
	 * 打开默认电话应用设置界面
	 */
	private val defaultSmsApps = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == RESULT_OK) {
			toast("授权成功")
		}
		checkDefaultSmsApp()
	}

	/**
	 * 检查是否未默认短信应用
	 */
	private fun checkDefaultSmsApp(): Boolean {
		var isDefault = false
		//判断默认短信应用
		if (!isDefaultSmsApp()) {
			isDefault = false
			binding.tvPermissionStatus.text = "默认短信应用: NO"
		} else {
			isDefault = true
			binding.tvPermissionStatus.text = "默认短信应用: YES"
		}
		with(receiver = binding) {
			btnUploadSms.visibility = if (isDefaultSmsApp()) View.VISIBLE else View.GONE
			tvUploadSmsInfo.visibility = if (isDefaultSmsApp()) View.VISIBLE else View.GONE
		}
		return isDefault
	}
}