/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.os.Environment
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigFileName
import cn.minimote.toolbox.constant.Icon
import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.File

object PathHelper {

    // 保存路径
    fun getSavePath(context: Context): File {
        return File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ),
            context.getString(R.string.app_name_en),
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    }


    // 数据路径
    fun getDataPath(context: Context): File {
        return File(
            context.filesDir,
            context.getString(R.string.app_name_en),
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    }


    // 缓存路径
    fun getCachePath(context: Context): File {
        return File(
            context.cacheDir,
            context.getString(R.string.app_name_en),
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    }


    // 图标缓存路径
    fun getIconCachePath(context: Context): File {
        return File(
            getCachePath(context),
            Icon.ICON_CACHE_PATH,
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    }


    // 获取配置路径
    fun getConfigPath(context: Context): File {
        return File(
            getDataPath(context),
            Config.FOLDER_NAME,
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    }


    // 获取用户配置路径
    fun MyViewModel.getUserConfigFile(): File {
        return File(
            this.configPath,
            ConfigFileName.USER_CONFIG
        ).apply {
            if(!exists()) {
                createNewFile()
            }
        }
    }


    // 获取动态快捷方式ID列表文件
    fun MyViewModel.getDynamicShortcutIdListFile(): File {
        return File(
            this.configPath,
            ConfigFileName.DYNAMIC_SHORTCUT_ID_LIST
        ).apply {
            if(!exists()) {
                createNewFile()
            }
        }
    }


    fun MyViewModel.getSearchHistoryFile(): File {
        return File(
            this.configPath,
            ConfigFileName.SEARCH_HISTORY
        ).apply {
            if(!exists()) {
                createNewFile()
            }
        }
    }


    fun MyViewModel.getSearchSuggestionFile(): File {
        return File(
            this.configPath,
            ConfigFileName.SEARCH_SUGGESTION
        ).apply {
            if(!exists()) {
                createNewFile()
            }
        }
    }


    fun MyViewModel.getOtherConfigFile(configName: String): File {
        return File(
            this.configPath,
            configName,
        ).apply {
            if(!exists()) {
                createNewFile()
            }
        }
    }

}