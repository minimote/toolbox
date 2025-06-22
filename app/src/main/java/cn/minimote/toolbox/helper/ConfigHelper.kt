/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigFileName
import cn.minimote.toolbox.constant.Config.ENCODING
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.TreeMap

object ConfigHelper {

    // 获取配置的值
    fun getConfigValue(
        key: String,
        viewModel: ToolboxViewModel,
//        defaultConfig: MutableMap<String, Any>,
//        userConfig: MutableMap<String, Any>,
    ): Any? {
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
    fun clearUserAndBackupConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.userConfig.clear()
        viewModel.userConfigBackup.clear()
    }


    // 仅清除用户配置
    fun clearUserConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.userConfig.clear()
    }

    // 更新配置的值
    fun updateConfigValue(
        key: String,
        value: Any,
        viewModel: ToolboxViewModel,
    ) {
        val defaultConfig = viewModel.defaultConfig
        val userConfig = viewModel.userConfig

        if(defaultConfig.containsKey(key)) {
            if(defaultConfig[key] == value) {
                userConfig.remove(key)
            } else {
                userConfig[key] = value
            }
        }
    }


    // 用户配置存在该项
    fun hasUserConfigKey(
        key: String,
        viewModel: ToolboxViewModel,
    ): Boolean {
        return viewModel.userConfig.containsKey(key)
    }


    // 删除配置的值
    fun deleteConfigValue(
        key: String,
        viewModel: ToolboxViewModel,
    ) {
        viewModel.userConfig.remove(key)
    }


    // 加载全部配置文件
    fun loadAllConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.defaultConfig = Config.defaultConfig
        viewModel.userConfig = loadUserConfig(viewModel)
        checkConfigFile(viewModel)
        viewModel.userConfigBackup = viewModel.userConfig.toMutableMap()
    }


    // 检查配置文件
    fun checkConfigFile(
        viewModel: ToolboxViewModel,
    ) {
        val defaultConfig = viewModel.defaultConfig
        val userConfig = viewModel.userConfig

        val correctUserConfig = userConfig.filterTo(mutableMapOf<String, Any>()) { (key, value) ->
            val defaultValue = defaultConfig[key]
            defaultValue != null && value != defaultValue
        }

        if(userConfig.size != correctUserConfig.size) {
            viewModel.userConfig = correctUserConfig
            saveUserConfig(viewModel)
        }
    }


    // 获取用户配置路径
    private fun getUserConfigPath(viewModel: ToolboxViewModel): File {
        return File(viewModel.dataPath, ConfigFileName.USER_CONFIG)
    }


    // 加载配置文件
    private fun loadUserConfig(
        viewModel: ToolboxViewModel,
    ): MutableMap<String, Any> {
        return try {
            val inputStream = FileInputStream(getUserConfigPath(viewModel))

            val json = inputStream.use { input ->
                val bytes = input.readBytes()
                String(bytes, ENCODING)
            }
            JSONObject(json).toMutableMap()
        } catch(e: Exception) {
            e.printStackTrace()
            mutableMapOf<String, Any>()
        }
    }


    // 保存用户配置文件
    fun saveUserConfig(
        viewModel: ToolboxViewModel,
    ) {
//        viewModel.viewModelScope.launch(Dispatchers.IO) {
        val context = viewModel.myContext
        val userConfigFile = getUserConfigPath(viewModel)
        try {
            val config = viewModel.userConfig.toMap()
            val jsonString = JSONObject(config).toString()

            FileOutputStream(userConfigFile).use { outputStream ->
                outputStream.write(jsonString.toByteArray(ENCODING))
            }

            backupUserConfig(viewModel)
        } catch(e: Exception) {
            e.printStackTrace()
        }
//        }
    }


    // 更新
    fun updateSettingWasModified(viewModel: ToolboxViewModel) {
        val flag = viewModel.userConfig != viewModel.userConfigBackup
//        Toast.makeText(
//            viewModel.myContext,
//            "user=${viewModel.userConfig.size}, back=${viewModel.userConfigBackup.size}",
//            Toast.LENGTH_SHORT,
//        ).show()
        if(flag != viewModel.settingWasModified.value) {
            viewModel.settingWasModified.value = flag
        }
    }


    // 备份用户配置文件
    private fun backupUserConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.userConfigBackup = TreeMap(viewModel.userConfig)
        if(viewModel.settingWasModified.value != false) {
            viewModel.settingWasModified.value = false
        }
    }


    // 恢复用户配置文件
    fun restoreUserConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.userConfig = TreeMap(viewModel.userConfigBackup)
//        if(viewModel.settingWasModified.value != false) {
//            viewModel.settingWasModified.value = false
//        }
    }


    // 将 JSONObject 转换为 MutableMap
    private fun JSONObject.toMutableMap(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keysItr = this.keys()
        while(keysItr.hasNext()) {
            val key = keysItr.next()
            val value = when(val value = this[key]) {
                is JSONObject -> value.toMutableMap()
                else -> value
            }
            map[key] = value
        }
        return map
    }
}