/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.constant.Config.ConfigFileName.otherConfigFileNameList
import cn.minimote.toolbox.constant.Config.recommendedConfig
import cn.minimote.toolbox.helper.OtherConfigHelper.loadOtherConfig
import cn.minimote.toolbox.helper.PathHelper.getUserConfigFile
import cn.minimote.toolbox.viewModel.MyViewModel
import java.util.TreeMap

object ConfigHelper {


    // 获取配置的值
    fun MyViewModel.getConfigValue(
        key: String,
    ): Any? {
        val viewModel = this
        val defaultConfig = viewModel.defaultConfig
        val userConfig = viewModel.userConfig

        if(defaultConfig.containsKey(key)) {
            if(userConfig.containsKey(key)) {
                return userConfig[key]
            }
            return defaultConfig[key]
        } else {
            return null
        }
    }


    // 清除用户和备份的配置文件
    fun MyViewModel.clearUserAndBackupConfig() {
        this.userConfig.clear()
        this.userConfigBackup.clear()
        loadAllConfig()
    }


    // 仅清除用户配置
    fun MyViewModel.clearUserConfigOnly() {
        this.userConfig.clear()
        this.configChanged.value = this.configChanged.value
    }


    // 更新配置的值
    fun MyViewModel.updateConfigValue(
        key: String,
        value: Any,
    ) {
        val defaultConfig = this.defaultConfig
        val userConfig = this.userConfig

        if(defaultConfig.containsKey(key)) {
            if(defaultConfig[key] == value) {
                userConfig.remove(key)
            } else {
                userConfig[key] = value
            }
        }
    }


    // 用户配置是否存在该项
    fun MyViewModel.hasUserConfigKey(
        key: String,
    ): Boolean {
        return this.userConfig.containsKey(key)
    }


    // 删除配置的值
    fun MyViewModel.deleteConfigValue(
        key: String,
    ) {
        this.userConfig.remove(key)
    }


    // 使用推荐配置
    fun MyViewModel.useRecommendedConfig() {
        this.userConfig = recommendedConfig.toMutableMap()
        checkConfigFile()
        saveUserConfig()
        this.userConfigBackup = this.userConfig.toMutableMap()
        loadAllConfig()
    }


    // 加载全部配置文件
    fun MyViewModel.loadAllConfig() {
        this.userConfig = getUserConfig()
        checkConfigFile()
        this.userConfigBackup = this.userConfig.toMutableMap()

        this.loadDynamicShortcutList()
        for(configName in otherConfigFileNameList) {
            loadOtherConfig(configName)
        }
    }


    // 检查配置文件
    fun MyViewModel.checkConfigFile() {
        val defaultConfig = this.defaultConfig
        val userConfig = this.userConfig

        val correctUserConfig = userConfig.filterTo(mutableMapOf()) { (key, value) ->
            val defaultValue = defaultConfig[key]
            defaultValue != null && value != defaultValue
        }

        if(userConfig.size != correctUserConfig.size) {
            this.userConfig = correctUserConfig
            saveUserConfig()
        }
    }


    // 加载配置文件
    private fun MyViewModel.getUserConfig(): MutableMap<String, Any> {
        return FileHelper.readJsonFileAsMutableMap(
            getUserConfigFile()
        ) ?: mutableMapOf()
    }


    // 保存用户配置文件
    fun MyViewModel.saveUserConfig(
        config: Map<String, Any> = this.userConfig.toMap(),
        updateBackup: Boolean = true,
    ): Boolean {
//        viewModel.viewModelScope.launch(Dispatchers.IO) {
//        val context = viewModel.myContext
        var flag = false
        try {
            val userConfigFile = getUserConfigFile()
            // 如果配置为空，删除配置文件
            if(config.isEmpty()) {
                if(userConfigFile.exists()) {
                    userConfigFile.delete()
                }
            } else {
                flag = FileHelper.writeDataToJsonFile(
                    data = config,
                    file = userConfigFile,
                )
            }

            if(updateBackup) {
                updateBackupConfig()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            flag = false
        }
        return flag
//        }
    }


    // 更新
    fun MyViewModel.updateSettingWasModified() {
        val flag = this.userConfig != this.userConfigBackup
        if(flag != this.configChanged.value) {
            this.configChanged.value = flag
        }
    }


    // 备份用户配置文件
    private fun MyViewModel.updateBackupConfig() {
        this.userConfigBackup = TreeMap(this.userConfig)
        if(this.configChanged.value != false) {
            this.configChanged.value = false
        }
    }


    // 恢复用户配置文件
    fun restoreUserConfig(
        viewModel: MyViewModel,
    ) {
        viewModel.userConfig = TreeMap(viewModel.userConfigBackup)
//        if(viewModel.settingWasModified.value != false) {
//            viewModel.settingWasModified.value = false
//        }
    }

}