/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object ToolMap {
    val idToTool by lazy {
        mapOf(
            ToolID.AppTool.WeChat.SCAN to Tools.AppTool.WeChat.scan,
            ToolID.AppTool.WeChat.PAY_CODE to Tools.AppTool.WeChat.payCode,
            ToolID.AppTool.WeChat.MY_CARD to Tools.AppTool.WeChat.myCard,

            ToolID.AppTool.Alipay.SCAN to Tools.AppTool.Alipay.scan,
            ToolID.AppTool.Alipay.PAY_CODE to Tools.AppTool.Alipay.payCode,
            ToolID.AppTool.Alipay.PAY_CODE_WATCH to Tools.AppTool.Alipay.payCodeWatch,
            ToolID.AppTool.Alipay.COLLECT_CODE to Tools.AppTool.Alipay.collectCode,
            ToolID.AppTool.Alipay.RIDE_CODE to Tools.AppTool.Alipay.rideCode,
            ToolID.AppTool.Alipay.SUPPORT_AUTHOR to Tools.AppTool.Alipay.supportAuthor,
            ToolID.AppTool.Alipay.PHONE_RECHARGE to Tools.AppTool.Alipay.phoneRecharge,
            ToolID.AppTool.Alipay.BILL to Tools.AppTool.Alipay.bill,
            ToolID.AppTool.Alipay.BANK_CARD to Tools.AppTool.Alipay.bankCard,
            ToolID.AppTool.Alipay.BALANCE to Tools.AppTool.Alipay.balance,
            ToolID.AppTool.Alipay.BALANCE_BANK to Tools.AppTool.Alipay.balanceBank,
            ToolID.AppTool.Alipay.TRANSFER to Tools.AppTool.Alipay.transfer,
            ToolID.AppTool.Alipay.STOCK to Tools.AppTool.Alipay.stock,
            ToolID.AppTool.Alipay.MEMBER to Tools.AppTool.Alipay.member,
            ToolID.AppTool.Alipay.CONTACTS to Tools.AppTool.Alipay.contacts,
            ToolID.AppTool.Alipay.BOOKKEEPING to Tools.AppTool.Alipay.bookkeeping,
            ToolID.AppTool.Alipay.LIFE_PAYMENT to Tools.AppTool.Alipay.lifePayment,
            ToolID.AppTool.Alipay.HUABEI to Tools.AppTool.Alipay.huabei,
            ToolID.AppTool.Alipay.GOLD to Tools.AppTool.Alipay.gold,
            ToolID.AppTool.Alipay.TOTAL_ASSETS to Tools.AppTool.Alipay.totalAssets,
            ToolID.AppTool.Alipay.MY_EXPRESS to Tools.AppTool.Alipay.myExpress,
            ToolID.AppTool.Alipay.DIDI to Tools.AppTool.Alipay.didi,
            ToolID.AppTool.Alipay.FUND to Tools.AppTool.Alipay.fund,
            ToolID.AppTool.Alipay.SMART_ASSISTANT to Tools.AppTool.Alipay.smartAssistant,
            ToolID.AppTool.Alipay.ANT_FOREST to Tools.AppTool.Alipay.antForest,
            ToolID.AppTool.Alipay.ANT_MANOR to Tools.AppTool.Alipay.antManor,
            ToolID.AppTool.Alipay.MERCHANT_SERVICE to Tools.AppTool.Alipay.merchantService,
            ToolID.AppTool.Alipay.BIKE_SHARING to Tools.AppTool.Alipay.bikeSharing,
            ToolID.AppTool.Alipay.RED_PACKET to Tools.AppTool.Alipay.redPacket,
            ToolID.AppTool.Alipay.LOTTERY to Tools.AppTool.Alipay.lottery,
            ToolID.AppTool.Alipay.CREDIT_CARD_REPAYMENT to Tools.AppTool.Alipay.creditCardRepayment,

            ToolID.AppTool.YunShanFu.SCAN to Tools.AppTool.YunShanFu.scan,
            ToolID.AppTool.YunShanFu.PAY_CODE to Tools.AppTool.YunShanFu.payCode,
            ToolID.AppTool.YunShanFu.CREDIT_CARD_REPAYMENT to Tools.AppTool.YunShanFu.creditCardRepayment,
            ToolID.AppTool.YunShanFu.RIDE_CODE to Tools.AppTool.YunShanFu.rideCode,
            ToolID.AppTool.YunShanFu.SIGN_IN to Tools.AppTool.YunShanFu.signIn,

            ToolID.SystemTool.DEVELOPER_OPTION to Tools.SystemTool.developerOption,
            ToolID.SystemTool.DEVELOPER_OPTION_PIXEL to Tools.SystemTool.developerOptionPixel,
            ToolID.SystemTool.ACCESSIBILITY_OPTION to Tools.SystemTool.accessibilityOption,
            ToolID.SystemTool.SYSTEM_UI_DEMO_MODE to Tools.SystemTool.systemUIDemoMode,
            ToolID.SystemTool.RECENT_TASK to Tools.SystemTool.recentTask,
            ToolID.SystemTool.ANDROID_EASTER_EGG to Tools.SystemTool.androidEasterEgg,

            ToolID.AppTool.QQ.ZONE to Tools.AppTool.QQ.zone,
            ToolID.AppTool.QQ.PROFILE to Tools.AppTool.QQ.profile,

            ToolID.AppTool.Bilibili.MY_COLLECTION to Tools.AppTool.Bilibili.myCollection,
            ToolID.AppTool.Bilibili.OFFLINE_CACHE to Tools.AppTool.Bilibili.offlineCache,

            ToolID.AppTool.NetEaseCloudMusic.DAILY_RECOMMEND to Tools.AppTool.NetEaseCloudMusic.dailyRecommend,
            ToolID.AppTool.NetEaseCloudMusic.PRIVATE_FM to Tools.AppTool.NetEaseCloudMusic.privateFM,

            ToolID.AppTool.QQMusic.DAILY_RECOMMEND to Tools.AppTool.QQMusic.dailyRecommend,
            ToolID.AppTool.QQMusic.MY_COLLECTION to Tools.AppTool.QQMusic.myCollection,
            ToolID.AppTool.QQMusic.PERSONAL_RADIO to Tools.AppTool.QQMusic.personalRadio,
            ToolID.AppTool.QQMusic.PLAY_HOT_SONG to Tools.AppTool.QQMusic.playHotSong,
            ToolID.AppTool.QQMusic.POINT_CENTER to Tools.AppTool.QQMusic.pointCenter,
            ToolID.AppTool.QQMusic.RECOGNIZE to Tools.AppTool.QQMusic.recognize,

            ToolID.AppTool.Douyin.HOT_RANK to Tools.AppTool.Douyin.hotRank,

            ToolID.AppTool.Jingdong.ORDER to Tools.AppTool.JingDong.order,
            ToolID.AppTool.Jingdong.JD_BEAN to Tools.AppTool.JingDong.jdBean,

            ToolID.AppTool.AMap.REAL_TIME_BUS to Tools.AppTool.AMap.realTimeBus,

            ToolID.AppTool.Meituan.SCAN to Tools.AppTool.Meituan.scan,
            ToolID.AppTool.Meituan.PAY_CODE to Tools.AppTool.Meituan.payCode,
            ToolID.AppTool.Meituan.BIKE to Tools.AppTool.Meituan.bike,
            ToolID.AppTool.Meituan.SEARCH to Tools.AppTool.Meituan.search,
            ToolID.AppTool.Meituan.ORDER to Tools.AppTool.Meituan.order,
            ToolID.AppTool.Meituan.COLLECTION to Tools.AppTool.Meituan.collection,
            ToolID.AppTool.Meituan.FOOD to Tools.AppTool.Meituan.food,
            ToolID.AppTool.Meituan.TAKEOUT to Tools.AppTool.Meituan.takeout,
            ToolID.AppTool.Meituan.HOME to Tools.AppTool.Meituan.home,
            ToolID.AppTool.Meituan.HOTEL to Tools.AppTool.Meituan.hotel,

            ToolID.AppTool.HelloBike.SCAN to Tools.AppTool.HelloBike.scan,
        )
    }

}
