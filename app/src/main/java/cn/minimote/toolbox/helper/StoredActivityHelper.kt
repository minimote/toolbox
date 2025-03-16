/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


import android.content.Context
import cn.minimote.toolbox.dataClass.StoredActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type


object StoredActivityHelper {

    private const val FILE_NAME = "app_info_storage.json"

    fun saveStoredActivityList(
        context: Context,
        storedActivityList: MutableList<StoredActivity>
    ) {
        val gson = Gson()
        val json = gson.toJson(storedActivityList)
        val file = File(context.filesDir, FILE_NAME)
//        Log.i("ActivityStorageHelper", "保存文件: ${file.absolutePath}")
        file.writeText(json)
    }

    fun loadStoredActivityList(context: Context): MutableList<StoredActivity> {
        val file = File(context.filesDir, FILE_NAME)
//        Log.i("ActivityStorageHelper", "读取文件: ${file.absolutePath}")
        if(!file.exists()) {
            return mutableListOf()
        }
        try {
            val json = file.readText()
            val gson = Gson()
            val type: Type = object : TypeToken<MutableList<StoredActivity>>() {}.type
            return gson.fromJson(json, type) ?: mutableListOf()
        } catch(e: JsonSyntaxException) {
            // Log.e("ActivityStorageHelper", "读取文件出错:${e}")
            e.printStackTrace()
            deleteFile(context)
        } catch(e: Exception) {
            // Log.e("ActivityStorageHelper", "读取文件出错:${e}")
            e.printStackTrace()
            deleteFile(context)
        }
        return mutableListOf()
    }

    // 删除有问题的文件
    private fun deleteFile(context: Context) {
        // Log.e("ActivityStorageHelper", "删除文件")
        val file = File(context.filesDir, FILE_NAME)
        if(file.exists()) {
            file.delete()
        }
    }
}
