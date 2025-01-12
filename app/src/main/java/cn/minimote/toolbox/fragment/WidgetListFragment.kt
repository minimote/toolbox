/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.WidgetListAdapter
import cn.minimote.toolbox.data_class.StoredActivity
import cn.minimote.toolbox.others.DividerItemDecoration
import cn.minimote.toolbox.view_model.ActivityViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WidgetListFragment(
    private val viewModel: ActivityViewModel,
    private val fragmentManager: FragmentManager,
) : Fragment() {

//    private val viewModel: ActivityViewModel by viewModels()

    companion object {
        private const val TAG = "WidgetListFragment"
    }

    private lateinit var context: Context
    private lateinit var editBackground: ImageView
    private lateinit var recyclerView: RecyclerView

    // 主视图小组件的列数
    private val spanCount = viewModel.spanCount

    // 存储观察者引用
    private lateinit var activityListObserver: Observer<List<StoredActivity>>
    private lateinit var isEditModeObserver: Observer<Boolean>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("WidgetListFragment", "onCreate")
        context = requireContext()
////        viewModel = ViewModelProvider(this)[WidgetListViewModel::class.java]
//        Log.i("WidgetListFragment", "viewModel:${System.identityHashCode(viewModel)}")
    }


//    override fun onDestroy() {
//        super.onDestroy()
//    }


//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Log.i(TAG, "onSaveInstanceState")
//        // 保存 RecyclerView 的滚动位置
//        val layoutManager = recyclerView.layoutManager as GridLayoutManager
//        outState.putInt("scroll_position", layoutManager.findFirstVisibleItemPosition())
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_widget_list, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
        Log.i(TAG, "onCreateView")
        editBackground = view.findViewById(R.id.edit_background)

        viewModel.loadStorageActivities()
        recyclerView = view.findViewById(R.id.recyclerView_widget_list)
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        val adapter = WidgetListAdapter(
            context = requireActivity(),
            viewModel = viewModel,
            fragmentManager = fragmentManager
        )
        recyclerView.adapter = adapter
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spanCount / (viewModel.storedActivityList.value?.get(position)?.widgetSize
                    ?: 1)
            }
        }
        recyclerView.layoutManager = gridLayoutManager

        // 恢复 RecyclerView 的滑动位置
//        if(savedInstanceState != null) {
//            Log.i("WidgetListFragment", "恢复滚动位置")
//            val position = savedInstanceState.getInt("scroll_position", 0)
//            gridLayoutManager.scrollToPosition(position)
//        }

        // 添加分割线
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext())
        )


        activityListObserver = Observer {
            adapter.submitList()
        }
        viewModel.storedActivityList.observe(viewLifecycleOwner, activityListObserver)
        isEditModeObserver = Observer { isEditMode ->
            if(isEditMode) {
                editBackground.visibility = View.VISIBLE
            } else {
                editBackground.visibility = View.GONE
            }
        }
        viewModel.isEditMode.observe(viewLifecycleOwner, isEditModeObserver)

        return view
    }


    // 移除观察者
    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView")
        viewModel.storedActivityList.removeObserver(activityListObserver)
        viewModel.isEditMode.removeObserver(isEditModeObserver)
    }
}
