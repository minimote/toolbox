/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.ToolConstants.Alignment
import cn.minimote.toolbox.constant.ToolConstants.DisplayMode
import cn.minimote.toolbox.constant.ToolID
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.fragment.ToolListFragment
import cn.minimote.toolbox.fragment.WidgetListFragment
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.IconHelper.cancelLoadImage
import cn.minimote.toolbox.helper.IconHelper.getDrawable
import cn.minimote.toolbox.helper.IconHelper.loadImage
import cn.minimote.toolbox.helper.ImageSaveHelper.saveImage
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.ShortcutHelper.getMaxShortcutCount
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.holder.NoResultViewHolder
import cn.minimote.toolbox.viewModel.MyViewModel


open class WidgetListAdapter(
    private val myActivity: MainActivity,
    private val viewModel: MyViewModel,
    private val fragment: Fragment? = null,
    open var toolList: MutableList<StoredTool> = mutableListOf(),
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var itemTouchHelper: ItemTouchHelper


    init {
        if(toolList.isEmpty()) {
            toolList = loadToolList()
        }
    }


    inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewBackground: View = itemView.findViewById(R.id.view_background)
        val widgetIcon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
        val widgetName: TextView = itemView.findViewById(R.id.textView_app_name)


        fun bind(tool: StoredTool) {
            when(tool.displayMode) {
                DisplayMode.String.ONLY_ICON -> {
                    // TODO添加显示和隐藏动画
                    widgetIcon.visibility = View.VISIBLE
                    widgetIcon.loadImage(
                        viewModel = viewModel,
                        tool = tool,
                        progressBar = itemView.findViewById(R.id.progressBar),
                    )
//                    widgetIcon.setImageDrawable(
//                        viewModel.getCircularDrawable(tool)
//                    )

                    widgetName.visibility = View.GONE
                }

                DisplayMode.String.ONLY_NAME -> {
                    widgetIcon.visibility = View.GONE

                    widgetName.text = tool.nickname
                    widgetName.visibility = View.VISIBLE
                }

                else -> {
                    widgetIcon.visibility = View.VISIBLE
                    widgetIcon.loadImage(
                        viewModel = viewModel,
                        tool = tool,
                        progressBar = itemView.findViewById(R.id.progressBar),
                    )
//                    widgetIcon.setImageDrawable(
//                        viewModel.getCircularDrawable(tool)
//                    )

                    widgetName.text = tool.nickname
                    widgetName.visibility = View.VISIBLE
                }
            }

//            if(tool.displayMode != DisplayMode.String.ONLY_ICON) {
////                widgetIcon.loadImage(
////                    viewModel = viewModel,
////                    tool = tool,
////                    progressBar = itemView.findViewById(R.id.progressBar),
////                )
//                widgetIcon.setImageDrawable(
//                    viewModel.getCircularDrawable(tool)
//                )
//            }


            val linearLayout = itemView.findViewById<LinearLayout>(R.id.linearLayout)
            val layoutParams = linearLayout.layoutParams as ConstraintLayout.LayoutParams

            when(tool.alignment) {
                Alignment.LEFT -> {
                    layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                    widgetName.gravity = Gravity.START
                }

                Alignment.RIGHT -> {
                    layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    widgetName.gravity = Gravity.END
                }

                else -> {
                    layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    widgetName.gravity = Gravity.CENTER
                }
            }
            // 应用修改后的约束
            linearLayout.layoutParams = layoutParams


            // 根据 selectedIds 设置背景状态
            val isSelected = viewModel.isSelected(tool.id)
            viewBackground.visibility = if(
                viewModel.multiselectMode.value == true && isSelected
            ) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }


            itemView.setOnClickListener {
                if(inLongPressMenuPage) {
                    return@setOnClickListener
                }

                if(viewModel.multiselectMode.value == true) {
                    VibrationHelper.vibrateOnClick(viewModel)
                    if(viewModel.isSelected(tool.id)) {
                        viewModel.deselectItem(tool.id)
                        viewBackground.visibility = View.INVISIBLE
                    } else {
                        viewModel.selectedItem(tool.id)
                        viewBackground.visibility = View.VISIBLE
                    }
                } else if(viewModel.sortMode.value != true) {
                    VibrationHelper.vibrateOnClick(viewModel)
                    // 启动新活动
                    LaunchHelper.launch(
                        myActivity = myActivity,
                        viewModel = viewModel,
                        tool = tool,
                    )
                }
            }

            // 禁用振动反馈
            itemView.isHapticFeedbackEnabled = false
            itemView.setOnLongClickListener {

                if(inLongPressMenuPage) {
                    VibrationHelper.vibrateOnLongPress(viewModel)
                    BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                        viewModel = viewModel,
                        activity = myActivity,
                        tool = tool,
                        menuList = MenuList.longPress,
                        onMenuItemClick = { menuItemId ->
                            when(menuItemId) {
                                MenuType.LongPress.REMOVE -> {

                                    DialogHelper.setAndShowDefaultDialog(
                                        context = myActivity,
                                        viewModel = viewModel,
                                        messageText = myActivity.getString(
                                            R.string.confirm_remove_single_tool,
                                            tool.name
                                        ),
                                        positiveAction = {
                                            viewModel.removeDynamicShortcutTool(tool.id)
                                            submitList(
                                                viewModel.getDynamicShortcutDisplayList()
                                            )
                                            (fragment as? ToolListFragment)?.refreshToolList()
                                        },
                                    )

                                }

                                MenuType.LongPress.MOVE_TO_TOP -> {
                                    viewModel.moveDynamicShortcutToolToTop(tool.id)
                                    submitList(
                                        viewModel.getDynamicShortcutDisplayList()
                                    )
                                }

                                MenuType.LongPress.MOVE_TO_BOTTOM -> {
                                    viewModel.moveDynamicShortcutToolToBottom(tool.id)
                                    submitList(
                                        viewModel.getDynamicShortcutDisplayList()
                                    )
                                }

                                else -> {}
                            }
                        }
                    )
                    return@setOnLongClickListener true
                }

//                LogHelper.e("长按二，${inLongPressMenuPage}", "")
                if(viewModel.multiselectMode.value != true && viewModel.sortMode.value != true) {
                    VibrationHelper.vibrateOnLongPress(viewModel)
                    BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                        viewModel = viewModel,
                        activity = myActivity,
                        tool = tool,
                        menuList = if(viewModel.isWatch) MenuList.widget_watch else MenuList.widget,
                        onMenuItemClick = { menuItemId ->
                            when(menuItemId) {
                                MenuType.MULTI_SELECT -> {
                                    viewBackground.visibility = View.VISIBLE
                                    viewModel.clearSelectedIds()
                                    viewModel.selectedItem(tool.id)
                                    viewModel.multiselectMode.value = true
                                }

                                MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
//                                // 添加缩放加透明度的组合动画
                                    // TODO: 会被刷新覆盖，无法显示动画
//                                holder.itemView.animate()
//                                    .alpha(0f)
//                                    .scaleX(0.8f)
//                                    .scaleY(0.8f)
//                                    .setDuration(300)
//                                    .start()
                                }

                                MenuType.SORT -> {
//                                viewModel.sortMode.value = true
                                    (fragment as? WidgetListFragment)?.buttonSortMode?.performClick()
                                }

                                MenuType.SAVE_IMAGE, MenuType.SAVE_ICON -> {
                                    saveImage(
                                        drawable = viewModel.getDrawable(tool),
                                        fileName = tool.nickname,
                                        viewModel = viewModel,
                                        context = myActivity,
                                    )
                                }

                                else -> {}
                            }
                        }
                    )
                }

                true // 返回 true 以表示事件已处理，不再继续传递
            }
        }
    }


    inner class TiTleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textView_name)

        fun bind() {
            textViewName.text = myActivity.getString(
                R.string.dynamic_shortcut_title,
                viewModel.getDynamicShortcutListSize(),
                viewModel.getMaxShortcutCount()
            )
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType) {
            ViewTypes.Widget.DYNAMIC_SHORTCUT_TITLE -> {
                TiTleViewHolder(
                    LayoutInflater.from(myActivity).inflate(
                        R.layout.item_edit_preview,
                        parent, false
                    )
                )
            }

            ViewTypes.Widget.NO_DYNAMIC_SHORTCUT -> {
                NoResultViewHolder(parent = parent)
            }

            else -> {
                WidgetViewHolder(
                    LayoutInflater.from(myActivity).inflate(
                        R.layout.item_widget_icon_and_name,
                        parent, false
                    )
                )
            }
        }
    }


    @SuppressLint("RtlHardcoded")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is WidgetViewHolder -> {
                holder.bind(toolList[position])
            }

            is TiTleViewHolder -> {
                holder.bind()
            }

            is NoResultViewHolder -> {
                holder.bind(
                    myActivity.getString(R.string.no_dynamic_shortcut),
                )
            }
        }


    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when(holder) {
            is WidgetViewHolder -> {
                holder.widgetIcon.cancelLoadImage()
            }
        }
    }


    override fun getItemCount(): Int = toolList.size


    override fun getItemViewType(position: Int): Int {
        return when(toolList[position].id) {
            ToolID.DYNAMIC_SHORTCUT_TITLE -> ViewTypes.Widget.DYNAMIC_SHORTCUT_TITLE
            ToolID.NO_DYNAMIC_SHORTCUT -> ViewTypes.Widget.NO_DYNAMIC_SHORTCUT
            else -> ViewTypes.Widget.NORMAL
        }
    }


    // 判断是否在长按菜单页面中
    private val inLongPressMenuPage: Boolean
        get() = viewModel.getFragmentName() == FragmentName.SCHEME_LIST_FRAGMENT


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newActivityList: MutableList<StoredTool> = loadToolList()) {
//        LogHelper.e("WidgetListAdapter", "submitList:${newActivityList.size}")
        toolList = newActivityList
        notifyDataSetChanged()
    }


    private fun loadToolList(): MutableList<StoredTool> {
        return viewModel.storedToolList.value ?: mutableListOf()
    }


    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

}
