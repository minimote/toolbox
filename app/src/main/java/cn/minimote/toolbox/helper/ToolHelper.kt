/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.constant.Collator
import cn.minimote.toolbox.constant.DeviceType
import cn.minimote.toolbox.constant.ToolList.GroupNameId
import cn.minimote.toolbox.constant.ToolList.GroupType
import cn.minimote.toolbox.constant.ToolList.allDevice
import cn.minimote.toolbox.constant.ToolList.phoneDevice
import cn.minimote.toolbox.constant.ToolList.watchDevice
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.viewModel.MyViewModel


object ToolHelper {


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