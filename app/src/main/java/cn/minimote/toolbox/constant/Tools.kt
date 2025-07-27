/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.constant.Icon.IconKey
import cn.minimote.toolbox.dataClass.Tool


object Tools {

    // 扫码与支付
    object ScanAndPay {
        // 微信
        object WeChat {
            private const val PACKAGE_NAME = "com.tencent.mm"

            val scan by lazy {
                Tool(
                    id = ToolID.ScanAndPay.WeChat.SCAN,
                    name = "微信-扫一扫",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.PACKAGE,
                    iconKey = PACKAGE_NAME,
                    intentExtras = mapOf(
                        "LauncherUI.From.Scaner.Shortcut" to true
                    ),
                )
            }

            val payCode by lazy {
                Tool(
                    id = ToolID.ScanAndPay.WeChat.PAY_CODE,
                    name = "微信-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.ACTION,
                    iconKey = PACKAGE_NAME,
                    intentAction = "com.tencent.mm.action.BIZSHORTCUT",
                    intentExtras = mapOf(
                        "LauncherUI.Shortcut.LaunchType" to "launch_type_offline_wallet"
                    ),
                )
            }

            val myCard by lazy {
                Tool(
                    id = ToolID.ScanAndPay.WeChat.MY_CARD,
                    name = "微信-名片码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.ACTION,
                    iconKey = PACKAGE_NAME,
                    intentAction = "com.tencent.mm.action.BIZSHORTCUT",
                    intentExtras = mapOf(
                        "LauncherUI.Shortcut.LaunchType" to "launch_type_my_qrcode"
                    ),
                )
            }
        }

        // 支付宝
        object Alipay {
            private const val PACKAGE_NAME = "com.eg.android.AlipayGphone"

            val scan by lazy {
                Tool(
                    id = ToolID.ScanAndPay.Alipay.SCAN,
                    name = "支付宝-扫一扫",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=10000007",
                )
            }

            val payCode by lazy {
                Tool(
                    id = ToolID.ScanAndPay.Alipay.PAY_CODE,
                    name = "支付宝-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=20000056",
                )
            }

            val collectCode by lazy {
                Tool(
                    id = ToolID.ScanAndPay.Alipay.COLLECT_CODE,
                    name = "支付宝-收款码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=20000123",
                )
            }

            val rideCode by lazy {
                Tool(
                    id = ToolID.ScanAndPay.Alipay.RIDE_CODE,
                    name = "支付宝-乘车码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=200011235",
                )
            }

            // 手表付款码
            val payCodeWatch by lazy {
                Tool(
                    id = ToolID.ScanAndPay.Alipay.PAY_CODE_WATCH,
                    name = "支付宝-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://showpage=codepay",
                )
            }

            // 支付宝-支持作者
            val support_author by lazy {
                Tool(
                    id = ToolID.ScanAndPay.Alipay.SUPPORT_AUTHOR,
                    name = "支付宝-支持作者",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/fkx13254he9xc0xpa3tdva0",
                )
            }
        }

        // 云闪付
        object YunShanFu {
            private const val PACKAGE_NAME = "com.unionpay"

            val scan by lazy {
                Tool(
                    id = ToolID.ScanAndPay.YunShanFu.SCAN,
                    name = "云闪付-扫一扫",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "upwallet://native/scanCode",
                )
            }

            val payCode by lazy {
                Tool(
                    id = ToolID.ScanAndPay.YunShanFu.PAY_CODE,
                    name = "云闪付-付款码",
                    packageName = PACKAGE_NAME,
                    intentType = LaunchType.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "upwallet://native/codepay",
                )
            }
        }
    }

    // 系统工具
    object SystemTool {

        private const val PACKAGE_NAME = "com.android.settings"

        val developerOption by lazy {
            Tool(
                id = ToolID.SystemTool.DEVELOPER_OPTION,
                name = "开发者选项",
                packageName = PACKAGE_NAME,
                activityName = "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity",
                iconKey = IconKey.DEVELOPER_OPTION,
            )
        }

        val accessibilityOption by lazy {
            Tool(
                id = ToolID.SystemTool.ACCESSIBILITY_OPTION,
                name = "无障碍选项",
                packageName = PACKAGE_NAME,
                activityName = "com.android.settings.Settings\$AccessibilitySettingsActivity",
                iconKey = IconKey.ACCESSIBILITY_OPTION,
            )
        }

        val systemUIDemoMode by lazy {
            Tool(
                id = ToolID.SystemTool.SYSTEM_UI_DEMO_MODE,
                name = "系统界面调节",
                packageName = "com.android.systemui",
                activityName = "com.android.systemui.DemoMode",
                description = "\u3000\u3000状态栏显示秒路径：状态栏→时间",
                warningMessage = "\u3000\u3000该界面是 Android 官方提供的实验性界面，存在修改后无法恢复等风险，使用者后果自负！",
            )
        }

        val recentTask by lazy {
            Tool(
                id = ToolID.SystemTool.RECENT_TASK,
                name = "最近任务",
                packageName = "com.heytap.wearable.launcher",
                activityName = "com.android.quickstep.RecentsActivity",
                iconKey = IconKey.RECENT_TASK,
            )
        }

        val androidEasterEgg by lazy {
            Tool(
                id = ToolID.SystemTool.ANDROID_EASTER_EGG,
                name = "安卓彩蛋",
                packageName = "com.android.egg",
                activityName = "com.android.egg.landroid.MainActivity"
            )
        }

    }
}

