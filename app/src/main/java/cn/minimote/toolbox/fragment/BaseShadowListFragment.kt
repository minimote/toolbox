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
import cn.minimote.toolbox.ui.widget.ShadowConstraintLayout
import cn.minimote.toolbox.viewModel.MyViewModel


abstract class BaseShadowListFragment : Fragment() {


    val viewModel: MyViewModel by activityViewModels()

    var addBottomPadding = true


    protected open fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context)
    }
    protected abstract fun getAdapter(): RecyclerView.Adapter<*>
    protected open fun someActionOnCreateView() {}


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.layout_shadow_constraintlayout_recyclerview,
            container,
            false
        ) as ShadowConstraintLayout


        val recyclerView = view.recyclerView

        recyclerView.layoutManager = getLayoutManager()
        recyclerView.adapter = getAdapter()

        view.setShadow(
            viewModel = viewModel,
            addBottomPadding = addBottomPadding,
        )

        someActionOnCreateView()
//        viewModel.checkHashCode()

        return view
    }

}