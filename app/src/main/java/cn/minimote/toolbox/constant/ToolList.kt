/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import android.content.Context
import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.ExpandableGroup

// 工具
object ToolList {
    object AllDevice {
        val topGroup by lazy {
            listOf(
                // 添加本机软件
                ExpandableGroup(
                    titleId = R.string.add_local_app,
                    viewTypeList = listOf()
                ),
            )

        }

        val unTopGroup by lazy {
            listOf(
                // 系统工具
                ExpandableGroup(
                    titleId = R.string.system_tools,
                    viewTypeList = listOf(
                        Tools.SystemTools.Settings.developerOption, // 开发者选项
                        Tools.SystemTools.Settings.accessibilityOption, // 无障碍选项
                    ),
                ),
            )
        }
    }

    object PhoneDevice {
        val topGroup by lazy {
            listOf<ExpandableGroup>()
        }

        val unTopGroup by lazy {
            listOf(
                // 扫码与支付
                ExpandableGroup(
                    titleId = R.string.scan_and_pay,
                    viewTypeList = listOf(
                        // 微信
                        Tools.ScanAndPay.WeChat.scanQrCode, // 扫一扫
                        Tools.ScanAndPay.WeChat.payCode, // 付款码
                        Tools.ScanAndPay.WeChat.myCard, // 名片码
                        // 支付宝
                        Tools.ScanAndPay.Alipay.scanQrCode, // 扫一扫
                        Tools.ScanAndPay.Alipay.payCode, // 付款码
                        Tools.ScanAndPay.Alipay.collectCode, // 收款码
                        Tools.ScanAndPay.Alipay.rideCode, // 乘车码
                        // 云闪付
                        Tools.ScanAndPay.YunShanFu.scanQrCode, // 扫一扫
                        Tools.ScanAndPay.YunShanFu.payCode, // 付款码
                    )
                ),
            )
        }
    }

    object WatchDevice {
        val topGroup by lazy {
            listOf<ExpandableGroup>()
        }

        val unTopGroup by lazy {
            listOf(
                // 扫码与支付
                ExpandableGroup(
                    titleId = R.string.scan_and_pay,
                    viewTypeList = listOf(
                        // 支付宝
                        Tools.ScanAndPay.Alipay.watchPayCode, // 付款码
                    )
                ),
                // 系统工具
                ExpandableGroup(
                    titleId = R.string.system_tools,
                    viewTypeList = listOf(
                        Tools.SystemTools.recentTask, // 最近任务
                    ),
                ),
            )
        }
    }


    // 获取工具列表的方法
    fun getToolListByDeviceType(context: Context, deviceType: String): List<ExpandableGroup> {
        return when(deviceType) {
            DeviceType.PHONE -> {
                (AllDevice.topGroup + PhoneDevice.topGroup).distinctAndSort(
                    context
                ) + (AllDevice.unTopGroup + PhoneDevice.unTopGroup).distinctAndSort(
                    context
                )
            }

            DeviceType.WATCH -> {
                (AllDevice.topGroup + WatchDevice.topGroup).distinctAndSort(
                    context
                ) + (AllDevice.unTopGroup + WatchDevice.unTopGroup).distinctAndSort(
                    context
                )
            }

            else -> {
                listOf()
            }
        }
    }


    // 去重并且排序
    fun List<ExpandableGroup>.distinctAndSort(context: Context): List<ExpandableGroup> {
        val dict = mutableMapOf<Int, ExpandableGroup>()
        for(item in this) {
            val titleId = item.titleId
            val currentItem = dict[titleId]
            if(currentItem != null) {
                currentItem.viewTypeList += item.viewTypeList
            } else {
                dict[titleId] = item
            }
        }

        // 对每个value的viewTypeList去重并排序
        dict.values.forEach { it ->
            it.viewTypeList = it.viewTypeList.distinct()
                .sortedWith(
                    compareBy(Collator.chineseCollator) { it.name }
                )
        }

        // 返回按照value排序的列表
        return dict.values.sortedWith(
            compareBy(Collator.chineseCollator) {
                context.getString(it.titleId)
            }
        )

    }
}