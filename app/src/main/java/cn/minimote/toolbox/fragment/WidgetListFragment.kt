/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.WidgetListAdapter
import cn.minimote.toolbox.data_class.StoredActivity
import cn.minimote.toolbox.view_model.ToolboxViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WidgetListFragment(
    private val viewModel: ToolboxViewModel,
    private val fragmentManager: FragmentManager,
) : Fragment() {

//    private val viewModel: ActivityViewModel by viewModels()

    companion object {
        private const val TAG = "WidgetListFragment"
    }

    private lateinit var context: Context
    private lateinit var imageViewEditBackground: ImageView
    private lateinit var textViewNoWidget: TextView
    private lateinit var recyclerView: RecyclerView

    // 主视图小组件的列数
    private val spanCount = viewModel.spanCount

    // 存储观察者引用
    private lateinit var activityListObserver: Observer<List<StoredActivity>>
    private lateinit var isEditModeObserver: Observer<Boolean>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.i("WidgetListFragment", "onCreate")
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
//        Log.i(TAG, "onCreateView")
        imageViewEditBackground = view.findViewById(R.id.edit_background)
        textViewNoWidget = view.findViewById(R.id.textView_no_widget)
        imageViewEditBackground.layoutParams.width = (0.8 * viewModel.screenWidth).toInt()
        imageViewEditBackground.layoutParams.height = (0.8 * viewModel.screenWidth).toInt()

        viewModel.loadStorageActivities()
        recyclerView = view.findViewById(R.id.recyclerView_widget_list)
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        val adapter = WidgetListAdapter(
            context = requireActivity(),
            viewModel = viewModel,
            fragmentManager = fragmentManager,
        )
        recyclerView.adapter = adapter
//        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//            override fun getSpanSize(position: Int): Int {
//                return spanCount / (viewModel.storedActivityList.value?.get(position)?.widgetSize
//                    ?: 1)
//            }
//        }
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return viewModel.storedActivityList.value?.get(position)?.widgetSize
                    ?: viewModel.maxWidgetSize
            }
        }
        recyclerView.layoutManager = gridLayoutManager

        // 恢复 RecyclerView 的滑动位置
//        if(savedInstanceState != null) {
//            Log.i("WidgetListFragment", "恢复滚动位置")
//            val position = savedInstanceState.getInt("scroll_position", 0)
//            gridLayoutManager.scrollToPosition(position)
//        }

//        // 添加分割线
//        recyclerView.addItemDecoration(
//            DividerItemDecoration(requireContext())
//        )
        // 设置 ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                val fromItem = adapter.activityList[fromPosition]
                adapter.activityList.removeAt(fromPosition)
                adapter.activityList.add(toPosition, fromItem)
                adapter.notifyItemMoved(fromPosition, toPosition)
                viewModel.storedActivityListWasModified()
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 不需要实现滑动删除
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
        adapter.setItemTouchHelper(itemTouchHelper)


        activityListObserver = Observer { activityList ->
            if(activityList.isEmpty()) {
                textViewNoWidget.visibility = View.VISIBLE
            } else {
                textViewNoWidget.visibility = View.GONE
            }
            adapter.submitList()
        }
        viewModel.storedActivityList.observe(viewLifecycleOwner, activityListObserver)


        isEditModeObserver = Observer { isEditMode ->
            if(isEditMode) {
                imageViewEditBackground.visibility = View.VISIBLE
            } else {
                // 退出编辑模式
                imageViewEditBackground.visibility = View.GONE
//                if(adapter.activityList.size > 1) {
//                    Log.i(TAG, "adapter[1]: ${adapter.activityList[1]}")
//                    Log.i(TAG, "viewModel[1]: ${viewModel.storedActivityList.value?.get(1)}")
//                }
                // 返回时可能没有保存，恢复原始数据
                viewModel.restoreOriginStoredActivityList()
                adapter.submitList()
            }
        }
        viewModel.editMode.observe(viewLifecycleOwner, isEditModeObserver)

        return view
    }


    // 移除观察者
    override fun onDestroyView() {
        super.onDestroyView()
//        Log.i(TAG, "onDestroyView")
        viewModel.storedActivityList.removeObserver(activityListObserver)
        viewModel.editMode.removeObserver(isEditModeObserver)
    }
}
