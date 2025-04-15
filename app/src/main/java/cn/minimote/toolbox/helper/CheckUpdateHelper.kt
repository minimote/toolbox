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
import androidx.core.net.toUri
import cn.minimote.toolbox.R
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigKeys
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.ConfigValues.CheckUpdateFrequency
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

    private const val TIMEOUT = 5000

    // 自动检查更新
    fun autoCheckUpdate(
        context: Context,
        viewModel: ToolboxViewModel,
    ) {
        // 不自动检查更新
        if(viewModel.updateCheckGap == -1L) {
            return
        }
        if(System.currentTimeMillis() >= viewModel.nextUpdateCheckTime) {
//            Toast.makeText(
//                context,
//                getFormatTimeString(System.currentTimeMillis()) + "\n" + getFormatTimeString(
//                    viewModel.nextUpdateCheckTime.value!!
//                ),
//                Toast.LENGTH_SHORT
//            ).show()
            checkUpdate(
                context = context,
                viewModel = viewModel,
                silence = true,
            )
        }
    }


    // 更新上次检查时间和下次检查时间
    private fun updateCheckTime(
        viewModel: ToolboxViewModel,
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
    fun getUpdateCheckGap(
        frequency: String,
    ): Long {
        return when(frequency) {
            CheckUpdateFrequency.DAILY -> 1 * 24 * 60 * 60 * 1000L
            CheckUpdateFrequency.WEEKLY -> 7 * 24 * 60 * 60 * 1000L
            CheckUpdateFrequency.MONTHLY -> 30 * 24 * 60 * 60 * 1000L
            CheckUpdateFrequency.NEVER -> -1L

            else -> {
                throw IllegalArgumentException("非法的频率：$frequency")
            }
        }
    }


    // 检查更新
    fun checkUpdate(
        context: Context,
        viewModel: ToolboxViewModel,
        checkingUpdateToast: Toast? = null,
        silence: Boolean = false, // 静默检查
    ) {
        updateCheckTime(viewModel)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val giteeApiUrl = "https://gitee.com/api/v5/repos/{owner}/{repo}/releases/latest"
                val url = URL(
                    giteeApiUrl.replace(
                        "{owner}", context.getString(R.string.author_name_en)
                    ).replace("{repo}", context.getString(R.string.app_name_en))
                )
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT

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
                val releaseNotes = jsonObject.getString("body")

                val remoteVersion = getRemoteVersion(jsonObject)
                val localVersion = getLocalVersion(viewModel)

                if(hasNewVersion(remoteVersion, localVersion)) {
                    val downloadUrl = getDownloadUrl(jsonObject)
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
        val assets = jsonObject.getJSONArray("assets")
        for(i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val fileName = asset.getString("name")
            if(fileName.endsWith(".apk")) {
                // toolbox_1.1.0-250415.110640
                try {
                    return fileName.removeSuffix(".apk").split("_")[1]
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return ""
    }


    // 获取本地版本号
    private fun getLocalVersion(
        viewModel: ToolboxViewModel,
    ): String {
        return viewModel.myVersionName.removeSuffix("-debug")
    }


    // 获取版本号的整数列表
    private fun getVersionIntList(
        versionString: String,
    ): List<Int> {
        // 1.1.0-250415.110640-debug
        val newVersionString = versionString.replace("[\\s_-]".toRegex(), ".")

//        Log.e("获取版本号的整数列表", "versionString: $newVersionString")
        val versionIntList = newVersionString.split(".").map {
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
            }
            if(remoteCode < localCode) {
                return false
            }
        }
        return false
    }


    // 获取下载链接
    private fun getDownloadUrl(
        jsonObject: JSONObject,
    ): String {
        val assets = jsonObject.getJSONArray("assets")
        for(i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            if(asset.getString("name").endsWith(".apk")) {
                return asset.getString("browser_download_url")
            }
        }
        return ""
    }


    // 显示更新确认对话框
    private fun showUpdateDialog(
        context: Context,
        viewModel: ToolboxViewModel,
        remoteVersion: String,
        releaseNotes: String,
        downloadUrl: String,
        checkingUpdateToast: Toast? = null,
    ) {
        checkingUpdateToast?.cancel()

        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_update_note, null)
        builder.setView(dialogView)

        val textViewTitle = dialogView.findViewById<TextView>(R.id.textViewTitle)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.textViewMessage)

        textViewTitle.text = context.getString(
            R.string.new_version_available,
            remoteVersion,
        )
        textViewMessage.text = context.getString(
            R.string.whether_download_now,
            releaseNotes + "\n",
        )

        builder.setPositiveButton(context.getString(R.string.download_now)) { dialog, _ ->
            VibrationHelper.vibrateOnClick(context, viewModel)
            // 处理立即下载逻辑
            downloadUpdate(
                context = context,
                viewModel = viewModel,
                downloadUrl = downloadUrl,
            )
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(R.string.download_later)) { dialog, _ ->
            VibrationHelper.vibrateOnClick(context, viewModel)
            dialog.dismiss()
        }
        builder.show()
    }


    // 下载更新
    private fun downloadUpdate(
        context: Context,
        viewModel: ToolboxViewModel,
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
            downloadingToast = downloadingToast,
        )
    }


    // 轮询查询下载状态
    private fun monitorDownloadStatus(
        context: Context,
        downloadId: Long,
        delayTime: Long = 1000L,
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
                                downloadId = downloadId
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
                delay(delayTime)
            }
        }
    }


    // 调用系统安装器
    private fun installApk(
        context: Context?,
        downloadId: Long,
    ) {
//        Toast.makeText(
//            context, "准备安装", Toast.LENGTH_SHORT
//        ).show()
        context?.let { ctx ->
            val downloadManager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = downloadManager.getUriForDownloadedFile(downloadId)
            if(uri != null) {
                if(!ctx.packageManager.canRequestPackageInstalls()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(ctx, "请授予安装权限", Toast.LENGTH_SHORT).show()
                    }
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    intent.data = "package:${ctx.packageName}".toUri()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    ctx.startActivity(intent)
                }
                if(ctx.packageManager.canRequestPackageInstalls()) {
                    startInstallIntent(ctx, uri)
                }
            }
        }
    }

//    // 将应用带到前台
//    private fun bringAppToForeground(context: Context) {
//        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
//        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        context.startActivity(launchIntent)
//    }


    private fun startInstallIntent(ctx: Context, uri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
        installIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        ctx.startActivity(installIntent)
    }


}