/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.Tool

// 工具
object ToolList {


    object GroupType {
        const val TOP_GROUP = "top_group"
        const val MID_GROUP = "mid_group"
        const val BOTTOM_GROUP = "bottom_group"
    }

    object GroupNameId {
        val addLocalApp = R.string.add_local_app
        val systemTool = R.string.system_tool
        val appTool = R.string.app_tool
    }


    val allDevice: Map<String, Map<Int, List<Tool>>> by lazy {
        mapOf(
            GroupType.TOP_GROUP to mapOf(
                // 添加本机软件
//                GroupNameId.addLocalApp to listOf(),
            ),

            GroupType.MID_GROUP to mapOf(
                // 系统工具
                GroupNameId.systemTool to listOf(
                    Tools.SystemTool.developerOption, // 开发者选项
                    Tools.SystemTool.developerOptionPixel, // 谷歌Pixel开发者选项
                    Tools.SystemTool.accessibilityOption, // 无障碍选项
                )
            ),

        )
    }


    val phoneDevice: Map<String, Map<Int, List<Tool>>> by lazy {
        mapOf(
            GroupType.MID_GROUP to mapOf(
                // 软件工具
                GroupNameId.appTool to listOf(
                    // 微信
                    Tools.AppTool.WeChat.scan, // 扫一扫
                    Tools.AppTool.WeChat.payCode, // 付款码
                    Tools.AppTool.WeChat.myCard, // 名片码

                    // 支付宝
                    Tools.AppTool.Alipay.scan, // 扫一扫
                    Tools.AppTool.Alipay.payCode, // 付款码
                    Tools.AppTool.Alipay.collectCode, // 收款码
                    Tools.AppTool.Alipay.rideCode, // 乘车码
                    Tools.AppTool.Alipay.phoneRecharge, // 手机充值
                    Tools.AppTool.Alipay.bill, // 账单
                    Tools.AppTool.Alipay.bankCard, // 银行卡
                    Tools.AppTool.Alipay.balance, // 余额
                    Tools.AppTool.Alipay.balanceBank, // 余额宝
                    Tools.AppTool.Alipay.transfer, // 转账
                    Tools.AppTool.Alipay.stock, // 股票
                    Tools.AppTool.Alipay.member, // 会员
                    Tools.AppTool.Alipay.contacts, // 通讯录
                    Tools.AppTool.Alipay.bookkeeping, // 记账
                    Tools.AppTool.Alipay.lifePayment, // 生活缴费
                    Tools.AppTool.Alipay.huabei, // 花呗
                    Tools.AppTool.Alipay.gold, // 黄金
                    Tools.AppTool.Alipay.totalAssets, // 总资产
                    Tools.AppTool.Alipay.myExpress, // 我的快递
                    Tools.AppTool.Alipay.didi, // 滴滴
                    Tools.AppTool.Alipay.fund, // 基金
                    Tools.AppTool.Alipay.smartAssistant, // 智能助手
                    Tools.AppTool.Alipay.antForest, // 蚂蚁森林
                    Tools.AppTool.Alipay.antManor, // 蚂蚁庄园
                    Tools.AppTool.Alipay.merchantService, // 商家服务
                    Tools.AppTool.Alipay.bikeSharing, // 共享单车
                    Tools.AppTool.Alipay.redPacket, // 红包
                    Tools.AppTool.Alipay.lottery, // 彩票
                    Tools.AppTool.Alipay.creditCardRepayment, // 信用卡还款

                    // 云闪付
                    Tools.AppTool.YunShanFu.scan, // 扫一扫
                    Tools.AppTool.YunShanFu.payCode, // 付款码
                    Tools.AppTool.YunShanFu.creditCardRepayment, // 信用卡还款
                    Tools.AppTool.YunShanFu.rideCode, // 乘车码
                    Tools.AppTool.YunShanFu.signIn, // 签到

                    // QQ
                    Tools.AppTool.QQ.zone,
                    Tools.AppTool.QQ.profile,

                    // 哔哩哔哩
                    Tools.AppTool.Bilibili.myCollection,
                    Tools.AppTool.Bilibili.offlineCache,

                    // 网易云音乐
                    Tools.AppTool.NetEaseCloudMusic.dailyRecommend,
                    Tools.AppTool.NetEaseCloudMusic.privateFM,

                    // QQ音乐
                    Tools.AppTool.QQMusic.dailyRecommend,
                    Tools.AppTool.QQMusic.myCollection,
                    Tools.AppTool.QQMusic.personalRadio,
                    Tools.AppTool.QQMusic.playHotSong,
                    Tools.AppTool.QQMusic.pointCenter,
                    Tools.AppTool.QQMusic.recognize,

                    // 抖音
                    Tools.AppTool.Douyin.hotRank,

                    // 京东
                    Tools.AppTool.JingDong.order,
                    Tools.AppTool.JingDong.jdBean,

                    // 高德地图
                    Tools.AppTool.AMap.realTimeBus,

                    // 美团
                    Tools.AppTool.Meituan.scan,
                    Tools.AppTool.Meituan.payCode,
                    Tools.AppTool.Meituan.bike,
                    Tools.AppTool.Meituan.search,
                    Tools.AppTool.Meituan.order,
                    Tools.AppTool.Meituan.collection,
                    Tools.AppTool.Meituan.food,
                    Tools.AppTool.Meituan.takeout,
                    Tools.AppTool.Meituan.home,
                    Tools.AppTool.Meituan.hotel,

                    // 哈啰
                    Tools.AppTool.HelloBike.scan,
                ),

                // 系统工具
                GroupNameId.systemTool to listOf(
                    Tools.SystemTool.systemUIDemoMode, // 系统界面调节
                    Tools.SystemTool.androidEasterEgg, // 安卓彩蛋
                ),
            )
        )
    }


    val watchDevice: Map<String, Map<Int, List<Tool>>> by lazy {
        mapOf(
            GroupType.MID_GROUP to mapOf(
                // 软件工具
                GroupNameId.appTool to listOf(
                    // 支付宝
                    Tools.AppTool.Alipay.payCodeWatch, // 付款码
                ),

                // 系统工具
                GroupNameId.systemTool to listOf(
                    Tools.SystemTool.recentTask, // 最近任务
                ),
            )
        )
    }


}