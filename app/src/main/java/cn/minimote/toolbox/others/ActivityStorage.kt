/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.others


import android.content.Context
import cn.minimote.toolbox.data_class.StoredActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type


object ActivityStorage {

    private const val FILE_NAME = "app_info_storage.json"

    fun saveActivityList(context: Context, storedActivityList: MutableList<StoredActivity>) {
        val gson = Gson()
        val json = gson.toJson(storedActivityList)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(json)
    }

    fun loadActivityList(context: Context): MutableList<StoredActivity> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            return mutableListOf()
        }
        try {
            val json = file.readText()
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<StoredActivity>>() {}.type
            return gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            deleteFile(context)
        } catch (e: Exception) {
            e.printStackTrace()
            deleteFile(context)
        }
        return mutableListOf()
    }

    // 删除有问题的文件
    private fun deleteFile(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
