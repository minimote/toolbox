/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.AboutProjectAdapter


class AboutProjectFragment : BaseShadowListFragment() {


    override fun getAdapter(): RecyclerView.Adapter<*> {
        return AboutProjectAdapter(
            viewModel = viewModel,
            activity = requireActivity() as MainActivity,
        )
    }


}