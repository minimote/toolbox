/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigFileName
import cn.minimote.toolbox.constant.Config.ENCODING
import cn.minimote.toolbox.helper.TypeConversionHelper.toMutableMap
import cn.minimote.toolbox.viewModel.MyViewModel
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
    }


    // 仅清除用户配置
    fun MyViewModel.clearUserConfig() {
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


    // 用户配置存在该项
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


    // 加载全部配置文件
    fun MyViewModel.loadAllConfig() {
        this.userConfig = loadUserConfig()
        checkConfigFile()
        this.userConfigBackup = this.userConfig.toMutableMap()
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


    // 获取用户配置路径
    private fun MyViewModel.getUserConfigPath(): File {
        return File(this.dataPath, ConfigFileName.USER_CONFIG)
    }


    // 加载配置文件
    private fun MyViewModel.loadUserConfig(): MutableMap<String, Any> {
        return try {
            val inputStream = FileInputStream(getUserConfigPath())

            val json = inputStream.use { input ->
                val bytes = input.readBytes()
                String(bytes, ENCODING)
            }
            JSONObject(json).toMutableMap()
        } catch(e: Exception) {
            e.printStackTrace()
            mutableMapOf()
        }
    }


    // 保存用户配置文件
    fun MyViewModel.saveUserConfig(
        config: Map<String, Any> = this.userConfig.toMap(),
        updateBackup: Boolean = true,
    ) {
//        viewModel.viewModelScope.launch(Dispatchers.IO) {
//        val context = viewModel.myContext
        try {
            val userConfigFile = getUserConfigPath()
            // 如果配置为空，删除配置文件
            if(config.isEmpty()) {
                if(userConfigFile.exists()) {
                    userConfigFile.delete()
                }
            } else {
                val jsonString = JSONObject(config).toString()

                FileOutputStream(userConfigFile).use { outputStream ->
                    outputStream.write(jsonString.toByteArray(ENCODING))
                }
            }

            if(updateBackup) {
                updateBackupConfig()
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
//        }
    }


    // 更新
    fun MyViewModel.updateSettingWasModified() {
        val flag = this.userConfig != this.userConfigBackup
        if(flag != this.configChanged.value) {
            this.configChanged.value = flag
        }
    }


    // 保存：保存按钮的位置
    fun MyViewModel.saveButtonPosition(
        targetPos: List<Float>,
    ) {
        val key = Config.ConfigKeys.SAVE_BUTTON_POSITION
        this.userConfig[key] = targetPos
        this.userConfigBackup[key] = targetPos

        saveUserConfig(
            config = this.userConfigBackup,
            updateBackup = false,
        )
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