/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.util.Log
import cn.minimote.toolbox.constant.Log.LOG_ENABLE
import cn.minimote.toolbox.constant.Log.LOG_FILE_NAME
import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStreamReader


object LogHelper {


    private var isLogging = false


    fun startDebugLogCapture(viewModel: MyViewModel) {
        if(LOG_ENABLE && !viewModel.isWatch && !isLogging) {
            isLogging = true
            Thread {
                try {
                    val process = Runtime.getRuntime().exec("logcat -v time *:E")

                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val logFile = File(viewModel.dataPath, LOG_FILE_NAME).apply {
                        // 确保父目录存在
                        parentFile?.takeIf { !it.exists() }?.mkdirs()
                        if(!exists()) {
                            createNewFile()
                        }
                    }
                    e("启动日志抓取", "${logFile.absolutePath}")

                    // 启动时先清理过期内容
                    cleanExpiredLogContent(logFile)

                    FileWriter(logFile, true).use { writer ->
                        var line: String?
                        while(reader.readLine().also { line = it } != null && isLogging) {
                            if(shouldKeepLine(line)) {
                                writer.write(line + "\n\n")
                                writer.flush()
                            }
                        }
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLogging = false
                }
            }.start()
        }
    }


    private fun shouldKeepLine(line: String?): Boolean {
    val filterKeywords = listOf("Input_Track", "Oplus", "gms.fonts", "IGraphicBufferProducer")

    return line?.let {
        !filterKeywords.any { keyword -> it.contains(keyword) }
    } ?: false
}



    private fun cleanExpiredLogContent(logFile: File) {
        // 检查文件是否存在
        if(!logFile.exists()) {
            return // 文件不存在则直接返回，无需清理
        }

        val lines = mutableListOf<String>()

        // 读取现有日志
        BufferedReader(FileReader(logFile)).use { reader ->
            var line: String?
            while(reader.readLine().also { line = it } != null) {
                lines.add(line!!)
            }
        }

        // 过滤24小时内的日志
        val recentLines = lines.filter { line ->
            // 解析时间戳逻辑（根据logcat格式）
            isWithin24Hours(line)
        }

        // 重写文件
        FileWriter(logFile, false).use { writer ->
            recentLines.forEach { line ->
                writer.write(line + "\n")
            }
        }
    }


    private fun isWithin24Hours(line: String): Boolean {
        // 实际 logcat 时间格式为 "MM-dd HH:mm:ss.SSS"
        try {
            val currentTime = System.currentTimeMillis()

            // 提取时间部分（格式为 "MM-dd HH:mm:ss.SSS"）
            val timePart = line.substringBefore(" ").trim()
            if(timePart.isEmpty() ||
                !timePart.contains("-") ||
                !timePart.contains(":")
            )
                return true // 如果没有标准时间戳，则保留该行日志

            // 获取当前年份并拼接完整的时间字符串
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val fullTimeStr = "$currentYear-$timePart"

            // 定义日期格式
            val dateFormat =
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault())
            val logTime = dateFormat.parse(fullTimeStr)?.time ?: return true

            // 判断是否在24小时内
            return (currentTime - logTime) < 24 * 60 * 60 * 1000
        } catch(e: Exception) {
            e.printStackTrace()
            return true // 解析失败保留日志
        }
    }


    fun stopDebugLogCapture() {
        isLogging = false
    }


    fun readLogFile(viewModel: MyViewModel): String {
        return try {
            val logFile = File(viewModel.dataPath, LOG_FILE_NAME)
            if(logFile.exists()) {
                BufferedReader(FileReader(logFile)).use { reader ->
                    reader.readText()
                }
            } else {
                ""
            }
        } catch(e: Exception) {
            e.printStackTrace()
            ""
        } + "\n\n"
    }


    fun e(tag: String?, msg: String?, tr: Throwable? = null) {
        if(LOG_ENABLE) {
            Log.e(tag, msg, tr)
        }
    }


    fun d(tag: String?, msg: String) {
        if(LOG_ENABLE) {
            Log.d(tag, msg)
        }
    }
}