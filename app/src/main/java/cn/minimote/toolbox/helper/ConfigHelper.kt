/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
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


    // 清除用户配置文件
    fun clearUserConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.userConfig.clear()
        viewModel.userConfigBackup.clear()
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


    // 加载全部配置文件
    fun loadAllConfig(
        viewModel: ToolboxViewModel,
    ) {
        viewModel.defaultConfig = loadConfig(viewModel.myContext, false)
        viewModel.userConfig = loadConfig(viewModel.myContext, true)
        checkConfigFile(viewModel)
        viewModel.userConfigBackup = TreeMap(viewModel.userConfig)
    }


    // 检查配置文件
    fun checkConfigFile(
        viewModel: ToolboxViewModel,
    ) {
        val defaultConfig = viewModel.defaultConfig
        val userConfig = viewModel.userConfig

        val correctUserConfig = userConfig.filterTo(TreeMap<String, Any>()) { (key, value) ->
            val defaultValue = defaultConfig[key]
            defaultValue != null && value != defaultValue
        }

        if(userConfig.size != correctUserConfig.size) {
            viewModel.userConfig = correctUserConfig
            saveUserConfig(viewModel)
        }
    }


    // 加载配置文件
    fun loadConfig(
        context: Context,
        userConfigFlag: Boolean,
    ): TreeMap<String, Any> {
        return try {
            val inputStream = if(userConfigFlag) {
                FileInputStream(File(context.filesDir, ConfigFileName.USER_CONFIG))
            } else {
                context.assets.open(ConfigFileName.DEFAULT_CONFIG)
            }
            val json = inputStream.use { input ->
                val bytes = input.readBytes()
                String(bytes, ENCODING)
            }
            JSONObject(json).toTreeMap()
        } catch(e: Exception) {
            e.printStackTrace()
            TreeMap<String, Any>()
        }
    }


    // 保存用户配置文件
    fun saveUserConfig(
        viewModel: ToolboxViewModel,
    ) {
//        viewModel.viewModelScope.launch(Dispatchers.IO) {
        val context = viewModel.myContext
        val userConfigFile = File(context.filesDir, ConfigFileName.USER_CONFIG)
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


    // 将JSONObject转换为Map
    private fun JSONObject.toTreeMap(): TreeMap<String, Any> {
        val map = TreeMap<String, Any>()
        val keysItr = this.keys()
        while(keysItr.hasNext()) {
            val key = keysItr.next()
            val value = when(val value = this[key]) {
                is JSONObject -> value.toTreeMap()
                else -> value
            }
            map[key] = value
        }
        return map
    }
}