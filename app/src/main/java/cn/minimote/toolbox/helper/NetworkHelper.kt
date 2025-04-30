/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config.ConfigKeys.NetworkAccessModeKeys
import cn.minimote.toolbox.constant.Config.ConfigValues.NetworkAccessModeValues
import cn.minimote.toolbox.constant.NetworkTypes
import cn.minimote.toolbox.viewModel.ToolboxViewModel

object NetworkHelper {

    // 获取网络类型
    fun getNetworkType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkTypes.DISCONNECTED
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkTypes.OTHER

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                -> NetworkTypes.MOBILE

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                -> NetworkTypes.BLUETOOTH

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                -> NetworkTypes.WIFI

            else -> NetworkTypes.OTHER
        }
    }


//    // 显示网络类型
//    fun showNetworkType(context: Context) {
//        val networkType = getNetworkType(context)
//        val networkTypeString = getNetworkTypeString(context, networkType)
//        Toast.makeText(context, "网络类型：$networkTypeString", Toast.LENGTH_SHORT).show()
//    }


    // 获取网络类型的字符串
    fun getNetworkTypeString(
        context: Context,
        networkType: String,
    ): String {
        return when(networkType) {
            NetworkTypes.WIFI -> {
                context.getString(R.string.network_type_wifi)
            }

            NetworkTypes.MOBILE -> {
                context.getString(R.string.network_type_mobile)
            }

            NetworkTypes.BLUETOOTH -> {
                context.getString(R.string.network_type_bluetooth)
            }

            NetworkTypes.DISCONNECTED -> {
                context.getString(R.string.network_type_disconnected)
            }

            NetworkTypes.OTHER -> {
                context.getString(R.string.network_type_other)
            }

            else -> {
                throw IllegalArgumentException("非法的网络类型：$networkType")
            }
        }
    }


    // 获取网络访问模式
    fun getNetworkAccessMode(
        networkType: String,
        viewModel: ToolboxViewModel,
    ): String {
        return when(networkType) {
            NetworkTypes.MOBILE -> {
                ConfigHelper.getConfigValue(NetworkAccessModeKeys.MOBILE, viewModel)
            }

            NetworkTypes.BLUETOOTH -> {
                ConfigHelper.getConfigValue(NetworkAccessModeKeys.BLUETOOTH, viewModel)
            }

            NetworkTypes.WIFI -> {
                ConfigHelper.getConfigValue(NetworkAccessModeKeys.WIFI, viewModel)
            }

            NetworkTypes.OTHER -> {
                ConfigHelper.getConfigValue(NetworkAccessModeKeys.OTHER, viewModel)
            }

            else -> {
                throw IllegalArgumentException("非法的网络类型：$networkType")
            }
        }.toString()
    }


    // 获取网络访问模式的字符串
    fun getNetworkAccessModeString(
        context: Context,
        networkAccessMode: String,
    ): String {
        return when(networkAccessMode) {
            NetworkAccessModeValues.ALERT -> {
                context.getString(R.string.network_access_mode_alert)
            }

            NetworkAccessModeValues.DENY -> {
                context.getString(R.string.network_access_mode_deny)
            }

            NetworkAccessModeValues.ALLOW -> {
                context.getString(R.string.network_access_mode_allow)
            }

            else -> {
                throw IllegalArgumentException("非法的网络访问模式：$networkAccessMode")
            }
        }
    }
}