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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.WidgetListAdapter
import cn.minimote.toolbox.data_class.StoredActivity
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel
import kotlin.math.min


//@AndroidEntryPoint
class WidgetListFragment(
    private val viewModel: ToolboxViewModel,
) : Fragment() {

//    private val viewModel: ToolboxViewModel by activityViewModels()
//    private val viewModel: ToolboxViewModel by viewModels()

    private lateinit var context: Context
    private lateinit var imageViewEditBackground: ImageView
    private lateinit var textViewNoWidget: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WidgetListAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    // 主视图小组件的列数
    private val spanCount = viewModel.spanCount

    // 存储观察者引用
    private lateinit var activityListObserver: Observer<List<StoredActivity>>
    private lateinit var isEditModeObserver: Observer<Boolean>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Log.d("WidgetListFragment", "ViewModel initialized: ${System.identityHashCode(viewModel)}")

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
//        Log.d("WidgetListFragment", "onCreateView")
        val view = inflater.inflate(R.layout.fragment_widget_list, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
//        Log.i(TAG, "onCreateView")
        textViewNoWidget = view.findViewById(R.id.textView_no_widget)

        // 设置编辑模式的背景
        setEditModeBackground(view)

        // 设置 RecyclerView
        setupRecyclerView(view)

        // 设置 ItemTouchHelper
        setItemTouchHelper()

        // 设置观察者
        setObservers()

        return view
    }


    // 设置编辑模式的背景
    private fun setEditModeBackground(view: View) {
        val imageViewSize = (0.8 * min(viewModel.screenWidth, viewModel.screenHeight)).toInt()
        imageViewEditBackground = view.findViewById(R.id.edit_background)
        imageViewEditBackground.layoutParams.width = imageViewSize
        imageViewEditBackground.layoutParams.height = imageViewSize
    }


    // 设置 RecyclerView
    private fun setupRecyclerView(view: View) {
        viewModel.loadStorageActivities()
        recyclerView = view.findViewById(R.id.recyclerView_widget_list)
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        adapter = WidgetListAdapter(
            context = requireActivity(),
            viewModel = viewModel,
            fragment = this,
            fragmentManager = requireActivity().supportFragmentManager,
        )
        recyclerView.adapter = adapter
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
    }


    // 设置 ItemTouchHelper
    private fun setItemTouchHelper() {
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                VibrationHelper.vibrateOnClick(context)
                // 移动后就立马进入编辑模式
                if(viewModel.editMode.value != true) {
                    viewModel.editMode.value = true
                    showEnterEditModeToast()
                }

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                val fromItem = adapter.activityList[fromPosition]

                adapter.activityList.removeAt(fromPosition)
                adapter.activityList.add(toPosition, fromItem)
                adapter.notifyItemMoved(fromPosition, toPosition)
                viewModel.modifyStoredActivityListOrder()

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 不需要实现滑动删除
            }
        })
        adapter.setItemTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    // 进入编辑模式的 Toast
    fun showEnterEditModeToast() {
        Toast.makeText(
            context,
            context.getString(R.string.enter_edit_mode),
            Toast.LENGTH_SHORT,
        ).show()
    }


    // 设置观察者
    private fun setObservers() {
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
                // 进入编辑模式
                imageViewEditBackground.visibility = View.VISIBLE

            } else {
                // 退出编辑模式
                imageViewEditBackground.visibility = View.GONE
                // 只有返回按钮和返回手势才会触发退出编辑模式的 Toast
                // 所以退出编辑模式的 Toast 设置在 MainActivity 中

                // 返回时可能没有保存，恢复原始数据
                viewModel.restoreOriginStoredActivityList()
                adapter.submitList()
            }
        }
        viewModel.editMode.observe(viewLifecycleOwner, isEditModeObserver)
    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.storedActivityList.removeObserver(activityListObserver)
        viewModel.editMode.removeObserver(isEditModeObserver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        Log.i(TAG, "onDestroyView")
        removeObservers()
    }
}
