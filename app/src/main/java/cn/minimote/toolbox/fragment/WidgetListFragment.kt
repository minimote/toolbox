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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.WidgetListAdapter
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import cn.minimote.toolbox.viewModel.ToolboxViewModel.Companion.FragmentNames
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WidgetListFragment(
//    private val viewModel: ToolboxViewModel,
    val viewPager: ViewPager2,
    val constraintLayoutOrigin: ConstraintLayout,
) : Fragment() {

    private val viewModel: ToolboxViewModel by activityViewModels()
//    val viewModel: ToolboxViewModel = ViewModelProvider(this)[ToolboxViewModel::class.java]
//    private val viewModel: ToolboxViewModel by viewModels()

    private lateinit var context: Context
    private lateinit var imageViewEditBackground: ImageView
    private lateinit var textViewNoWidget: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WidgetListAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    // 主视图小组件的列数
    private var spanCount = 0


    // 观察者
//    private lateinit var widgetListOrderWasModifiedObserver: Observer<Boolean>
//    private lateinit var storedActivityListObserver: Observer<Boolean>
    private lateinit var fragmentNameObserver: Observer<String>
    private lateinit var widgetListWasSortedObserver: Observer<Boolean>
    private lateinit var widgetListSizeWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetWasModifiedObserver: Observer<Boolean>

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
//        Log.d("WidgetListFragment", "onCreateView")
        spanCount = viewModel.spanCount

        val view = inflater.inflate(R.layout.fragment_widget_list, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
//        Log.i(TAG, "onCreateView")
        textViewNoWidget = view.findViewById(R.id.textView_no_widget)

        // 设置编辑模式的背景
        setEditModeBackground(view)

        // 设置 RecyclerView
        setRecyclerView(view)

        // 设置 ItemTouchHelper
        setItemTouchHelper()

        // 设置观察者
        setObservers()

        return view
    }


    // 设置编辑模式的背景
    private fun setEditModeBackground(view: View) {
        val imageViewSize = viewModel.imageSize
        imageViewEditBackground = view.findViewById(R.id.edit_background)
        imageViewEditBackground.layoutParams.width = imageViewSize
        imageViewEditBackground.layoutParams.height = imageViewSize
    }


    // 设置 RecyclerView
    private fun setRecyclerView(view: View) {
        viewModel.loadStorageActivities()
        recyclerView = view.findViewById(R.id.recyclerView_widget_list)

        adapter = WidgetListAdapter(
            context = requireActivity(),
            viewModel = viewModel,
            fragment = this,
            fragmentManager = requireActivity().supportFragmentManager,
        )
        recyclerView.adapter = adapter


        // 使用 GridLayoutManager
        val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.activityList[position].width
            }
        }
        recyclerView.layoutManager = gridLayoutManager


//        // 使用 StaggeredGridLayoutManager
//        val staggeredGridLayoutManager =
//            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
//
//        recyclerView.layoutManager = staggeredGridLayoutManager


//        // 使用 FlexboxLayoutManager
//        val flexboxLayoutManager = FlexboxLayoutManager(context)
//        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
//        flexboxLayoutManager.flexDirection = FlexDirection.ROW
//        flexboxLayoutManager.alignItems = AlignItems.CENTER
//        flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
//        recyclerView.layoutManager = flexboxLayoutManager


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
                VibrationHelper.vibrateOnClick(context, viewModel)
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
        // 顺序改变就刷新列表会导致拖动异常
//        widgetListOrderWasModifiedObserver = Observer { widgetListOrderWasModified ->
////            Log.e("顺序观察者", "$widgetListOrderWasModified")
//            if(widgetListOrderWasModified) {
////                updateData()
//            }
//        }
//        viewModel.widgetListOrderWasModified.observe(
//            viewLifecycleOwner,
//            widgetListOrderWasModifiedObserver
//        )

//        storedActivityListObserver = Observer {
//            updateData()
//        }
//        viewModel.storedActivityList.observe(viewLifecycleOwner, storedActivityListObserver)
        fragmentNameObserver = Observer { fragmentName ->
            if(fragmentName == FragmentNames.WIDGET_LIST_FRAGMENT) {
                updateData()
            }
        }
        viewModel.fragmentName.observe(viewLifecycleOwner, fragmentNameObserver)

        widgetListWasSortedObserver = Observer { widgetListWasSorted ->
            if(widgetListWasSorted) {
                updateData()
                viewModel.widgetListWasSorted.value = false
            }
        }
        viewModel.widgetListWasSorted.observe(viewLifecycleOwner, widgetListWasSortedObserver)


        widgetListSizeWasModifiedObserver = Observer { widgetListSizeWasModified ->
//            Log.e("大小观察者", "$widgetListSizeWasModified")
//            Toast.makeText(
//                context, "大小观察者:$widgetListSizeWasModified", Toast.LENGTH_SHORT
//            ).show()
            if(widgetListSizeWasModified) {
                updateData()
            }
        }
        viewModel.widgetListSizeWasModified.observe(
            viewLifecycleOwner, widgetListSizeWasModifiedObserver
        )


        widgetWasModifiedObserver = Observer { widgetWasModified ->
            if(widgetWasModified) {
                updateData()
            }
        }
        viewModel.widgetWasModified.observe(viewLifecycleOwner, widgetWasModifiedObserver)


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
                updateData()
            }
        }
        viewModel.editMode.observe(viewLifecycleOwner, isEditModeObserver)
    }


    // 更新数据
    private fun updateData() {
        adapter.submitList()
//        Toast.makeText(
//            context,
//            "更新数据：${viewModel.storedActivityList.value?.size}",
//            Toast.LENGTH_SHORT,
//        ).show()
        showNoWidget()
    }


    // 显示无组件提示
    private fun showNoWidget() {
        if(viewModel.storedActivityList.value?.isEmpty() == true) {
            textViewNoWidget.visibility = View.VISIBLE
        } else {
            textViewNoWidget.visibility = View.GONE
        }
    }


    override fun onResume() {
        super.onResume()
//        Toast.makeText(
//            context,
//            "onResume",
//            Toast.LENGTH_SHORT,
//        ).show()
        // 刷新数据
        updateData()
    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
//        viewModel.widgetListOrderWasModified.removeObserver(widgetListOrderWasModifiedObserver)
        viewModel.widgetListWasSorted.removeObserver(widgetListWasSortedObserver)
        viewModel.widgetListSizeWasModified.removeObserver(widgetListSizeWasModifiedObserver)
        viewModel.widgetWasModified.removeObserver(widgetWasModifiedObserver)
        viewModel.editMode.removeObserver(isEditModeObserver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        Log.i(TAG, "onDestroyView")
        removeObservers()
    }
}
