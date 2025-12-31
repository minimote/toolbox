/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.helper.PathHelper.getOtherConfigFile
import cn.minimote.toolbox.viewModel.MyViewModel


object OtherConfigHelper {


    // 加载配置-----------------------------------------
    fun MyViewModel.loadOtherConfig(configName: String) {
        when(configName) {
            Config.ConfigFileName.UI_STATE -> {
                this.uiStateConfig = FileHelper.readJsonFileAsMutableMap(
                    getOtherConfigFile(configName)
                ) ?: mutableMapOf()
            }

            Config.ConfigFileName.SEARCH_HISTORY -> {
                this.searchHistoryConfig = FileHelper.readJsonFileAsMutableMap(
                    getOtherConfigFile(configName)
                ) ?: mutableMapOf()
            }

            Config.ConfigFileName.SEARCH_SUGGESTION -> {
                this.searchSuggestionsConfig = FileHelper.readJsonFileAsMutableMap(
                    getOtherConfigFile(configName)
                ) ?: mutableMapOf()
            }

            else -> {}
        }
    }


    // 保存配置-----------------------------------------
    private fun MyViewModel.saveOtherConfig(
        configName: String,
    ): Boolean {
        try {
            val file = getOtherConfigFile(configName)
            val map = getOtherConfigMap(configName)
            // 尝试访问属性，如果未初始化会抛出 UninitializedPropertyAccessException
            if(map.isEmpty()) {
                file.delete()
                return true
            }
            FileHelper.writeDataToJsonFile(
                data = map,
                file = file,
            )
            return true
        } catch(_: UninitializedPropertyAccessException) {
            return false
        }
    }

    fun MyViewModel.saveUiStateConfig(): Boolean {
        return saveOtherConfig(Config.ConfigFileName.UI_STATE)
    }

    fun MyViewModel.saveSearchHistoryConfig(): Boolean {
        return saveOtherConfig(Config.ConfigFileName.SEARCH_HISTORY)
    }

    fun MyViewModel.saveSearchSuggestionConfig(): Boolean {
        return saveOtherConfig(Config.ConfigFileName.SEARCH_SUGGESTION)
    }


    // 获取配置的值-----------------------------------------
    private fun MyViewModel.getOtherConfigMap(
        configName: String,
    ): MutableMap<String, Any> {
        return when(configName) {
            Config.ConfigFileName.UI_STATE -> this.uiStateConfig
            Config.ConfigFileName.SEARCH_HISTORY -> this.searchHistoryConfig
            Config.ConfigFileName.SEARCH_SUGGESTION -> this.searchSuggestionsConfig
            else -> mutableMapOf()
        }
    }


    private fun MyViewModel.getOtherConfigValue(
        configName: String,
        key: String,
    ): Any? {
        return try {
            getOtherConfigMap(configName)[key]
        } catch(_: Exception) {
            null
        }
    }

    fun MyViewModel.getUiStateConfigValue(
        key: String,
    ): Any? {
        return getOtherConfigValue(
            configName = Config.ConfigFileName.UI_STATE,
            key = key,
        )
    }

    fun MyViewModel.getSearchHistoryConfigValue(
        key: String,
    ): Any? {
        return getOtherConfigValue(
            configName = Config.ConfigFileName.SEARCH_HISTORY,
            key = key,
        )
    }

    fun MyViewModel.getSearchSuggestionConfigValue(
        key: String,
    ): Any? {
        return getOtherConfigValue(
            configName = Config.ConfigFileName.SEARCH_SUGGESTION,
            key = key,
        )
    }


    // 更新配置的值-----------------------------------------
    fun MyViewModel.updateOtherConfigValue(
        configName: String,
        key: String,
        value: Any,
    ): Boolean {
        try {
            getOtherConfigMap(configName)[key] = value
            return true
        } catch(_: Exception) {
            return false
        }
    }

    fun MyViewModel.updateUiStateConfigValue(
        key: String,
        value: Any,
    ): Boolean {
        return updateOtherConfigValue(
            configName = Config.ConfigFileName.UI_STATE,
            key = key,
            value = value,
        )
    }

    fun MyViewModel.updateSearchHistoryConfigValue(
        key: String,
        value: Any,
    ): Boolean {
        return updateOtherConfigValue(
            configName = Config.ConfigFileName.SEARCH_HISTORY,
            key = key,
            value = value,
        )
    }

    fun MyViewModel.updateSearchSuggestionConfigValue(
        key: String,
        value: Any,
    ): Boolean {
        return updateOtherConfigValue(
            configName = Config.ConfigFileName.SEARCH_SUGGESTION,
            key = key,
            value = value,
        )
    }


    // 删除配置的值-----------------------------------------
    fun MyViewModel.deleteOtherConfigValue(
        configName: String,
        key: String,
    ): Boolean {
        try {
            getOtherConfigMap(configName).remove(key)
            return true
        } catch(_: Exception) {
            return false
        }
    }

//    fun MyViewModel.deleteUiStateConfigValue(
//        key: String,
//    ): Boolean {
//        return deleteOtherConfigValue(
//            configName = Config.ConfigFileName.UI_STATE,
//            key = key,
//        )
//    }

    fun MyViewModel.deleteSearchHistoryConfigValue(
        key: String,
    ): Boolean {
        return deleteOtherConfigValue(
            configName = Config.ConfigFileName.SEARCH_HISTORY,
            key = key,
        )
    }

    fun MyViewModel.deleteSearchSuggestionConfigValue(
        key: String,
    ): Boolean {
        return deleteOtherConfigValue(
            configName = Config.ConfigFileName.SEARCH_SUGGESTION,
            key = key,
        )
    }
}
