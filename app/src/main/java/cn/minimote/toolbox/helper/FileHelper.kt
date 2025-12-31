/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config.ENCODING
import cn.minimote.toolbox.constant.File.FileOperationResult
import cn.minimote.toolbox.helper.ConfigHelper.loadAllConfig
import cn.minimote.toolbox.helper.TypeConversionHelper.toMutableList
import cn.minimote.toolbox.helper.TypeConversionHelper.toMutableMap
import cn.minimote.toolbox.viewModel.MyViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader


object FileHelper {

    /**
     * 导入文件：调起文件选择器，选中文件并保存到指定位置
     *
     * @param myActivity 上下文活动
     * @param destinationFile 目标文件
     * @param mimeTypes 支持的MIME类型数组，默认为所有类型
     * @param onResult 结果回调 (状态码, 目标文件路径)
     */
    fun importFile(
        myActivity: MainActivity,
        viewModel: MyViewModel,
        destinationFile: File,
        mimeTypes: Array<String> = arrayOf(
            getMimeType(destinationFile.absolutePath)
        ),
        onResult: (Int) -> Unit = { state ->
            val message = when(state) {
                FileOperationResult.SUCCESS -> {
                    myActivity.getString(R.string.import_success)
                }

                FileOperationResult.CANCEL -> {
                    myActivity.getString(R.string.import_cancel)
                }

                FileOperationResult.FAIL -> {
                    myActivity.getString(R.string.import_failed)
                }

                else -> {
                    myActivity.getString(R.string.unknown_error)
                }
            }

            Toast.makeText(
                myActivity,
                message,
                Toast.LENGTH_SHORT,
            ).show()

            viewModel.loadStorageActivities()
            viewModel.loadAllConfig()
        },
    ) {

        myActivity.importFile(
            destinationFile = destinationFile,
            mimeTypes = mimeTypes,
            onResult = onResult,
        )

    }


    /**
     * 导出文件：调起文件夹选择器，将指定文件导出到指定目录
     *
     * @param myActivity 上下文活动
     * @param sourceFile 源文件
     * @param onResult 结果回调 (成功: true/false, 文件Uri)
     */
    fun exportFile(
        myActivity: MainActivity,
        sourceFile: File,
        onResult: (Int, Uri?) -> Unit = { state, uri ->
            val message = when(state) {
                FileOperationResult.SUCCESS -> {
                    val fileName = getFileNameFromUri(myActivity, uri)
                    if(fileName != null && fileName.isNotEmpty()) {
                        myActivity.getString(
                            R.string.export_success_with_filename,
                            fileName
                        )
                    } else {
                        myActivity.getString(R.string.export_success)
                    }
                }

                FileOperationResult.CANCEL -> {
                    myActivity.getString(R.string.export_cancel)
                }

                FileOperationResult.FAIL -> {
                    myActivity.getString(R.string.export_failed)
                }

                else -> {
                    myActivity.getString(R.string.unknown_error)
                }
            }


            Toast.makeText(
                myActivity,
                message,
                Toast.LENGTH_SHORT,
            ).show()
        },
    ) {

        myActivity.exportFile(
            sourceFile = sourceFile,
            onResult = onResult,
        )

    }


    /**
     * 删除文件或文件夹
     *
     * @param file 要删除的文件或文件夹
     * @return 是否删除成功
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if(file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    if(!deleteFile(child)) {
                        return false
                    }
                }
            }
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // 复制文件
    fun copyFile(context: Context, sourceUri: Uri, destinationFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    // 获取文件 MIME 类型
    fun getMimeType(filePath: String): String {
        val extension = filePath.substringAfterLast(".", "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }


    fun getFileNameFromUri(context: Context, uri: Uri?): String? {
        if(uri == null) {
            return null
        }

        return try {
            var result: String? = null
            if(uri.scheme.equals("content")) {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if(it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if(nameIndex != -1) {
                            result = it.getString(nameIndex)
                        }
                    }
                }
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun getAssetsString(
        context: Context,
        fileName: String,
    ): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.use { it.readText() }
        } catch(e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    // 读取 json 文件
    fun readJsonFileAsJSONString(file: File): String? {
        return try {
            val inputStream = FileInputStream(file)

            inputStream.use { input ->
                val bytes = input.readBytes()
                String(bytes, ENCODING)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun readJsonFileAsJSONObject(file: File): JSONObject? {
        return try {
            val jsonString = readJsonFileAsJSONString(file)
            if (jsonString != null) {
                JSONObject(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readJsonFileAsMutableMap(file: File): MutableMap<String, Any>? {
        return try {
            val jsonObject = readJsonFileAsJSONObject(file)
            jsonObject?.toMutableMap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun readJsonFileAsJSONArray(file: File): JSONArray? {
        return try {
            val jsonString = readJsonFileAsJSONString(file)
            if (jsonString != null) {
                JSONArray(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readJsonFileAsMutableList(file: File): MutableList<*>? {
        return try {
            val jsonArray = readJsonFileAsJSONArray(file)
            jsonArray?.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun readJsonFileAsMutableStringList(file: File): MutableList<String>? {
        return try {
            val list = readJsonFileAsMutableList(file)
            list?.filterIsInstance<String>()?.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun writeDataToJsonFile(data: Any, file: File): Boolean {
        return try {
            val jsonString = when(data) {
                is MutableMap<*, *> -> JSONObject(data)
                is Map<*, *> -> JSONObject(data)
                is MutableList<*> -> JSONArray(data)
                is List<*> -> JSONArray(data)
                is Array<*> -> JSONArray(data)
                else -> {
                    throw Exception("写入json文件遇到类型错误${data::class.simpleName}")
                }
            }.toString()

            FileOutputStream(file).use { outputStream ->
                outputStream.write(jsonString.toByteArray(ENCODING))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
