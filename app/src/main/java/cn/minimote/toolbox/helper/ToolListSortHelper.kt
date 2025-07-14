/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import cn.minimote.toolbox.constant.Collator
import cn.minimote.toolbox.constant.MenuType.SortOrder
import cn.minimote.toolbox.dataClass.StoredTool

object ToolListSortHelper {

    fun sort(activities: MutableList<StoredTool>, sortType: Int): MutableList<StoredTool> {

        return when(sortType) {
            SortOrder.NAME_A_TO_Z -> sortByNameAToZ(activities)
            SortOrder.NAME_Z_TO_A -> sortByNameZToA(activities)

            SortOrder.USE_CNT_LESS_TO_MORE -> sortByUseCntLessToMore(activities)
            SortOrder.USE_CNT_MORE_TO_LESS -> sortByUseCntMoreToLess(activities)

            SortOrder.CREATED_TIME_EARLY_TO_LATE -> sortByCreatedTimeEarlyToLate(activities)
            SortOrder.CREATED_TIME_LATE_TO_EARLY -> sortByCreatedTimeLateToEarly(activities)

            SortOrder.LAST_MODIFIED_TIME_EARLY_TO_LATE -> sortByLastModifiedTimeEarlyToLate(activities)
            SortOrder.LAST_MODIFIED_TIME_LATE_TO_EARLY -> sortByLastModifiedTimeLateToEarly(activities)

            SortOrder.LAST_USED_TIME_EARLY_TO_LATE -> sortByLastUsedTimeEarlyToLate(activities)
            SortOrder.LAST_USED_TIME_LATE_TO_EARLY -> sortByLastUsedTimeLateToEarly(activities)

            else -> activities
        }
    }


    private fun sortByNameAToZ(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareBy(
                Collator.chineseCollator,
                StoredTool::nickname,
            )
        ).toMutableList()
    }


    private fun sortByNameZToA(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareByDescending(
                Collator.chineseCollator,
                StoredTool::nickname,
            )
        ).toMutableList()
    }

    private fun sortByUseCntLessToMore(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareBy(StoredTool::useCount)
        ).toMutableList()
    }


    private fun sortByUseCntMoreToLess(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareByDescending(StoredTool::useCount)
        ).toMutableList()
    }


    private fun sortByCreatedTimeEarlyToLate(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareBy(StoredTool::createdTime)
        ).toMutableList()
    }


    private fun sortByCreatedTimeLateToEarly(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareByDescending(StoredTool::createdTime)
        ).toMutableList()
    }


    private fun sortByLastModifiedTimeEarlyToLate(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareBy(StoredTool::lastModifiedTime)
        ).toMutableList()
    }


    private fun sortByLastModifiedTimeLateToEarly(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareByDescending(StoredTool::lastModifiedTime)
        ).toMutableList()
    }


    private fun sortByLastUsedTimeEarlyToLate(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareBy(StoredTool::lastUsedTime)
        ).toMutableList()
    }


    private fun sortByLastUsedTimeLateToEarly(activities: MutableList<StoredTool>): MutableList<StoredTool> {
        return activities.sortedWith(
            compareByDescending(StoredTool::lastUsedTime)
        ).toMutableList()
    }
}