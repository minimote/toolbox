/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
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
import cn.minimote.toolbox.adapter.AboutProjectAdapter
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AboutProjectFragment : Fragment() {

    private val viewModel: ToolboxViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AboutProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_about_project, container, false)

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_about_project)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AboutProjectAdapter(
            context = requireContext(),
            viewModel = viewModel,
        )
        recyclerView.adapter = adapter

        return view
    }
}