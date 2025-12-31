/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.BackupAndRecoveryAdapter
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class BackupAndRecoveryFragment : BaseShadowListFragment() {


    override fun getLayoutManager(): RecyclerView.LayoutManager {
        // 使用 FlexboxLayoutManager 实现自适应列数
        val flexboxLayoutManager = FlexboxLayoutManager(context)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexboxLayoutManager.alignItems = AlignItems.CENTER
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行

        return flexboxLayoutManager
    }


    override fun getAdapter(): RecyclerView.Adapter<*> {
        return BackupAndRecoveryAdapter(
            myActivity = requireActivity() as MainActivity,
            viewModel = viewModel,
        )
    }

}
