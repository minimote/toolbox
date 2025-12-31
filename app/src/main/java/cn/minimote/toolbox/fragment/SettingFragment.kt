/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment


import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.adapter.SettingAdapter
import cn.minimote.toolbox.helper.ConfigHelper


class SettingFragment : BaseShadowListFragment() {


    override fun getAdapter(): RecyclerView.Adapter<*> {
        return SettingAdapter(
            context = requireContext(),
            viewModel = viewModel,
        )
    }


    override fun someActionOnCreateView() {
        viewModel.configChanged.value = false
        ConfigHelper.restoreUserConfig(viewModel)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        someActionOnCreateView()
    }

}