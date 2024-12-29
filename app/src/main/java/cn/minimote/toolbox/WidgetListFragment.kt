/*
 * Copyright (c) 2024 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class WidgetListFragment : Fragment() {

    private lateinit var widgetListRecyclerView: RecyclerView
    private lateinit var widgetListAdapter: WidgetListAdapter
    private lateinit var iconCacheManager: IconCacheManager
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("WidgetListFragment", "onCreateView")
        val view = inflater.inflate(R.layout.fragment_widget_list, container, false)


        // 初始化 IconCacheManager
        iconCacheManager = IconCacheManager(requireContext())


        // 初始化 RecyclerView
        widgetListRecyclerView = view.findViewById(R.id.recyclerView_widget_list)
        widgetListRecyclerView.layoutManager = LinearLayoutManager(context)

        // 在后台线程获取存储的应用列表
        uiScope.launch {

            widgetListRecyclerView.adapter = widgetListAdapter
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}