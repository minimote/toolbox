/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.viewModel.MyViewModel

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
            )
        )
    }


    val phoneDevice: Map<String, Map<Int, List<Tool>>> by lazy {
        mapOf(
            GroupType.MID_GROUP to mapOf(
                // 扫码与支付
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

                    // 抖音
                    Tools.AppTool.Douyin.hotRank,

                    // 京东
                    Tools.AppTool.JD.order,
                    Tools.AppTool.JD.jdBean,

                    // 高德地图
                    Tools.AppTool.AMap.realTimeBus,
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
                // 扫描与支付
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


    // 获取工具列表的方法
    fun getToolListByDeviceType(
        viewModel: MyViewModel,
        deviceType: String,
        // 是否筛选工具
        filterTool: Boolean = true,
    ): List<ExpandableGroup> {
        return when(deviceType) {
            DeviceType.PHONE -> {
                getMergedGroupList(
                    viewModel = viewModel,
                    baseDevice = allDevice,
                    specificDevice = phoneDevice,
//                    filterTool = filterTool,
                )
            }

            DeviceType.WATCH -> {
                getMergedGroupList(
                    viewModel = viewModel,
                    baseDevice = allDevice,
                    specificDevice = watchDevice,
//                    filterTool = filterTool,
                )
            }

            else -> {
                listOf()
            }
        }
    }

    // 合并所有组的列表
    private fun getMergedGroupList(
        viewModel: MyViewModel,
        baseDevice: Map<String, Map<Int, List<Tool>>>,
        specificDevice: Map<String, Map<Int, List<Tool>>>,
//        filterTool: Boolean = true,
    ): List<ExpandableGroup> {
        return listOf(
            GroupType.TOP_GROUP,
            GroupType.MID_GROUP,
            GroupType.BOTTOM_GROUP,
        ).flatMap { groupType ->
            getExpandableGroupList(
                viewModel = viewModel,
                map1 = baseDevice[groupType],
                map2 = specificDevice[groupType],
//                filterTool = filterTool,
            )
        }
    }


    // 获取 ExpandableGroup 列表
    private fun getExpandableGroupList(
        viewModel: MyViewModel,
        map1: Map<Int, List<Tool>>?,
        map2: Map<Int, List<Tool>>?,
//        filterTool: Boolean = true,
    ): List<ExpandableGroup> {
        val mergeMap = mutableMapOf<Int, MutableList<Tool>>()

        // 合并两个map中的数据
        sequenceOf(map1, map2).forEach { map ->
            map?.forEach { (titleId, toolList) ->
                mergeMap.getOrPut(titleId) { mutableListOf() }.addAll(toolList)
            }
        }

//        val hideUninstalledTools = filterTool && !(viewModel.getConfigValue(
//            Config.ConfigKeys.Display.SHOW_UNAVAILABLE_TOOLS
//        ) as Boolean)

        // 创建 ExpandableGroup 对象并排序
        return mergeMap.mapNotNull { (titleId, toolList) ->
//            // 过滤掉未安装的应用
//            val installedToolList = toolList.filter { tool ->
//                val available = LaunchHelper.isToolAvailable(
//                    context = viewModel.myContext, tool = tool,
//                )
//                if(!available) {
//                    tool.iconKey = Icon.IconKey.UNAVAILABLE
//                }
//                // 某些工具可能不需要检查安装状态（如Fragment类型）
//                !hideUninstalledTools || available
//            }

            // 对工具列表去重并按名称排序
            val distinctSortedToolList = toolList.distinct().sortedWith(
                compareBy(Collator.chineseCollator) { it.name })


//            var toolListWithSeparators = mutableListOf<Tool>()
//            // 在不同包名的应用工具之间插入空白工具
//            if(titleId == GroupNameId.appTool) {
//                var lastPackageName: String? = null
//
//                distinctSortedToolList.forEach { tool ->
//                    // 如果当前工具的包名与上一个不同，且不是第一个工具，则插入空白工具
//                    if(lastPackageName != null && lastPackageName != tool.packageName) {
//                        toolListWithSeparators.add(Tools.blank)
//                    }
//                    toolListWithSeparators.add(tool)
//                    lastPackageName = tool.packageName
//                }
//            } else {
//                toolListWithSeparators = distinctSortedToolList.toMutableList()
//            }


            // 如果工具列表为空，则不创建ExpandableGroup
            if(//hideUninstalledTools
//                &&
                    titleId != GroupNameId.addLocalApp
                && distinctSortedToolList.isEmpty()
            ) {
                null
            } else {
                ExpandableGroup(
                    titleString = viewModel.myContext.getString(titleId),
                    dataList = distinctSortedToolList,
                )
            }
        }.sortedWith(
            compareBy(Collator.chineseCollator) { it.titleString })
    }


}