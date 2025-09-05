/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import org.json.JSONArray
import org.json.JSONObject


object TypeConversionHelper {

    fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for(i in 0 until length()) {
            list.add(getString(i))
        }
        return list
    }

    fun List<*>.toStringList(): List<String> {
        return filterIsInstance<String>()
    }

    // 将 JSONObject 转换为 MutableMap
    fun JSONObject.toMutableMap(): MutableMap<String, Any> {
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