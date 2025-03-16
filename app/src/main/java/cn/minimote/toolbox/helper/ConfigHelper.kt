///*
// * Copyright (c) 2025 minimote(微尘). All rights reserved.
// * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
// */

package cn.minimote.toolbox.helper

import android.content.Context
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object ConfigHelper {

    private val ENCODING: Charset = StandardCharsets.UTF_8

    object ConfigName {
        const val DEFAULT_CONFIG = "default_config.jsonc"
        const val USER_CONFIG = "user_config.jsonc"
    }


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


    // 加载配置文件
    fun loadConfig(
        context: Context,
        configName: String,
    ): MutableMap<String, Any> {
        return when(configName) {
            ConfigName.DEFAULT_CONFIG -> loadDefaultConfig(context)
            ConfigName.USER_CONFIG -> loadUserConfig(context)
            else -> mutableMapOf()
        }
    }


    // 加载默认配置文件
    private fun loadDefaultConfig(context: Context): MutableMap<String, Any> {
        return try {
            val inputStream = context.assets.open(ConfigName.DEFAULT_CONFIG)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, ENCODING)
            JSONObject(json).toMutableMap()
        } catch(e: Exception) {
            e.printStackTrace()
            mutableMapOf()
        }
    }


    // 加载用户配置文件
    private fun loadUserConfig(
        context: Context,
    ): MutableMap<String, Any> {
        val userConfigFile = File(context.filesDir, ConfigName.USER_CONFIG)
//        if(!userConfigFile.exists()) {
//            return mutableMapOf()
//        }
        return try {
            val inputStream = FileInputStream(userConfigFile)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, ENCODING)
            JSONObject(json).toMutableMap()
        } catch(e: Exception) {
            e.printStackTrace()
            mutableMapOf()
        }
    }


    // 保存用户配置文件
    fun saveUserConfig(
        context: Context,
        config: Map<String, Any>,
    ) {
        val userConfigFile = File(context.filesDir, ConfigName.USER_CONFIG)
        try {
            val jsonString = JSONObject(config).toString()
            val outputStream = FileOutputStream(userConfigFile)
            outputStream.write(jsonString.toByteArray(ENCODING))
            outputStream.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }


    // 将JSONObject转换为Map
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