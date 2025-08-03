/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import cn.minimote.toolbox.constant.MenuType.SortOrder.CREATED_TIME_EARLY_TO_LATE
import cn.minimote.toolbox.constant.MenuType.SortOrder.CREATED_TIME_LATE_TO_EARLY
import cn.minimote.toolbox.constant.MenuType.SortOrder.FREE_SORT
import cn.minimote.toolbox.constant.MenuType.SortOrder.LAST_MODIFIED_TIME_EARLY_TO_LATE
import cn.minimote.toolbox.constant.MenuType.SortOrder.LAST_MODIFIED_TIME_LATE_TO_EARLY
import cn.minimote.toolbox.constant.MenuType.SortOrder.LAST_USED_TIME_EARLY_TO_LATE
import cn.minimote.toolbox.constant.MenuType.SortOrder.LAST_USED_TIME_LATE_TO_EARLY
import cn.minimote.toolbox.constant.MenuType.SortOrder.NAME_A_TO_Z
import cn.minimote.toolbox.constant.MenuType.SortOrder.NAME_Z_TO_A
import cn.minimote.toolbox.constant.MenuType.SortOrder.RESTORE_SORT
import cn.minimote.toolbox.constant.MenuType.SortOrder.USE_CNT_LESS_TO_MORE
import cn.minimote.toolbox.constant.MenuType.SortOrder.USE_CNT_MORE_TO_LESS

object MenuType {
    // 创建快捷方式
    const val CREATE_SHORTCUT = 0
    // 添加到主页或从移除
    const val ADD_TO_HOME_OR_REMOVE_FROM_HOME = 1
    // 编辑该组件
    const val EDIT_THIS_WIDGET = 2
    // 多选
    const val MULTI_SELECT = 3
    // 排序
    const val SORT = 4
    // 取消
    const val CANCEL = 5

    // 排序方式
    object SortOrder {
        // 名称排序
        const val NAME_A_TO_Z = 6
        const val NAME_Z_TO_A = 7

        // 使用次数排序
        const val USE_CNT_LESS_TO_MORE = 8
        const val USE_CNT_MORE_TO_LESS = 9

        // 创建时间排序
        const val CREATED_TIME_EARLY_TO_LATE = 10
        const val CREATED_TIME_LATE_TO_EARLY = 11

        // 修改时间排序
        const val LAST_MODIFIED_TIME_EARLY_TO_LATE = 12
        const val LAST_MODIFIED_TIME_LATE_TO_EARLY = 13

        // 最后启动时间排序
        const val LAST_USED_TIME_EARLY_TO_LATE = 14
        const val LAST_USED_TIME_LATE_TO_EARLY = 15

        // 自由排序
        const val FREE_SORT = 16

        // 恢复排序
        const val RESTORE_SORT = 17
    }
    // 所有合法值集合
    val SortOrderSet by lazy {
        setOf(
            FREE_SORT,
            RESTORE_SORT,
            NAME_A_TO_Z, NAME_Z_TO_A,
            USE_CNT_LESS_TO_MORE, USE_CNT_MORE_TO_LESS,
            CREATED_TIME_EARLY_TO_LATE, CREATED_TIME_LATE_TO_EARLY,
            LAST_MODIFIED_TIME_EARLY_TO_LATE, LAST_MODIFIED_TIME_LATE_TO_EARLY,
            LAST_USED_TIME_EARLY_TO_LATE, LAST_USED_TIME_LATE_TO_EARLY
        )
    }


    // 保存图片
    const val SAVE_IMAGE = 18


    // 工具信息
    const val TOOL_DETAIL = 19
    const val WIDGET_DETAIL = 20
}