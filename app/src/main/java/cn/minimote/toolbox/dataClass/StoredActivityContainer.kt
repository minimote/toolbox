/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass


// 存储活动数据容器，包含数据版本和实际的数据列表
data class StoredActivityContainer(
    var version: Int,
    var data: MutableList<StoredActivity>,
)
