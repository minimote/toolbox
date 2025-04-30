/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


import android.content.Context
import cn.minimote.toolbox.constant.StoredActivity.STORED_FILE_NAME
import cn.minimote.toolbox.dataClass.StoredActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type


object StoredActivityHelper {

    var FILE_NAME = STORED_FILE_NAME

    private val gson = Gson()

    // 获取存储文件对象
    private fun getStorageFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }


    // 保存活动列表
    fun saveStoredActivityList(
        context: Context,
        storedActivityList: MutableList<StoredActivity>
    ) {
        synchronized(this) {
            val json = gson.toJson(storedActivityList)
            val file = getStorageFile(context)
//            Log.i("ActivityStorageHelper", "保存文件: ${file.absolutePath}")
            file.writeText(json)
        }
    }


    // 加载活动列表
    fun loadStoredActivityList(context: Context): MutableList<StoredActivity> {
        val file = getStorageFile(context)
//        Log.i("ActivityStorageHelper", "读取文件: ${file.absolutePath}")

        if(!file.exists()) {
            return mutableListOf()
        }

        return synchronized(this) {
            try {
                val json = file.readText()
                if(json.isBlank()) {
                    // 文件存在但内容为空，视为无效文件
//                    Log.e("ActivityStorageHelper", "文件内容为空")
                    deleteFile(context)
                    return mutableListOf()
                }

                val type: Type = object : TypeToken<MutableList<StoredActivity>>() {}.type
                gson.fromJson<MutableList<StoredActivity>>(json, type) ?: mutableListOf()
            } catch(e: Exception) {
//                Log.e("ActivityStorageHelper", "读取文件出错: ${e.message}", e)
                e.printStackTrace()
                deleteFile(context)
                mutableListOf()
            }
        }
    }

    // 删除有问题的文件
    private fun deleteFile(context: Context) {
        // Log.e("ActivityStorageHelper", "删除文件")
        val file = getStorageFile(context)
        if(file.exists()) {
            file.delete()
        }
    }
}
