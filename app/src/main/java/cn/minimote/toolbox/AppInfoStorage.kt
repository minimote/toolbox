/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

object AppInfoStorage {

    private const val FILE_NAME = "app_info_storage.json"

    fun saveAppSet(context: Context, appSet: Set<StorageAppInfo>) {
        val gson = Gson()
        val json = gson.toJson(appSet)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(json)
    }

    fun loadAppSet(context: Context): Set<StorageAppInfo> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            return emptySet()
        }
        try {
            val json = file.readText()
            val gson = Gson()
            val type: Type = object : TypeToken<Set<StorageAppInfo>>() {}.type
            return gson.fromJson(json, type)
        } catch (e: JsonSyntaxException) {
            // 捕获 JSON 解析错误
            e.printStackTrace()
            deleteFile(context)
        } catch (e: Exception) {
            // 捕获其他可能的异常
            e.printStackTrace()
            deleteFile(context)
        }
        return emptySet()
    }

    // 删除有问题的文件
    private fun deleteFile(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
//            println("Deleted corrupted file: $FILE_NAME")
        }
    }

    // 获取 activityName 到 StorageAppInfo 的映射
    fun getMapActivityNameToStorageAppInfo(storageAppSet: MutableSet<StorageAppInfo>): MutableMap<String, StorageAppInfo> {
        val activityNameToStorageAppInfoMap = mutableMapOf<String, StorageAppInfo>()
        for (storageAppInfo in storageAppSet) {
            activityNameToStorageAppInfoMap[storageAppInfo.activityName] = storageAppInfo
        }
        return activityNameToStorageAppInfoMap
    }
}
