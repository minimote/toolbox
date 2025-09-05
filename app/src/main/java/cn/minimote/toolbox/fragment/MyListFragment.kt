/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.MyListAdapter
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyListFragment() : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    private val myActivity get() = requireActivity() as MainActivity

    val viewPager: ViewPager2
        get() = myActivity.viewPager

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyListAdapter

    private lateinit var fragmentNameObserver: Observer<String>


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }


//    override fun onDestroy() {
//        super.onDestroy()
//
//    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_list, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
//        Log.i("${this::class.simpleName}", "当前：${viewModel.fragmentName.value}")

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MyListAdapter(
            myActivity = myActivity,
            viewModel = viewModel,
        )
        recyclerView.adapter = adapter
//
////        // 添加分割线
////        recyclerView.addItemDecoration(
////            DividerItemDecoration(requireContext())
////        )

        // 设置观察者
        setObservers()

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 移除观察者
        removeObservers()
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // 刷新数据
////        refreshData()
//    }

    override fun onResume() {
        super.onResume()
        // 刷新数据
        refreshData()
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        removeObserver()
//    }

    // 设置观察者
    private fun setObservers() {
        fragmentNameObserver = Observer { fragmentName ->
            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT) {
                // 切换回来的时候刷新数据，主要是从活动列表返回的时候
                refreshData()
            }
        }
        viewModel.fragmentName.observe(viewLifecycleOwner, fragmentNameObserver)
    }

    // 移除观察者
    private fun removeObservers() {
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        // 通知适配器数据已更改
        adapter.notifyDataSetChanged()
    }
}
