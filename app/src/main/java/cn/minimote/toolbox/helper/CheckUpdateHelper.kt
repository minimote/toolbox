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
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getString
import androidx.core.net.toUri
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.CheckUpdate
import cn.minimote.toolbox.constant.CheckUpdate.Frequency
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.constant.Config.ConfigValues.CheckUpdateFrequency
import cn.minimote.toolbox.constant.Config.ConfigValues.NetworkAccessModeValues
import cn.minimote.toolbox.constant.NetworkType
import cn.minimote.toolbox.helper.ConfigHelper.deleteConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.saveUserConfig
import cn.minimote.toolbox.helper.ConfigHelper.updateConfigValue
import cn.minimote.toolbox.helper.DialogHelper.showMyDialog
import cn.minimote.toolbox.helper.NetworkHelper.getNetworkAccessMode
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
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


object CheckUpdateHelper {

    // 自动检查更新
    fun autoCheckUpdate(
        context: Context,
        viewModel: MyViewModel,
    ) {
        // 不自动检查更新
        if(viewModel.updateCheckGapLong == Frequency.NEVER) {
            return
        }
        // 如果存在下载ID，说明之前的APK没有安装
        val downloadId = viewModel.getConfigValue(
            key = ConfigKeys.DOWNLOAD_ID,
        ).toString().toLongOrNull()
        if(downloadId != null && downloadId >= 0) {
            installApk(
                context = context,
                viewModel = viewModel,
                downloadId = downloadId,
            )
        } else
            if(System.currentTimeMillis() >= viewModel.nextUpdateCheckTime) {
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
        viewModel.updateConfigValue(
            key = ConfigKeys.CheckUpdate.LAST_CHECK_UPDATE_TIME,
            value = System.currentTimeMillis(),
        )
        viewModel.saveUserConfig()
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
        // 检查更新时是否忽略网络限制
        val checkUpdateIgnoreNetworkRestrictions = viewModel.getConfigValue(
            key = ConfigKeys.CheckUpdate.CHECK_UPDATE_IGNORE_NETWORK_RESTRICTIONS,
        )
        if(checkUpdateIgnoreNetworkRestrictions == true) {
            checkUpdate(
                context = context,
                viewModel = viewModel,
                silence = silence,
            )
            return
        }

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

        when(viewModel.getNetworkAccessMode(networkType)) {
            NetworkAccessModeValues.ALERT -> {
                DialogHelper.setAndShowDefaultDialog(
                    context = context,
                    viewModel = viewModel,
                    messageText = context.getString(
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

                val remoteVersion = getRemoteVersion(jsonObject)
                val localVersion = getLocalVersion(viewModel)
                val simpleLocalVersion = getSimpleVersion(localVersion)
                val simpleRemoteVersion = getSimpleVersion(remoteVersion)
                val downloadUrl = getDownloadUrl(jsonObject)

                if(
                    hasNewVersion(remoteVersion, localVersion) && downloadUrl.isNotBlank()
                ) {

//                val releaseNotes = jsonObject.getString(CheckUpdate.GITEE_API_RELEASE_NOTES_KEY)
                    val releaseNotes = getUpdateLogsBetweenVersions(
                        context = context,
                        simpleLocalVersion = simpleLocalVersion,
                        simpleRemoteVersion = simpleRemoteVersion,
                    )

//                LogHelper.e(
//                    "新旧版本号",
//                    "旧<${localVersion}>,新<${remoteVersion}>",
//                )


                    withContext(Dispatchers.Main) {
                        showUpdateDialog(
                            context = context,
                            viewModel = viewModel,
                            simpleLocalVersion = simpleLocalVersion,
                            simpleRemoteVersion = simpleRemoteVersion,
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


    private fun getSimpleVersion(
        version: String,
    ): String {
        val versionNameSeparatorRegex = CheckUpdate.VERSION_NAME_SEPARATOR_REGEX
        val versionNameSplit = version.split(versionNameSeparatorRegex)
        return CheckUpdate.SIMPLE_VERSION_PREFIX +
                versionNameSplit[0].replace(
                    CheckUpdate.DIGITS_AND_DOT_REGEX, ""
                )
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
//        return "2.1.0-250905.183545"

//        return "2.1.0-240905.183545"
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
//        return true
        val remoteVersionList = getVersionIntList(remoteVersion)
        val localVersionList = getVersionIntList(localVersion)

//        LogHelper.e("检查更新版本号", "远端：$remoteVersionList, 本地：$localVersionList")

        // 获取两个列表的最大长度
        val maxLength = maxOf(remoteVersionList.size, localVersionList.size)

        // 逐个比较版本号的每个部分
        for(i in 0 until maxLength) {
            // 如果一个列表已经没有元素，则默认值为 -1
            val remoteCode = if(i < remoteVersionList.size) remoteVersionList[i] else -1
            val localCode = if(i < localVersionList.size) localVersionList[i] else -1

            if(remoteCode > localCode) {
                return true
            } else if(remoteCode < localCode) {
                return false
            }
            // 如果相等，继续比较下一个部分
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
        simpleLocalVersion: String,
        simpleRemoteVersion: String,
        releaseNotes: String,
        downloadUrl: String,
        checkingUpdateToast: Toast? = null,
    ) {
        checkingUpdateToast?.cancel()

        DialogHelper.setAndShowDefaultDialog(
            context = context,
            viewModel = viewModel,
            titleText = context.getString(R.string.find_new_version),
//            messageText = context.getString(
//                R.string.latest_version,
//                remoteVersion,
//            ) + "\n\n" + context.getString(
//                R.string.whether_download_now,
//                releaseNotes.trim() + "\n\n",
//            ),
            messageTextList = listOf(
                context.getString(
                    R.string.update_path,
                    simpleLocalVersion,
                    simpleRemoteVersion,
                ) + releaseNotes + context.getString(
                    R.string.whether_download_now,
                ),
            ),
            positiveButtonText = context.getString(R.string.download_now),
            positiveButtonTextColor = context.getColor(R.color.primary),
            negativeButtonText = context.getString(R.string.download_later),
            positiveAction = {
                // 处理立即下载逻辑
                downloadUpdate(
                    context = context,
                    viewModel = viewModel,
                    downloadUrl = downloadUrl,
//                    downloadUrl = "https://mirrors.tuna.tsinghua.edu.cn/anaconda/archive/Anaconda-1.8.0-Linux-x86_64.sh",
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
        )
    }


    // 轮询查询下载状态
    private fun monitorDownloadStatus(
        context: Context,
        downloadId: Long,
        viewModel: MyViewModel,
        gapTime: Long = CheckUpdate.MONITOR_DOWNLOAD_STATUS_GAP_TIME,
    ) {

        var progressDialog: AlertDialog? = null
        var progressBar: LinearProgressIndicator? = null
        var textViewProgress: TextView? = null
//        var textViewSpeed: TextView? = null
//        var lastBytes: Long = 0
//        var lastTime: Long = System.currentTimeMillis()
        var lastProgress = 0f
        var buttonNegative: TextView?
        var buttonPositive: TextView?


        fun updateProgress(progress: Float) {

            progressBar?.progress = (progress * 100).toInt()
            textViewProgress?.text = context.getString(
                R.string.percent,
                progress,
            )

//            val speedText = if(bytesPerSecond > 0) {
//                when {
//                    bytesPerSecond >= 1024 * 1024 * 1024 -> {
//                        "%.1f GB/s".format(bytesPerSecond / (1024.0 * 1024.0 * 1024.0))
//                    }
//
//                    bytesPerSecond >= 1024 * 1024 -> {
//                        "%.1f MB/s".format(bytesPerSecond / (1024.0 * 1024.0))
//                    }
//
//                    bytesPerSecond >= 1024 -> {
//                        "%.1f KB/s".format(bytesPerSecond / 1024.0)
//                    }
//
//                    else -> {
//                        "$bytesPerSecond B/s"
//                    }
//                }
//            } else {
//                ""
//            }
//            textViewSpeed?.text = speedText

        }


        CoroutineScope(Dispatchers.Main).launch {
            val progressView =
                LayoutInflater.from(context).inflate(R.layout.layout_dialog_download, null)
            progressBar = progressView.findViewById(R.id.linearProgressIndicator)
            progressBar.max = 10000
            textViewProgress = progressView.findViewById(R.id.textView_progress)
//            textViewSpeed = progressView.findViewById(R.id.textView_speed)

            buttonNegative = progressView.findViewById(R.id.button_negative)
            buttonNegative?.text = context.getString(R.string.background_download)
            buttonNegative?.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                progressDialog?.dismiss()
                Toast.makeText(
                    context,
                    context.getString(R.string.background_continue_download),
                    Toast.LENGTH_SHORT
                ).show()
            }

            buttonPositive = progressView.findViewById(R.id.button_positive)
            buttonPositive?.text = context.getString(R.string.cancel_download)
            buttonPositive?.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                VibrationHelper.vibrateOnDangerousOperation(viewModel)
                // 显示确认对话框
                DialogHelper.setAndShowDefaultDialog(
                    context = context,
                    viewModel = viewModel,
                    messageText = context.getString(R.string.confirm_cancel_download),
                    positiveAction = {
                        // 确认取消下载
                        val downloadManager =
                            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.remove(downloadId)

                        // 关闭对话框
                        progressDialog?.dismiss()

                        // 显示取消提示
                        Toast.makeText(
                            context,
                            context.getString(R.string.download_cancelled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            updateProgress(lastProgress)

            progressDialog = DialogHelper.getCustomizeDialog(
                context = context,
                view = progressView,
            )
            progressDialog.showMyDialog(viewModel)
        }


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
                            progressDialog?.dismiss()
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
                            withContext(Dispatchers.Main) {
                                progressDialog?.dismiss()
                            }
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
                                progressDialog?.dismiss()
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.download_failed),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                            cursor.close()
                            break // 停止轮询
                        }

                        DownloadManager.STATUS_RUNNING -> {
                            // 获取下载进度
                            val sofarIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalIndex =
                                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                            if(sofarIndex != -1 && totalIndex != -1) {
                                val sofar = cursor.getLong(sofarIndex)
                                val total = cursor.getLong(totalIndex)

                                if(total > 0) {
                                    val progress = (sofar * 100f) / total
//                                    val currentTime = System.currentTimeMillis()
//
//                                    // 计算下载速度
//                                    val bytesDiff = sofar - lastBytes
//                                    val timeDiff = currentTime - lastTime
//                                    val bytesPerSecond = if(timeDiff >= 10) { // 至少100毫秒才计算速度
//                                        (bytesDiff * 1000) / timeDiff
//                                    } else {
//                                        0
//                                    }
//
//                                    lastBytes = sofar
//                                    lastTime = currentTime

                                    // 只有当进度变化时才更新UI
                                    if(progress != lastProgress) {
                                        lastProgress = progress
                                        withContext(Dispatchers.Main) {
                                            updateProgress(progress)
                                        }
                                    }
                                }
                            }
                        }

                        DownloadManager.STATUS_PAUSED -> {
                            // 下载暂停，可选择继续轮询
                        }
                    }
                }
                cursor?.close()
                // 延迟一段时间再继续轮询
                delay(gapTime) // 添加延迟
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
                viewModel.updateConfigValue(
                    key = ConfigKeys.DOWNLOAD_ID,
                    value = downloadId,
                )
                viewModel.saveUserConfig()

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
                viewModel.deleteConfigValue(
                    key = ConfigKeys.DOWNLOAD_ID,
                )
                viewModel.saveUserConfig()

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


    // 获取更新日志
    private fun getUpdateLog(context: Context): String {
        return try {
            val url = URL(context.getString(R.string.update_log_content_url))
//            val url = URL("https://gitee.com/minimote/toolbox/raw/master/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97.md")

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = CheckUpdate.TIMEOUT
            connection.readTimeout = CheckUpdate.TIMEOUT

            if(connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                ""
            }
        } catch(e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun splitUpdateLogByVersions(updateLog: String): List<Pair<String, String>> {
        val versionAndLog = mutableListOf<Pair<String, String>>()

        // 匹配版本标题的正则表达式
        val versionRegex = CheckUpdate.VERSION_NAME_REGEX

        // 查找所有匹配项
        val matches = versionRegex.findAll(updateLog)
        val matchList = matches.toList()

        // 遍历每个匹配项，提取版本号和内容
        for(i in matchList.indices) {
            val currentMatch = matchList[i]
            val versionName = currentMatch.groupValues[1]
//            val simpleVersion = getSimpleVersion(versionName)

            // 获取当前版本内容的起始位置
            val contentStart = currentMatch.range.last + 1

            // 获取下一个版本的起始位置，如果没有下一个版本，则到字符串末尾
            val contentEnd = if(i < matchList.size - 1) {
                matchList[i + 1].range.first
            } else {
                updateLog.length
            }

            // 提取内容并去除首尾空白
            val versionContent = updateLog.substring(contentStart, contentEnd).trim()

            versionAndLog.add(Pair(versionName, versionContent))
        }

        return versionAndLog
    }


    private fun getUpdateLogsBetweenVersions(
        context: Context,
        simpleLocalVersion: String,
        simpleRemoteVersion: String,
    ): String {
        val updateLog = getUpdateLog(context)
        val versionAndLogList = splitUpdateLogByVersions(updateLog)

        var releaseLog = ""

        val gapCharInVersionAndLog = "\n"
        val gapCharInLog = "\n\n"

        var started = false

//        LogHelper.e("两个版本号", "本地：<${simpleLocalVersion}>,远端：<${simpleRemoteVersion}>")
        for((versionString, logString) in versionAndLogList) {
//            LogHelper.e("versionString: $versionString", "")
            // 当前版本是远程版本，开始收集日志
            if(simpleRemoteVersion in versionString) {
                started = true
            }

            // 当前版本是本地版本，结束循环
            if(simpleLocalVersion in versionString) {
                // 本地和远程的简单版本号相同，则显示一段日志再退出
                if(simpleLocalVersion == simpleRemoteVersion) {
                    releaseLog += "$versionString$gapCharInVersionAndLog$logString$gapCharInLog"
                }
                break
            }

            // 如果已经开始收集，并且当前版本不是本地版本，则添加到日志中
            if(started) {
                releaseLog += "$versionString$gapCharInVersionAndLog$logString$gapCharInLog"
            }
        }

        return releaseLog

    }

}