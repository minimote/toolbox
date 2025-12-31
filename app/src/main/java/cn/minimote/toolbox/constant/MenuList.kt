/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object MenuList {
    // 工具的弹出菜单
    val tool by lazy {
        listOf(
            MenuType.TOP,
            // 添加到主页或从移除
            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME,
            // 创建快捷方式
            MenuType.CREATE_SHORTCUT,
            // 工具信息
            MenuType.TOOL_DETAIL,
            // 保存图标
            MenuType.SAVE_ICON,
            // 取消
            MenuType.CANCEL,
        )
    }
    val tool_watch by lazy {
        tool.filter { it != MenuType.CREATE_SHORTCUT }
    }

    // 桌面组件的弹出菜单
    val widget by lazy {
        listOf(
            MenuType.TOP,
            // 添加到主页或从移除
            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME,
            // 创建快捷方式
            MenuType.CREATE_SHORTCUT,
            // 编辑工具
            MenuType.EDIT_THIS_WIDGET,
            // 工具信息
            MenuType.WIDGET_DETAIL,
            // 多选
            MenuType.MULTI_SELECT,
            // 排序
            MenuType.SORT,
            // 保存图标
            MenuType.SAVE_ICON,
            // 取消
            MenuType.CANCEL,
        )
    }
    val widget_watch by lazy {
        widget.filter { it != MenuType.CREATE_SHORTCUT }
    }

    // 排序的菜单
    val sort by lazy {
        listOf(
            MenuType.TOP,
            MenuType.SortOrder.RESTORE_SORT,
            MenuType.SortOrder.FREE_SORT,
            // 启动次数
            MenuType.SortOrder.USE_CNT_LESS_TO_MORE,
            MenuType.SortOrder.USE_CNT_MORE_TO_LESS,
            // 名称
            MenuType.SortOrder.NAME_A_TO_Z,
            MenuType.SortOrder.NAME_Z_TO_A,
            // 最后启动时间
            MenuType.SortOrder.LAST_USED_TIME_EARLY_TO_LATE,
            MenuType.SortOrder.LAST_USED_TIME_LATE_TO_EARLY,
            // 修改时间
            MenuType.SortOrder.LAST_MODIFIED_TIME_EARLY_TO_LATE,
            MenuType.SortOrder.LAST_MODIFIED_TIME_LATE_TO_EARLY,
            // 创建时间
            MenuType.SortOrder.CREATED_TIME_EARLY_TO_LATE,
            MenuType.SortOrder.CREATED_TIME_LATE_TO_EARLY,
            // 取消
            MenuType.CANCEL,
        )
    }


    // 保存图片
    val saveImage by lazy {
        listOf(
            MenuType.TOP,
            MenuType.SAVE_IMAGE,
            MenuType.CANCEL,
        )
    }


    // 长按菜单
    val longPress by lazy {
        listOf(
            MenuType.TOP,
            MenuType.LongPress.MOVE_TO_TOP,
            MenuType.LongPress.MOVE_TO_BOTTOM,
            MenuType.LongPress.REMOVE,
            MenuType.CANCEL,
        )
    }
}