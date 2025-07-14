/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object MenuList {
    // 工具的弹出菜单
    val tool by lazy {
        listOf(
            // 添加到主页或从移除
            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME,
            // 创建快捷方式
            MenuType.CREATE_SHORTCUT,
            // 取消
            MenuType.CANCEL,
        )
    }

    // 桌面组件的弹出菜单
    val widget by lazy {
        listOf(
            // 添加到主页或从移除
            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME,
            // 创建快捷方式
            MenuType.CREATE_SHORTCUT,
            // 编辑该组件
            MenuType.EDIT_THIS_WIDGET,
            // 多选
            MenuType.MULTI_SELECT,
            // 排序
            MenuType.SORT,
            // 取消
            MenuType.CANCEL,
        )
    }

    // 排序的菜单
    val sort by lazy {
        listOf(
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
            MenuType.SAVE_IMAGE,
            MenuType.CANCEL,
        )
    }
}