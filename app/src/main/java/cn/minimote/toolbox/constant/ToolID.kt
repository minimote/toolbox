/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ToolID {

    // 扫码与支付
    object ScanAndPay {
        // 微信
        object WeChat {
            private const val URL_NAME = "wechat/"

            const val SCAN = URL_NAME + "scan"

            const val PAY_CODE = URL_NAME + "pay_code"

            const val MY_CARD = URL_NAME + "my_card"
        }

        // 支付宝
        object Alipay {
            private const val URL_NAME = "alipay/"

            const val SCAN = URL_NAME + "scan"

            const val PAY_CODE = URL_NAME + "pay_code"
            // 手表付款码
            const val PAY_CODE_WATCH = URL_NAME + "pay_code_watch"

            const val COLLECT_CODE = URL_NAME + "collect_code"

            const val RIDE_CODE = URL_NAME + "ride_code"

            // 支持作者
            const val SUPPORT_AUTHOR = URL_NAME + "support_author"
        }

        // 云闪付
        object YunShanFu {
            private const val URL_NAME = "yunshanfu/"

            const val SCAN = URL_NAME + "scan"

            const val PAY_CODE = URL_NAME + "pay_code"
        }
    }

    // 系统工具
    object SystemTool {
        private const val URL_NAME = "system/"

        const val DEVELOPER_OPTION = URL_NAME + "developer_option"

        const val ACCESSIBILITY_OPTION = URL_NAME + "accessibility_option"

        const val SYSTEM_UI_DEMO_MODE = URL_NAME + "system_UI_demo_mode"

        const val RECENT_TASK = URL_NAME + "recent_task"

        const val ANDROID_EASTER_EGG = URL_NAME + "android_easter_egg"
    }
}