/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.File
import java.util.Locale


object DataCleanHelper {

    // 获取缓存路径列表
    private fun getCachePathList(viewModel: MyViewModel): List<File?> {
        return listOf(
            viewModel.cachePath
        )
    }
    // 获取缓存大小
    fun getCacheSize(viewModel: MyViewModel): String {
        val pathList = getCachePathList(viewModel)
        var size = 0L
        for(path in pathList) {
            if(path != null) {
                size += getDirectorySize(path)
            }
        }
//        Log.e("缓存大小：${formatSize(size)}", "缓存目录：$pathList")
        return formatSize(size)
    }
    // 清除缓存
    fun clearCache(viewModel: MyViewModel) {
        val pathList = getCachePathList(viewModel)
        for(path in pathList) {
            if(path != null) {
                deleteDirectory(path)
            }
        }
    }


    // 获取数据路径列表
    private fun getDataPathList(viewModel: MyViewModel): List<File?> {
        return listOf(
            viewModel.dataPath,
            viewModel.savePath,
        )
    }
    // 获取数据大小
    fun getDataSize(viewModel: MyViewModel): String {
        val pathList = getDataPathList(viewModel)
        var size = 0L
        for(path in pathList) {
            if(path != null) {
                size += getDirectorySize(path)
            }
        }
//        Log.e("数据大小：${formatSize(size)}", "dataDir: $pathList")
        return formatSize(size)
    }
    // 清除数据
    fun clearData(
        viewModel: MyViewModel,
    ) {
        val pathList = getDataPathList(viewModel)
        for(path in pathList) {
            if(path != null) {
                deleteDirectory(path)
            }
        }
        // 重新加载数据
        viewModel.loadStorageActivities()
        // 清除配置文件
        ConfigHelper.clearUserAndBackupConfig(viewModel = viewModel)
    }


    // 获取目录大小
    private fun getDirectorySize(directory: File): Long {
        var size: Long = 0
        val files = directory.listFiles()
        if(files != null) {
            for(file in files) {
                size += if(file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }


    // 格式化数据大小
    private fun formatSize(size: Long): String {
        val kilobyte = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024L
        return when {
            size >= terabyte -> String.format(Locale.CHINA, "%.1f TB", size.toDouble() / terabyte)
            size >= gigabyte -> String.format(Locale.CHINA, "%.1f GB", size.toDouble() / gigabyte)
            size >= megabyte -> String.format(Locale.CHINA, "%.1f MB", size.toDouble() / megabyte)
            size >= kilobyte -> String.format(Locale.CHINA, "%.1f KB", size.toDouble() / kilobyte)
            else -> "$size B"
        }
    }


    // 删除文件夹
    private fun deleteDirectory(directory: File) {
        if(directory.exists()) {
            val files = directory.listFiles()
            if(files != null) {
                for(file in files) {
                    if(file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
            directory.delete()
        }
    }
}