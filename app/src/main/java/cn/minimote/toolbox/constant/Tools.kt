/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.constant.Icon.IconKey
import cn.minimote.toolbox.dataClass.ToolActivity

object Tools {
    // 扫码与支付
    object ScanAndPay {
        // 微信
        object WeChat {
            private const val PACKAGE_NAME = "com.tencent.mm"

            val scanQrCode by lazy {
                ToolActivity(
                    appName = "微信-扫一扫",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.PACKAGE,
                    iconKey = PACKAGE_NAME,
                    intentExtras = mapOf(
                        "LauncherUI.From.Scaner.Shortcut" to true
                    ),
                )
            }

            val payCode by lazy {
                ToolActivity(
                    appName = "微信-付款码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.ACTION,
                    iconKey = PACKAGE_NAME,
                    intentAction = "com.tencent.mm.action.BIZSHORTCUT",
                    intentExtras = mapOf(
                        "LauncherUI.Shortcut.LaunchType" to "launch_type_offline_wallet"
                    ),
                )
            }

            val businessCard by lazy {
                ToolActivity(
                    appName = "微信-名片码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.ACTION,
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

            val scanQrCode by lazy {
                ToolActivity(
                    appName = "支付宝-扫一扫",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=10000007",
                )
            }

            val payCode by lazy {
                ToolActivity(
                    appName = "支付宝-付款码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=20000056",
                )
            }

            val collectCode by lazy {
                ToolActivity(
                    appName = "支付宝-收款码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=20000123",
                )
            }

            val rideCode by lazy {
                ToolActivity(
                    appName = "支付宝-乘车码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://platformapi/startapp?appId=200011235",
                )
            }

            // 手表付款码
            val watchPayCode by lazy {
                ToolActivity(
                    appName = "支付宝-付款码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "alipays://showpage=codepay",
                )
            }
        }

        // 云闪付
        object YunShanFu {
            private const val PACKAGE_NAME = "com.unionpay"

            val scanQrCode by lazy {
                ToolActivity(
                    appName = "云闪付-扫一扫",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "upwallet://native/scanCode",
                )
            }

            val payCode by lazy {
                ToolActivity(
                    appName = "云闪付-付款码",
                    packageName = PACKAGE_NAME,
                    launchType = LaunchTypes.SCHEME,
                    iconKey = PACKAGE_NAME,
                    intentUri = "upwallet://native/codepay",
                )
            }
        }
    }

    // 系统工具
    object SystemTools {
        object Settings {
            private const val PACKAGE_NAME = "com.android.settings"

            val developerOption by lazy {
                ToolActivity(
                    appName = "开发者选项",
                    packageName = PACKAGE_NAME,
                    activityName = "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity",
                    launchType = LaunchTypes.PACKAGE_AND_ACTIVITY,
                    iconKey = IconKey.DEVELOPER_OPTION,
                )
            }

            val accessibilityOption by lazy {
                ToolActivity(
                    appName = "无障碍选项",
                    packageName = PACKAGE_NAME,
                    activityName = "com.android.settings.Settings\$AccessibilitySettingsActivity",
                    launchType = LaunchTypes.PACKAGE_AND_ACTIVITY,
                    iconKey = IconKey.ACCESSIBILITY_OPTION,
                )
            }
        }

        val recentTask by lazy {
            ToolActivity(
                appName = "最近任务",
                packageName = "com.heytap.wearable.launcher",
                activityName = "com.android.quickstep.RecentsActivity",
                iconKey = IconKey.RECENT_TASK,
            )
        }
    }
}
