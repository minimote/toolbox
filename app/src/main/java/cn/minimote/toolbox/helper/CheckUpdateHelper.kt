/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.net.toUri
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.CheckUpdate
import cn.minimote.toolbox.constant.CheckUpdate.Frequency
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.constant.Config.ConfigValues.CheckUpdateFrequency
import cn.minimote.toolbox.constant.Config.ConfigValues.NetworkAccessModeValues
import cn.minimote.toolbox.constant.NetworkType
import cn.minimote.toolbox.viewModel.MyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object CheckUpdateHelper {

    // 自动检查更新
    fun autoCheckUpdate(
        context: Context,
        viewModel: MyViewModel,
    ) {
        // 不自动检查更新
        if(viewModel.updateCheckGap == Frequency.NEVER) {
            return
        }
        // 如果存在下载ID，说明之前的APK没有安装
        val downloadId = ConfigHelper.getConfigValue(
            key = ConfigKeys.DOWNLOAD_ID,
            viewModel = viewModel,
        ).toString().toLongOrNull()
        if(downloadId != null && downloadId >= 0) {
            installApk(
                context = context,
                viewModel = viewModel,
                downloadId = downloadId,
            )
        } else
            if(System.currentTimeMillis() >= viewModel.nextUpdateCheckTime) {
//            Toast.makeText(
//                context,
//                getFormatTimeString(System.currentTimeMillis()) + "\n" + getFormatTimeString(
//                    viewModel.nextUpdateCheckTime.value!!
//                ),
//                Toast.LENGTH_SHORT
//            ).show()
                checkNetworkAccessModeAndCheckUpdate(
                    context = context,
                    viewModel = viewModel,
                    silence = true,
                )
            }
    }


    // 更新上次检查时间和下次检查时间
    private fun updateCheckTime(
        viewModel: MyViewModel,
    ) {
        ConfigHelper.updateConfigValue(
            key = ConfigKeys.LAST_CHECK_UPDATE_TIME,
            value = System.currentTimeMillis(),
            viewModel = viewModel,
        )
        ConfigHelper.saveUserConfig(
            viewModel = viewModel,
        )
    }


    // 将时间戳转换为格式化时间
    fun getFormatTimeString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("M月d日 HH:mm:ss", Locale.CHINA)
        return dateFormat.format(Date(timestamp))
    }


    // 获取更新频率的字符串
    fun getUpdateFrequencyString(
        context: Context,
        frequency: String,
    ): String {
        return when(frequency) {
            CheckUpdateFrequency.DAILY -> {
                context.getString(R.string.check_update_frequency_daily)
            }

            CheckUpdateFrequency.WEEKLY -> {
                context.getString(R.string.check_update_frequency_weekly)
            }

            CheckUpdateFrequency.MONTHLY -> {
                context.getString(R.string.check_update_frequency_monthly)
            }

            CheckUpdateFrequency.NEVER -> {
                context.getString(R.string.check_update_frequency_never)
            }

            else -> {
                throw IllegalArgumentException("非法的频率：$frequency")
            }
        }
    }


    // 获取更新间隔
    fun getUpdateCheckGapLong(
        frequency: String,
    ): Long {
        return when(frequency) {
            CheckUpdateFrequency.DAILY -> Frequency.DAILY
            CheckUpdateFrequency.WEEKLY -> Frequency.WEEKLY
            CheckUpdateFrequency.MONTHLY -> Frequency.MONTHLY
            CheckUpdateFrequency.NEVER -> Frequency.NEVER

            else -> {
                throw IllegalArgumentException("非法的频率：$frequency")
            }
        }
    }


    // 检查网络访问模式再检查更新
    fun checkNetworkAccessModeAndCheckUpdate(
        context: Context,
        viewModel: MyViewModel,
        silence: Boolean, // 是否静默检查
    ) {
        // 获取网络类型
        val networkType = NetworkHelper.getNetworkType(viewModel.myContext)
        // 网络未连接
        if(networkType == NetworkType.DISCONNECTED) {
            if(!silence) {
                Toast.makeText(
                    viewModel.myContext,
                    getString(viewModel.myContext, R.string.toast_no_network),
                    Toast.LENGTH_SHORT,
                ).show()
            }
            return
        }

        val networkTypeString = NetworkHelper.getNetworkTypeString(
            context = viewModel.myContext,
            networkType = networkType,
        )

        when(NetworkHelper.getNetworkAccessMode(networkType, viewModel)) {
            NetworkAccessModeValues.ALERT -> {
                DialogHelper.showConfirmDialog(
                    context = context,
                    viewModel = viewModel,
                    titleText = context.getString(
                        R.string.dialog_message_network_check_update,
                        networkTypeString,
                    ),
                    positiveAction = {
                        // 经过确认弹窗，执行非静默检查更新
                        checkUpdate(
                            context = context,
                            viewModel = viewModel,
                            silence = false,
                        )
                    }
                )
            }

            NetworkAccessModeValues.DENY -> {
                if(!silence) {
                    Toast.makeText(
                        viewModel.myContext,
                        viewModel.myContext.getString(
                            R.string.toast_network_access_denied,
                            networkTypeString,
                        ),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }

            NetworkAccessModeValues.ALLOW -> {
                checkUpdate(
                    context = context,
                    viewModel = viewModel,
                    silence = silence,
                )
            }
        }
    }


    // 检查更新
    fun checkUpdate(
        context: Context,
        viewModel: MyViewModel,
        silence: Boolean, // 是否静默检查
    ) {
        val checkingUpdateToast = Toast.makeText(
            context, context.getString(R.string.checking_update),
            Toast.LENGTH_LONG,
        )
        if(!silence) {
            checkingUpdateToast.show()
        }

        updateCheckTime(viewModel)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val giteeApiUrl = CheckUpdate.GITEE_API_URL
                val url = URL(
                    giteeApiUrl.replace(
                        CheckUpdate.GITEE_API_OWNER,
                        context.getString(R.string.author_name_en),
                    ).replace(
                        CheckUpdate.GITEE_API_REPO,
                        context.getString(R.string.app_name_en),
                    )
                )
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = CheckUpdate.GITEE_API_REQUEST_METHOD
                connection.connectTimeout = CheckUpdate.TIMEOUT

                if(connection.responseCode != HttpURLConnection.HTTP_OK) {
                    if(!silence) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.failed_to_check_update),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    return@launch
                }

                val inputStream = connection.inputStream
                val json = inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(json)
                val releaseNotes = jsonObject.getString(CheckUpdate.GITEE_API_RELEASE_NOTES_KEY)

                val remoteVersion = getRemoteVersion(jsonObject)
                val localVersion = getLocalVersion(viewModel)
                val downloadUrl = getDownloadUrl(jsonObject)

                if(hasNewVersion(remoteVersion, localVersion) and downloadUrl.isNotBlank()) {
                    withContext(Dispatchers.Main) {
                        showUpdateDialog(
                            context = context,
                            viewModel = viewModel,
                            remoteVersion = remoteVersion,
                            releaseNotes = releaseNotes,
                            downloadUrl = downloadUrl,
                            checkingUpdateToast = checkingUpdateToast,
                        )
                    }
                } else {
                    if(!silence) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.latest_version_installed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                if(!silence) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_occurred_while_check_update),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    // 获取远端版本号
    private fun getRemoteVersion(
        jsonObject: JSONObject,
    ): String {
        val assets = jsonObject.getJSONArray(CheckUpdate.GITEE_API_ASSETS_KEY)
        for(i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            var fileName = asset.getString(CheckUpdate.GITEE_API_FILENAME_KEY)
            if(fileName.endsWith(CheckUpdate.FILENAME_SUFFIX)) {
                fileName = fileName.removeSuffix(CheckUpdate.FILENAME_SUFFIX)
                // toolbox_1.1.0-250415.110640
                try {
                    return fileName.split("_")[1]
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return ""
    }


    // 获取本地版本号
    private fun getLocalVersion(
        viewModel: MyViewModel,
    ): String {
        return viewModel.myVersionName.removeSuffix(CheckUpdate.LOCAL_VERSION_SUFFIX)
    }


    // 获取版本号的整数列表
    private fun getVersionIntList(
        versionString: String,
    ): List<Int> {
        // 1.1.0-250415.110640-debug
        val newVersionString = versionString.replace(
            CheckUpdate.VERSION_NAME_SEPARATOR_REGEX,
            CheckUpdate.VERSION_NAME_SPLIT_CHAR,
        )

//        Log.e("获取版本号的整数列表", "versionString: $newVersionString")
        val versionIntList = newVersionString.split(
            CheckUpdate.VERSION_NAME_SPLIT_CHAR
        ).map {
            it.toIntOrNull() ?: -1
        }
        return versionIntList
    }


    // 比较版本号
    private fun hasNewVersion(
        remoteVersion: String,
        localVersion: String,
    ): Boolean {
        val remoteVersionList = getVersionIntList(remoteVersion)
        val localVersionList = getVersionIntList(localVersion)

//        Log.e("检查更新", "远端：$remoteVersionList, 本地：$localVersionList")

        for((remoteCode, localCode) in remoteVersionList.zip(localVersionList)) {
            if(remoteCode > localCode) {
                return true
            } else if(remoteCode < localCode) {
                return false
            }
        }
        return false
    }


    // 获取下载链接
    private fun getDownloadUrl(
        jsonObject: JSONObject,
    ): String {
        val assets = jsonObject.getJSONArray(CheckUpdate.GITEE_API_ASSETS_KEY)
        for(i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            if(asset.getString(CheckUpdate.GITEE_API_FILENAME_KEY)
                    .endsWith(CheckUpdate.FILENAME_SUFFIX)
            ) {
                return asset.getString(CheckUpdate.GITEE_API_DOWNLOAD_URL_KEY)
            }
        }
        return ""
    }


    // 显示更新确认对话框
    private fun showUpdateDialog(
        context: Context,
        viewModel: MyViewModel,
        remoteVersion: String,
        releaseNotes: String,
        downloadUrl: String,
        checkingUpdateToast: Toast? = null,
    ) {
        checkingUpdateToast?.cancel()

        DialogHelper.showConfirmDialog(
            context = context,
            viewModel = viewModel,
            titleText = context.getString(
                R.string.new_version_available,
                remoteVersion,
            ),
            messageText = context.getString(
                R.string.whether_download_now,
                releaseNotes.trim() + "\n\n",
            ),
            positiveButtonText = context.getString(R.string.download_now),
            negativeButtonText = context.getString(R.string.download_later),
            positiveAction = {
                // 处理立即下载逻辑
                downloadUpdate(
                    context = context,
                    viewModel = viewModel,
                    downloadUrl = downloadUrl,
                )
            }
        )

//        val builder = AlertDialog.Builder(context)
//        val dialogView = LayoutInflater.from(context)
//            .inflate(R.layout.dialog_update_note, null)
//        builder.setView(dialogView)
//
//        val textViewTitle = dialogView.findViewById<TextView>(R.id.textViewTitle)
//        val textViewMessage = dialogView.findViewById<TextView>(R.id.textViewMessage)
//
//        textViewTitle.text = context.getString(
//            R.string.new_version_available,
//            remoteVersion,
//        )
//        textViewMessage.text = context.getString(
//            R.string.whether_download_now,
//            releaseNotes + "\n",
//        )
//
//        builder.setPositiveButton(context.getString(R.string.download_now)) { dialog, _ ->
//            VibrationHelper.vibrateOnClick(viewModel)
//            // 处理立即下载逻辑
//            downloadUpdate(
//                context = context,
//                viewModel = viewModel,
//                downloadUrl = downloadUrl,
//            )
//            dialog.dismiss()
//        }
//        builder.setNegativeButton(context.getString(R.string.download_later)) { dialog, _ ->
//            VibrationHelper.vibrateOnClick(viewModel)
//            dialog.dismiss()
//        }
//        builder.show()
    }


    // 下载更新
    private fun downloadUpdate(
        context: Context,
        viewModel: MyViewModel,
        downloadUrl: String,
    ) {
        val downloadingToast = Toast.makeText(
            context,
            context.getString(R.string.downloading_please_wait),
            Toast.LENGTH_SHORT,
        )
        downloadingToast.show()

        // 创建 DownloadManager 请求
        val request = DownloadManager.Request(downloadUrl.toUri())
        // 允许的网络类型
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(context.getString(R.string.downloading_update_title))
        request.setDescription(context.getString(R.string.downloading_update_description))
        request.setNotificationVisibility(
            // 下载中显示通知，完成后消失
            DownloadManager.Request.VISIBILITY_VISIBLE
        )

        // 从下载链接中提取文件名
        val fileName = URLDecoder.decode(
            downloadUrl.substringAfterLast('/'),
            StandardCharsets.UTF_8.toString(),
        )
        request.setDestinationUri(
            File(
                viewModel.savePath,
                fileName,
            ).toUri()
        )

        // 获取系统的 DownloadManager 服务
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // 启动轮询监控下载状态
        monitorDownloadStatus(
            context = context,
            downloadId = downloadId,
            viewModel = viewModel,
            downloadingToast = downloadingToast,
        )
    }


    // 轮询查询下载状态
    private fun monitorDownloadStatus(
        context: Context,
        downloadId: Long,
        viewModel: MyViewModel,
        gapTime: Long = CheckUpdate.MONITOR_DOWNLOAD_STATUS_GAP_RIME,
        downloadingToast: Toast? = null,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            while(true) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if(cursor != null && cursor.moveToFirst()) {
                    // 检查列索引是否有效
                    val statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if(statusColumnIndex == -1) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.unable_get_download_status),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        cursor.close()
                        break
                    }

                    val status = cursor.getInt(statusColumnIndex)
                    when(status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
//                            withContext(Dispatchers.Main) {
//                                Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show()
//                            }
                            downloadingToast?.cancel()
                            // 调用安装逻辑
                            installApk(
                                context = context,
                                downloadId = downloadId,
                                viewModel = viewModel,
                            )
                            cursor.close()
                            break // 停止轮询
                        }

                        DownloadManager.STATUS_FAILED -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.download_failed),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                            cursor.close()
                            break // 停止轮询
                        }

                        DownloadManager.STATUS_PAUSED -> {
                            // 下载暂停，可选择继续轮询
                        }

                        DownloadManager.STATUS_RUNNING -> {
                            // 下载进行中，可选择显示进度
                        }
                    }
                }
                cursor?.close()
                // 延迟一段时间再继续轮询
                delay(gapTime)
            }
        }
    }


    // 调用系统安装器
    private fun installApk(
        context: Context,
        downloadId: Long,
        viewModel: MyViewModel,
    ) {
//        Toast.makeText(
//            context, "准备安装", Toast.LENGTH_SHORT
//        ).show()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = downloadManager.getUriForDownloadedFile(downloadId)

        if(uri != null) {
            // 申请“允许安装未知来源的应用”权限
            if(!context.packageManager.canRequestPackageInstalls()) {
                //  保存下载ID，便于启动时直接调用安装程序
                ConfigHelper.updateConfigValue(
                    key = ConfigKeys.DOWNLOAD_ID,
                    value = downloadId,
                    viewModel = viewModel,
                )
                ConfigHelper.saveUserConfig(viewModel = viewModel)

                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        context.getString(R.string.please_grant_install_permission),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = "package:${context.packageName}".toUri()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)


                context.startActivity(intent)
            } else {
                // 删除下载ID，避免下次启动时重复调用安装程序
                ConfigHelper.deleteConfigValue(
                    key = ConfigKeys.DOWNLOAD_ID,
                    viewModel = viewModel,
                )
                ConfigHelper.saveUserConfig(viewModel = viewModel)

                startInstallIntent(context, uri)
            }
        }

    }

//    // 将应用带到前台
//    private fun bringAppToForeground(context: Context) {
//        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
//        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        context.startActivity(launchIntent)
//    }


    // 安装 APK
    private fun startInstallIntent(ctx: Context, uri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
        installIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        ctx.startActivity(installIntent)
    }


}