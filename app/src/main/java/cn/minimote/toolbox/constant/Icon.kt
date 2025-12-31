/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object Icon {
    const val ICON_CACHE_PATH = "icon_cache"

    // 图标大小
    const val ICON_SIZE = 512

    object IconKey {
        const val DEFAULT = "default"


        private const val SCAN = "_scan"
        private const val PAY_CODE = "_pay_code"
        private const val COLLECT_CODE = "_collect_code"
        private const val RIDE_CODE = "_ride_code"
        private const val MY_CARD = "my_card"


        // 不可用
        const val UNAVAILABLE = "unavailable"
        const val DEVELOPER_OPTION = "developer_option"
        const val RECENT_TASK = "recent_task"
        const val ACCESSIBILITY_OPTION = "accessibility_option"
        const val WOODEN_FISH = "wooden_fish"


        const val BILIBILI = "bilibili"


        const val DOUYIN = "douyin"


        const val AMAP = "amap"


        const val HELLO_BIKE = "hello_bike"
        const val HELLO_BIKE_SCAN = HELLO_BIKE + SCAN


        const val JINGDONG = "jingdong"


        const val MEITUAN = "meituan"
        const val MEITUAN_SCAN = MEITUAN + SCAN
        const val MEITUAN_PAY_CODE = MEITUAN + PAY_CODE


        const val NETEASE_CLOUDMUSIC = "netease_cloudmusic"


        const val WECHAT = "wechat"
        const val WECHAT_SCAN = WECHAT + SCAN
        const val WECHAT_PAY_CODE = WECHAT + PAY_CODE
        const val WECHAT_MY_CARD = WECHAT + MY_CARD


        const val YUNSHANFU = "yunshanfu"
        const val YUNSHANFU_SCAN = YUNSHANFU + SCAN
        const val YUNSHANFU_PAY_CODE = YUNSHANFU + PAY_CODE
        const val YUNSHANFU_RIDE_CODE = YUNSHANFU + RIDE_CODE


        const val ALIPAY = "alipay"
        const val ALIPAY_SCAN = ALIPAY + SCAN
        const val ALIPAY_PAY_CODE = ALIPAY + PAY_CODE
        const val ALIPAY_COLLECT_CODE = ALIPAY + COLLECT_CODE
        const val ALIPAY_RIDE_CODE = ALIPAY + RIDE_CODE


        const val QQ = "qq"


        const val QQ_MUSIC = "qq_music"


        const val SYSTEM_UI_DEMO_MODE = ToolID.SystemTool.SYSTEM_UI_DEMO_MODE


        const val ANDROID_EASTER_EGG = ToolID.SystemTool.ANDROID_EASTER_EGG
    }
}