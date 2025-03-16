/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


import android.content.Context
import cn.minimote.toolbox.dataClass.InstalledActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


object InstalledActivityHelper {

    private val ENCODING: Charset = StandardCharsets.UTF_8


    // 根据设备类型获取文件名
    private fun getFileName(deviceType: String): String {
        return "recommended_activities/recommended_activities_${deviceType}.jsonc"
    }


    private fun loadJsonFromAssets(
        context: Context,
        fileName: String,
    ): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, ENCODING)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun saveInstalledActivityList(
        context: Context,
        installedActivityList: List<InstalledActivity>,
        deviceType: String,
    ) {
        val gson = Gson()
        val json = gson.toJson(installedActivityList)
        val fileName = getFileName(deviceType)
        val file = File(context.filesDir, fileName)
//        Log.i("ActivityStorageHelper", "保存文件: ${file.absolutePath}")
        file.writeText(json)
    }


    fun loadInstalledActivityList(
        context: Context,
        deviceType: String,
    ): MutableList<InstalledActivity> {
        val fileName = getFileName(deviceType)
        val json = loadJsonFromAssets(context, fileName)
        var installedActivityList: MutableList<InstalledActivity> = mutableListOf()
        if(json != null) {
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<InstalledActivity>>() {}.type
            installedActivityList = gson.fromJson(json, type)
        }
        return installedActivityList
    }
}
