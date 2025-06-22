/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.dataClass


data class InstalledActivity(
    val name: String,
    val packageName: String,
    val activityName: String,
    var iconKey: String = activityName,
    val id: String = activityName,
) {
    // 将安装类型的活动转换为存储类型
    fun toStoredActivity(
        width: Int,
    ): StoredActivity {
        return StoredActivity(
            name = name,
            packageName = packageName,
            activityName = activityName,
            width = width,
            iconKey = iconKey,
            id = id,
        )
    }

//    fun List<InstalledActivity>.distinctWithOptionalActivity(): List<InstalledActivity> {
//        val seen = mutableSetOf<Pair<String, String>>()
//        val result = mutableListOf<InstalledActivity>()
//
//        for(item in this) {
//            if(item.activityName != null) {
//                val key = item.packageName to item.activityName
//                if(!seen.contains(key)) {
//                    seen.add(key)
//                    result.add(item)
//                }
//            } else {
//                // activityName == null 时直接保留
//                result.add(item)
//            }
//        }
//
//        return result
//    }

}


