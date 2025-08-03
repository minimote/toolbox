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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.WidgetListAdapter
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WidgetListFragment() : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    private val activity get() = requireActivity() as MainActivity

    val viewPager: ViewPager2
        get() = activity.viewPager
    val constraintLayoutOrigin: ConstraintLayout
        get() = activity.constraintLayoutOrigin


    private lateinit var context: Context
    private lateinit var imageViewBackground: ImageView
    private lateinit var textViewNoWidget: TextView
    private lateinit var constraintLayoutBackground: ConstraintLayout

    private lateinit var buttonSelectAll: Button
    private lateinit var buttonReverseSelect: Button
    lateinit var buttonSortMode: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WidgetListAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    // 主视图小组件的列数
    private val maxWidgetSize: Int by lazy {
        viewModel.maxWidgetSize
    }

    // 观察者
    private lateinit var fragmentNameObserver: Observer<String>
    private lateinit var widgetListSizeWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetWasModifiedObserver: Observer<Boolean>

    private lateinit var multiSelectModeObserver: Observer<Boolean>
    private lateinit var sortModeObserver: Observer<Boolean>


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        Log.d("WidgetListFragment", "ViewModel initialized: ${System.identityHashCode(viewModel)}")
//
////        Log.i("WidgetListFragment", "onCreate")
//////        viewModel = ViewModelProvider(this)[WidgetListViewModel::class.java]
////        Log.i("WidgetListFragment", "viewModel:${System.identityHashCode(viewModel)}")
//    }


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
        context = requireContext()

        val view = inflater.inflate(R.layout.fragment_widget_list, container, false)

//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
//        Log.i(TAG, "onCreateView")
        // 设置无组件提示
        setNoWidgetShow(view)

        // 设置编辑模式的背景
        setEditModeBackground(view)

        // 设置底部的按钮
        setBottomButton(view)

        // 设置 RecyclerView
        setRecyclerView(view)

        // 设置 ItemTouchHelper
        setItemTouchHelper()

        // 设置观察者
        setObservers()

        return view
    }


    // 设置无组件提示
    private fun setNoWidgetShow(view: View) {
        textViewNoWidget = view.findViewById(R.id.textView_no_widget)
        if(viewModel.isWatch) {
//            textViewNoWidget.textSize = resources.getDimension(R.dimen.text_size_2_footnote)
//            val paddingSize = resources.getDimensionPixelSize(R.dimen.layout_size_3_script)
//            textViewNoWidget.setPadding(
//                paddingSize, paddingSize, paddingSize, paddingSize,
//            )
            textViewNoWidget.visibility = View.GONE
        }
        constraintLayoutBackground = view.findViewById(R.id.constraintLayout_background)
        constraintLayoutBackground.setOnClickListener {
            // 手表没有切换振动，所以需要点击振动
            if(viewModel.isWatch) {
                VibrationHelper.vibrateOnClick(viewModel)
            }
            // 手机已经有切换振动了，所以这里不需要再振动
            viewPager.setCurrentItem(0, true) // 切换到第一页（左边页面）
        }
    }


    // 设置编辑模式的背景
    private fun setEditModeBackground(view: View) {
//        val imageViewSize = viewModel.imageSize
        imageViewBackground = view.findViewById(R.id.imageView_background)

        if(viewModel.isWatch) {
            val layoutParams = imageViewBackground.layoutParams as ConstraintLayout.LayoutParams

            // 修改高度百分比
            layoutParams.matchConstraintPercentHeight = 0.5f
            // 修改宽度百分比
            layoutParams.matchConstraintPercentWidth = 0.5f

            // 应用更改
            imageViewBackground.layoutParams = layoutParams
        }

        imageViewBackground.visibility = View.INVISIBLE
//        imageViewBackground.layoutParams.width = imageViewSize
//        imageViewBackground.layoutParams.height = imageViewSize
        imageViewBackground.alpha = UI.ALPHA_3
    }


    // 设置底部的按钮
    private fun setBottomButton(view: View) {
        // 全选
        buttonSelectAll = view.findViewById(R.id.button_select_all)
        buttonSelectAll.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            viewModel.selectAllItems()
            updateData()
        }
        // 反选
        buttonReverseSelect = view.findViewById(R.id.button_reverse_select)
        buttonReverseSelect.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            viewModel.invertSelection()
            updateData()
        }

        // 排序方式
        buttonSortMode = view.findViewById(R.id.button_sort_mode)
        buttonSortMode.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                context = context,
                viewModel = viewModel,
                menuList = MenuList.sort,
                viewPager = viewPager,
                fragmentManager = requireActivity().supportFragmentManager,
                constraintLayoutOrigin = constraintLayoutOrigin,
                onMenuItemClick = { menuItemId ->
                    when(menuItemId) {
                        else -> {
                            if(menuItemId in MenuType.SortOrderSet) {
                                viewModel.sortMode.value = true
                                adapter.submitList()
                            }
                        }
                    }
                })
        }
    }


    // 设置 RecyclerView
    private fun setRecyclerView(view: View) {
        viewModel.loadStorageActivities()
        recyclerView = view.findViewById(R.id.recyclerView_widget_list)

        adapter = WidgetListAdapter(
            myActivity = activity,
            viewModel = viewModel,
            fragment = this,
            fragmentManager = requireActivity().supportFragmentManager,
            viewPager = viewPager,
        )
        recyclerView.adapter = adapter


        // 使用 GridLayoutManager
        val gridLayoutManager = GridLayoutManager(requireContext(), maxWidgetSize)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.toolList[position].width
            }
        }
        recyclerView.layoutManager = gridLayoutManager

    }


    // 设置 ItemTouchHelper
    private fun setItemTouchHelper() {
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                VibrationHelper.vibrateOnClick(viewModel)

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                val fromItem = adapter.toolList[fromPosition]

                adapter.toolList.removeAt(fromPosition)
                adapter.toolList.add(toPosition, fromItem)
                adapter.notifyItemMoved(fromPosition, toPosition)
                viewModel.updateStoredToolListOrder()

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    VibrationHelper.vibrateOnLongPress(viewModel) // 添加振动反馈
                }
                super.onSelectedChanged(viewHolder, actionState)
            }
        })
        adapter.setItemTouchHelper(itemTouchHelper)
    }


    // 禁用 ItemTouchHelper
    fun disableItemTouchHelper() {
        itemTouchHelper.attachToRecyclerView(null)
    }

    // 启用 ItemTouchHelper
    fun enableItemTouchHelper() {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    // 设置观察者
    private fun setObservers() {
        fragmentNameObserver = Observer { fragmentName ->
            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT) {
                updateData()
            }
        }
        viewModel.fragmentName.observe(viewLifecycleOwner, fragmentNameObserver)


        widgetListSizeWasModifiedObserver = Observer { widgetListSizeWasModified ->
            if(widgetListSizeWasModified || viewModel.storedToolList.value?.size != viewModel.storedToolMap.size) {
                updateData()
            }
        }
        viewModel.toolListSizeChanged.observe(
            viewLifecycleOwner, widgetListSizeWasModifiedObserver
        )


        widgetWasModifiedObserver = Observer { widgetWasModified ->
            if(widgetWasModified) {
                updateData()
            }
        }
        viewModel.toolChanged.observe(viewLifecycleOwner, widgetWasModifiedObserver)


        multiSelectModeObserver = Observer { multiSelectMode ->
            if(multiSelectMode) {
                // 进入多选模式
                imageViewBackground.setImageResource(R.drawable.ic_multiselect)
                imageViewBackground.visibility = View.VISIBLE
                constraintLayoutBackground.isClickable = false

                buttonSelectAll.visibility = View.VISIBLE
                buttonReverseSelect.visibility = View.VISIBLE

                textViewNoWidget.text = getString(context, R.string.multi_select)
                textViewNoWidget.alpha = UI.ALPHA_5
                if(!viewModel.isWatch) {
                    textViewNoWidget.visibility = View.VISIBLE
                }

                Toast.makeText(
                    context,
                    getString(context, R.string.enter_multiselect_mode),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                // 退出多选模式
                imageViewBackground.visibility = View.INVISIBLE

                buttonSelectAll.visibility = View.GONE
                buttonReverseSelect.visibility = View.GONE

                if(!viewModel.isWatch) {
                    textViewNoWidget.visibility = View.INVISIBLE
                }

                // 只有返回按钮和返回手势才会触发退出多选模式的 Toast
                // 所以退出多选模式的 Toast 设置在 MainActivity 中

                // 退出多选模式后，刷新列表
                adapter.submitList()
            }
        }
        viewModel.multiselectMode.observe(viewLifecycleOwner, multiSelectModeObserver)

        sortModeObserver = Observer { sortMode ->
            if(sortMode) {
                // 进入排序模式
                if(viewModel.freeSort) {
                    enableItemTouchHelper()
                    imageViewBackground.setImageResource(R.drawable.ic_drag)
                    textViewNoWidget.text = getString(context, R.string.free_sort)
                } else {
                    disableItemTouchHelper()
                    imageViewBackground.setImageResource(R.drawable.ic_sort)
                    // 固定排序
                    textViewNoWidget.text = viewModel.sortModeString
                }

                textViewNoWidget.alpha = UI.ALPHA_5

                if(!viewModel.isWatch) {
                    textViewNoWidget.visibility = View.VISIBLE
                }

                imageViewBackground.visibility = View.VISIBLE
                constraintLayoutBackground.isClickable = false

                buttonSortMode.visibility = View.VISIBLE

            } else {
                // 退出排序模式
                imageViewBackground.visibility = View.INVISIBLE

                buttonSortMode.visibility = View.GONE
                // 只有返回按钮和返回手势才会触发退出多选模式的 Toast
                // 所以退出多选模式的 Toast 设置在 MainActivity 中
                // 返回时可能没有保存，恢复原始数据
                viewModel.restoreStoredToolList()
                updateData()

                if(viewModel.freeSort) {
                    viewModel.freeSort = false
                    disableItemTouchHelper()
                }
            }
        }
        viewModel.sortMode.observe(viewLifecycleOwner, sortModeObserver)
    }


    // 更新数据
    private fun updateData() {
        adapter.submitList()
        showNoWidget()
    }


    // 显示无组件提示
    fun showNoWidget() {
//        LogHelper.e("WidgetListFragment", "showNoWidget:editMode=${viewModel.editMode.value},listSize=${viewModel.storedActivityList.value?.size},mapSize=${viewModel.storageActivityMap.size}")

        if(viewModel.multiselectMode.value == true) {
            if(viewModel.storedToolList.value?.isEmpty() == true) {
                viewModel.multiselectMode.value = false
                Toast.makeText(
                    context,
                    getString(context, R.string.toast_no_widget_exit_multiselect_mode),
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                return
            }
        }
        if(viewModel.sortMode.value == true) {
            return
        }
        if(viewModel.storedToolList.value?.isEmpty() == true) {
            textViewNoWidget.text = getString(context, R.string.textView_no_widget)
            textViewNoWidget.alpha = UI.ALPHA_10
            textViewNoWidget.visibility = View.VISIBLE

            imageViewBackground.visibility = View.VISIBLE
            imageViewBackground.setImageResource(R.drawable.ic_blank)

            constraintLayoutBackground.isClickable = true
//            LogHelper.e("WidgetListFragment", "showNoWidget: 显示无组件提示")
        } else {
            if(viewModel.isWatch) {
                textViewNoWidget.visibility = View.GONE
            } else {
                textViewNoWidget.visibility = View.INVISIBLE
            }
            imageViewBackground.visibility = View.INVISIBLE
            constraintLayoutBackground.isClickable = false
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
        viewModel.toolListSizeChanged.removeObserver(widgetListSizeWasModifiedObserver)
        viewModel.toolChanged.removeObserver(widgetWasModifiedObserver)
        viewModel.multiselectMode.removeObserver(multiSelectModeObserver)
        viewModel.sortMode.removeObserver(sortModeObserver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        Log.i(TAG, "onDestroyView")
        removeObservers()
    }
}
