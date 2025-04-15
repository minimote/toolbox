/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.SettingAdapter
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingFragment : Fragment() {

    private val viewModel: ToolboxViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SettingAdapter


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    private fun getHash(viewModel: ToolboxViewModel): Int {
//        return System.identityHashCode(viewModel)
//    }


//    override fun onDestroy() {
//        super.onDestroy()
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        viewModel.settingWasModified.value = false
        ConfigHelper.restoreUserConfig(viewModel)

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_setting)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = SettingAdapter(
            context = requireContext(),
            viewModel = viewModel,
        )
        recyclerView.adapter = adapter
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ConfigHelper.restoreUserConfig(viewModel)
    }

}
