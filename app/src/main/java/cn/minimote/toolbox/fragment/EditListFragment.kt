/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.EditListAdapter


class EditListFragment : BaseShadowListFragment() {


    override fun getAdapter(): RecyclerView.Adapter<*> {
        return EditListAdapter(
            context = requireActivity(),
            viewModel = viewModel,
            lifecycleOwner = viewLifecycleOwner,
            myActivity = requireActivity() as MainActivity,
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.resetToolChanged()
        viewModel.toolListSizeChanged.value = false
    }


    override fun onDestroy() {
        super.onDestroy()

        viewModel.originTool.value = null
        viewModel.editedTool.value = null

        viewModel.resetToolChanged()
        viewModel.toolListSizeChanged.value = false
    }


}
