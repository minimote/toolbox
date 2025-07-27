/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ToolMap {
    val idToTool by lazy {
        mapOf(
            ToolID.ScanAndPay.WeChat.SCAN to Tools.ScanAndPay.WeChat.scan,
            ToolID.ScanAndPay.WeChat.PAY_CODE to Tools.ScanAndPay.WeChat.payCode,
            ToolID.ScanAndPay.WeChat.MY_CARD to Tools.ScanAndPay.WeChat.myCard,

            ToolID.ScanAndPay.Alipay.SCAN to Tools.ScanAndPay.Alipay.scan,
            ToolID.ScanAndPay.Alipay.PAY_CODE to Tools.ScanAndPay.Alipay.payCode,
            ToolID.ScanAndPay.Alipay.PAY_CODE_WATCH to Tools.ScanAndPay.Alipay.payCodeWatch,
            ToolID.ScanAndPay.Alipay.COLLECT_CODE to Tools.ScanAndPay.Alipay.collectCode,
            ToolID.ScanAndPay.Alipay.RIDE_CODE to Tools.ScanAndPay.Alipay.rideCode,

            ToolID.ScanAndPay.YunShanFu.SCAN to Tools.ScanAndPay.YunShanFu.scan,
            ToolID.ScanAndPay.YunShanFu.PAY_CODE to Tools.ScanAndPay.YunShanFu.payCode,

            ToolID.SystemTool.DEVELOPER_OPTION to Tools.SystemTool.developerOption,
            ToolID.SystemTool.ACCESSIBILITY_OPTION to Tools.SystemTool.accessibilityOption,
            ToolID.SystemTool.SYSTEM_UI_DEMO_MODE to Tools.SystemTool.systemUIDemoMode,
            ToolID.SystemTool.RECENT_TASK to Tools.SystemTool.recentTask,
            ToolID.SystemTool.ANDROID_EASTER_EGG to Tools.SystemTool.androidEasterEgg,
        )
    }
}