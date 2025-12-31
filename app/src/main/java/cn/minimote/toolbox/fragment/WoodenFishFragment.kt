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
import cn.minimote.toolbox.R
import cn.minimote.toolbox.viewModel.MyViewModel


class WoodenFishFragment : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tool_wooden_fish, container, false)

//        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView_edit_list)
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        val adapter = DetailListAdapter(
//            context = requireActivity(),
//            viewModel = viewModel,
//        )
//        recyclerView.adapter = adapter
//
//        val context = requireContext()

        return view
    }

}
