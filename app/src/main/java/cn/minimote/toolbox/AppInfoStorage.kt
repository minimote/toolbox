/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.content.Context
import com.google.gson.Gson
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
        val json = file.readText()
        val gson = Gson()
        val type: Type = object : TypeToken<Set<StorageAppInfo>>() {}.type
        return gson.fromJson(json, type)
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

