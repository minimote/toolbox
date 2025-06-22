/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.ExpandableGroup

// 工具
object ToolList {
    // 默认设备：手机平板
    val default by lazy {
        listOf(
            // 添加本机软件
            ExpandableGroup(
                titleId = R.string.add_local_app,
                viewTypeList = listOf()
            ),

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
    // 手表
    val watch by lazy {
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
                    Tools.SystemTools.Settings.developerOption, // 开发者选项
                    Tools.SystemTools.Settings.accessibilityOption, // 无障碍选项
                    Tools.SystemTools.recentTask, // 最近任务
                ),
            ),
        )
    }
}