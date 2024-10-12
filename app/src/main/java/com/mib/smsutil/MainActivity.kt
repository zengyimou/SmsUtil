package com.mib.smsutil

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mib.smsutil.ContextHolder.context
import com.mib.smsutil.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding

	private var startInsertJob = false

	private var startInsertCalllog = false

	private val requiredCommonPermissions: Array<String> by lazy {
		val commonArray = mutableListOf(
			Manifest.permission.SEND_SMS,
			Manifest.permission.READ_SMS,
			Manifest.permission.RECEIVE_SMS,
		)
		commonArray.toTypedArray()
	}

	private val requiredCalllogPermissions: Array<String> by lazy {
		val commonArray = mutableListOf(
			Manifest.permission.READ_CALL_LOG,
			Manifest.permission.WRITE_CALL_LOG,
		)
		commonArray.toTypedArray()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		smsRegister.launch(requiredCommonPermissions)
		init()
	}

	private fun init() {
		with(receiver = binding) {
			btnApplyPermissions.setOnClickListener {
				smsRegister.launch(requiredCommonPermissions)
			}
			btnUploadSms.setOnClickListener {
				if (startInsertJob) {
					btnUploadSms.text = context.getString(R.string.upload_sms)
					if (SmsDbManager.insertSmsJobList.size > 0) {
						SmsDbManager.insertSmsJobList[0].cancel()
						SmsDbManager.insertSmsJobList.clear()
					}
				} else {
					val smsCount = if(edSmsCount.text?.isNotEmpty() == true) edSmsCount.text.toString() else "2000"
					val phoneNumber = if(edPhoneNum.text?.isNotEmpty() == true) edPhoneNum.text.toString() else "10086"
					val smsContent = if(edMessageContent.text?.isNotEmpty() == true) edMessageContent.text.toString() else "我是${(0..1000).random()}的，快开门"
					val type = if(edSmsType.text?.isNotEmpty() == true) edSmsType.text.toString() else "1"
					val time = if(edSmsTime.text?.isNotEmpty() == true) edSmsTime.text.toString() else "0"

					SmsDbManager.insertSms(
						smsCount = smsCount,
						phoneNumber = phoneNumber,
						smsContent = smsContent,
						type = type,
						time = time
					) { percent, count ->
						btnUploadSms.text = context.getString(R.string.stop_upload_sms)
						tvUploadSmsProgress.text = "上传短信进度：${
							BigDecimal(percent.toString()).multiply(BigDecimal(100)).setScale(1, RoundingMode.HALF_UP)
						} % 已上传${count}条短信"
						tvUploadSmsProgress.visibility = View.VISIBLE
						if(percent == 1F) {
							btnUploadSms.text = context.getString(R.string.upload_sms)
							startInsertJob = !startInsertJob
						}
					}
				}
				startInsertJob = !startInsertJob
			}

			//上传通话记录
			btnUploadCallLog.setOnClickListener {
				if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
					if (startInsertCalllog) {
						btnUploadCallLog.text = context.getString(R.string.upload_calllog)
						if (CalllogManager.insertJobList.size > 0) {
							CalllogManager.insertJobList[0].cancel()
							CalllogManager.insertJobList.clear()
						}
					} else {
						CalllogManager.insertCallLogData(insertCount = 1000) { percent, size, success ->
							btnUploadCallLog.text = context.getString(R.string.stop_upload_calllog)
							//上传进度
							binding.tvUploadCallLogProgress.text = "上传通话记录进度：${
								BigDecimal(percent.toString()).multiply(BigDecimal(100)).setScale(1, RoundingMode.HALF_UP)
							} % 已上传${size}条通话记录"
						}
					}
					startInsertCalllog = !startInsertCalllog
				} else {
					calllogRegister.launch(requiredCalllogPermissions)
				}
			}
		}
	}

	val smsRegister = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
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
			toast(context.getString(R.string.limit_default_sms_permissions))
			allGranted = false
		}

		if (allGranted && !checkDefaultSmsApp()) openDefaultSmsPage(defaultSmsApps)
	}

	val calllogRegister = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
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
			toast(context.getString(R.string.limit_default_calllog_permissions))
			allGranted = false
		}

		if (allGranted) {
			CalllogManager.insertCallLogData(insertCount = 1000) { percent, size, success ->
				//上传进度
				binding.tvUploadCallLogProgress.text = "上传通话记录进度：${
					BigDecimal(percent.toString()).multiply(BigDecimal(100)).setScale(1, RoundingMode.HALF_UP)
				} % 已上传${size}条通话记录"
			}
		}
	}

	/**
	 * 打开默认电话应用设置界面
	 */
	private val defaultSmsApps = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == RESULT_OK) {
			toast(context.getString(R.string.request_success))
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
			binding.tvPermissionStatus.text = context.getString(R.string.default_sms_application, context.getString(R.string.no))
		} else {
			isDefault = true
			binding.tvPermissionStatus.text = context.getString(R.string.default_sms_application, context.getString(R.string.yes))
		}
		with(receiver = binding) {
			btnUploadSms.visibility = if (isDefaultSmsApp()) View.VISIBLE else View.GONE
			tvUploadSmsInfo.visibility = if (isDefaultSmsApp()) View.VISIBLE else View.GONE
			llCustomConfig.visibility = if (isDefaultSmsApp()) View.VISIBLE else View.GONE
		}
		return isDefault
	}
}