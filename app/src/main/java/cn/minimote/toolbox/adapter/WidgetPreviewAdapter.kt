/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.viewModel.MyViewModel


class WidgetPreviewAdapter(
    myActivity: MainActivity,
    viewModel: MyViewModel,
) : WidgetListAdapter(
    myActivity = myActivity,
    viewModel = viewModel,
    toolList = mutableListOf(viewModel.editedTool.value!!),
) {


    fun submit() {
        notifyItemChanged(0)
    }

}
