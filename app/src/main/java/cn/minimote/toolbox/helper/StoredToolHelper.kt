/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper


import android.widget.Toast
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config.ENCODING
import cn.minimote.toolbox.constant.ToolConstants.STORED_FILE_NAME
import cn.minimote.toolbox.constant.Version
import cn.minimote.toolbox.dataClass.StoredActivityContainer
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type


object StoredToolHelper {

    private val gson = Gson()

    // 获取存储文件对象
    private fun getStoredFile(viewModel: MyViewModel): File {
        return File(viewModel.dataPath, STORED_FILE_NAME)
    }


    // 保存活动列表
    fun saveStoredActivityList(
        viewModel: MyViewModel,
        storedToolList: MutableList<StoredTool>
    ) {
        if(storedToolList.isNotEmpty()) {
            synchronized(this) {
                val storedActivityContainer =
                    StoredActivityContainer(
                        version = Version.STORED_ACTIVITY,
                        data = storedToolList,
                    )
                val json = gson.toJson(storedActivityContainer)
                val file = getStoredFile(viewModel)
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(json.toByteArray(ENCODING))
                }
            }
        } else {
            deleteStorageFile(viewModel)
        }
    }


    // 加载存储列表
    fun loadStoredToolList(viewModel: MyViewModel): MutableList<StoredTool> {
        val file = getStoredFile(viewModel)
//        LogHelper.e("loadStoredToolList", "读取文件: ${file.absolutePath}")

        if(!file.exists()) {
//            LogHelper.e("loadStoredToolList", "文件不存在: ${file.absolutePath}")
            return mutableListOf()
        }
//        LogHelper.e("loadStoredToolList", "文件存在: ${file.absolutePath}")

        return synchronized(this) {
            try {
                val json = file.readText()
                if(json.isBlank()) {
                    // 文件存在但内容为空，视为无效文件
//                    Log.e("ActivityStorageHelper", "文件内容为空")
                    deleteStorageFile(viewModel)
                    return mutableListOf()
                }

                val type: Type = object : TypeToken<StoredActivityContainer>() {}.type
                val storedActivityContainer: StoredActivityContainer = gson.fromJson(json, type)

                // 如果列表为空或者版本不一致，则删除文件
                if(storedActivityContainer.data.isEmpty() || storedActivityContainer.version != Version.STORED_ACTIVITY) {
                    deleteStorageFile(viewModel)
                    return mutableListOf()
                }
                storedActivityContainer.data
            } catch(e: Exception) {
//                Log.e("ActivityStorageHelper", "读取文件出错: ${e.message}", e)
                e.printStackTrace()
                deleteStorageFile(viewModel=viewModel, silence = false)
                mutableListOf()
            }
        }
    }


    // 删除有问题的文件
    private fun deleteStorageFile(
        viewModel: MyViewModel,
        silence: Boolean = true, // 是否静默
    ) {
        val context = viewModel.myContext
        // Log.e("ActivityStorageHelper", "删除文件")
        if(!silence) {
            Toast.makeText(
                context, context.getString(R.string.data_error_and_delete_file),
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        val file = getStoredFile(viewModel)
        if(file.exists()) {
            file.delete()
        }
    }
}
