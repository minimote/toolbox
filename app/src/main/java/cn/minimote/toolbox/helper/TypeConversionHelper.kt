/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import org.json.JSONArray
import org.json.JSONObject


object TypeConversionHelper {

    fun JSONArray.toMutableList(): MutableList<*> {
        val result = mutableListOf<Any?>()
        for(i in 0 until this.length()) {
            val convertedValue = when(val value = this[i]) {
                is JSONObject -> value.toMutableMap()
                is JSONArray -> value.toMutableList()
                else -> value
            }
            result.add(convertedValue)
        }
        return result
    }


    fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for(i in 0 until length()) {
            list.add(getString(i))
        }
        return list
    }


    fun List<*>.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for(item in this) {
            try {
                list.add(item.toString())
            } catch(_: Exception) {
//                list.add("")
            }
        }
        return list
    }


    // 将 JSONObject 转换为 MutableMap
    fun JSONObject.toMutableMap(): MutableMap<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keysItr = this.keys()
        while(keysItr.hasNext()) {
            val key = keysItr.next()
            val value = when(val value = this[key]) {
                is JSONObject -> value.toMutableMap()
                is JSONArray -> value.toMutableList()
                else -> value
            }
            map[key] = value
        }
        return map
    }


    fun Any?.toFloatList(): List<Float> {
        return when(this) {
            is List<*> -> {
                val list = mutableListOf<Float>()
                for(item in this) {
                    try {
                        list.add(item.toString().toFloat())
                    } catch(_: Exception) {
//                        list.add(0f)
                    }
                }
                list
            }

            is JSONArray -> {
                val list = mutableListOf<Float>()
                for(i in 0 until length()) {
                    try {
                        list.add(opt(i).toString().toFloat())
                    } catch(_: Exception) {
//                        list.add(0f)
                    }
                }
                list
            }

            else -> emptyList()
        }
    }


    fun Any?.toStringList(): List<String> {
        return when(this) {
            is List<*> -> {
                this.toStringList()
            }

            is JSONArray -> {
                val list = mutableListOf<String>()
                for(i in 0 until length()) {
                    try {
                        list.add(opt(i).toString())
                    } catch(_: Exception) {
//                        list.add("")
                    }
                }
                list
            }

            else -> emptyList()
        }
    }

}