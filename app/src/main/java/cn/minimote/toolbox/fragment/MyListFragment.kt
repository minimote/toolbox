/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.MyListAdapter


class MyListFragment() : BaseShadowListFragment() {


    override fun getAdapter(): RecyclerView.Adapter<*> {
        return MyListAdapter(
            myActivity = requireActivity() as MainActivity,
            viewModel = viewModel,
        )
    }


//    private val viewModel: MyViewModel by activityViewModels()
//
//    private val myActivity get() = requireActivity() as MainActivity
//
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: MyListAdapter
//
//    private lateinit var fragmentNameObserver: Observer<String>


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }


//    override fun onDestroy() {
//        super.onDestroy()
//
//    }


//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        val view = inflater.inflate(
//            R.layout.layout_shadow_constraintlayout_recyclerview,
//            container,
//            false
//        ) as ShadowConstraintLayout
//
//        // 初始化 RecyclerView
//        recyclerView = view.recyclerView
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        adapter = MyListAdapter(
//            myActivity = myActivity,
//            viewModel = viewModel,
//        )
//        recyclerView.adapter = adapter
//        view.setShadow(viewModel = viewModel, addBottomPadding = false)
//
////        // 设置观察者
////        setObservers()
//
//        return view
//    }


//    override fun onDestroyView() {
//        super.onDestroyView()
//        // 移除观察者
//        removeObservers()
//    }
//
//
////    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
////        super.onViewCreated(view, savedInstanceState)
////        // 刷新数据
//////        refreshData()
////    }
//
//    override fun onResume() {
//        super.onResume()
//        // 刷新数据
//        refreshData()
//    }
//
////    override fun onDestroyView() {
////        super.onDestroyView()
////        removeObserver()
////    }
//
//    // 设置观察者
//    private fun setObservers() {
//        fragmentNameObserver = Observer { fragmentName ->
//            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT) {
//                // 切换回来的时候刷新数据，主要是1
//                refreshData()
//            }
//        }
//        viewModel.fragmentName.observe(viewLifecycleOwner, fragmentNameObserver)
//    }
//
//    // 移除观察者
//    private fun removeObservers() {
//        viewModel.fragmentName.removeObserver(fragmentNameObserver)
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun refreshData() {
//        // 通知适配器数据已更改
//        adapter.notifyDataSetChanged()
//    }
}
