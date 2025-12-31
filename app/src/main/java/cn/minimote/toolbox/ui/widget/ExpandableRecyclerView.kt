/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.ToolID
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.AnimationHelper.cancelAnimation
import cn.minimote.toolbox.helper.AnimationHelper.rotate
import cn.minimote.toolbox.helper.AnimationHelper.scaleDown
import cn.minimote.toolbox.helper.AnimationHelper.scaleUp
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.DimensionHelper.getLayoutSize
import cn.minimote.toolbox.helper.IconHelper.cancelLoadImage
import cn.minimote.toolbox.helper.IconHelper.getDrawable
import cn.minimote.toolbox.helper.IconHelper.loadImage
import cn.minimote.toolbox.helper.ImageSaveHelper.saveImage
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.OtherConfigHelper.getUiStateConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.saveUiStateConfig
import cn.minimote.toolbox.helper.OtherConfigHelper.updateUiStateConfigValue
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.SearchHelper
import cn.minimote.toolbox.helper.ShortcutHelper.getMaxShortcutCount
import cn.minimote.toolbox.helper.ShortcutHelper.reachShortcutMaxCnt
import cn.minimote.toolbox.helper.TypeConversionHelper.toStringList
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.holder.NoResultViewHolder
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.switchmaterial.SwitchMaterial


class ExpandableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FixedSizeRecyclerView(context, attrs, defStyleAttr) {


    //    private lateinit var coilIconHelper: CoilIconHelper
    private lateinit var adapter: ExpandableAdapter
    private lateinit var viewModel: MyViewModel
    private lateinit var myActivity: MainActivity
    private var isSchemeList: Boolean = false
    private val configKey: String
        get() = if(isSchemeList) {
            Config.ConfigKeys.CollapsedGroups.SCHEME_LIST
        } else {
            Config.ConfigKeys.CollapsedGroups.TOOL_LIST
        }
    private var emptyAreaClickAction: () -> Unit = {}
    private var getQuery: () -> String = { "" }
    private var updateSearchHistoryAndSuggestion: (String) -> Unit = {}
    private var clearSearchHistoryOrSuggestion: (String) -> Unit = {}
    private var setSearchBoxText: (String) -> Unit = {}

    //    private lateinit var viewShadowTop: View
//    private lateinit var viewShadowBottom: View
    private lateinit var shadowConstraintLayout: ShadowConstraintLayout


//    init {
    // 设置带有展开/折叠动画的 ItemAnimator
//        itemAnimator = ExpandableItemAnimator()
//        itemAnimator = null
//    }


    fun setParameters(
        viewModel: MyViewModel,
        myActivity: MainActivity,
        isSchemeList: Boolean,
        shadowConstraintLayout: ShadowConstraintLayout,
        emptyAreaClickListener: () -> Unit,
        getQuery: () -> String,
        updateSearchHistoryAndSuggestion: (String) -> Unit,
        clearSearchHistoryOrSuggestion: (String) -> Unit,
        setSearchBoxText: (String) -> Unit,
        actionOnClick: ((Tool) -> Unit) = {},
    ) {
        this.viewModel = viewModel
        this.myActivity = myActivity
        this.isSchemeList = isSchemeList
//        this.fragment = fragment
        this.emptyAreaClickAction = emptyAreaClickListener
        this.getQuery = getQuery
        this.updateSearchHistoryAndSuggestion = updateSearchHistoryAndSuggestion
        this.clearSearchHistoryOrSuggestion = clearSearchHistoryOrSuggestion
        this.setSearchBoxText = setSearchBoxText
//        this.coilIconHelper = CoilIconHelper(viewModel)
        this.shadowConstraintLayout = shadowConstraintLayout


        // 初始化适配器
        adapter = ExpandableAdapter(
            actionOnClick = actionOnClick,
        )

        // 手表使用 LinearLayoutManager
        // 使用后会出现项目错位问题，所以统一用 LinearLayoutManager
        if(viewModel.isWatch) {
            layoutManager = LinearLayoutManager(context)
        } else {
            // 使用 FlexboxLayoutManager 实现自适应列数
            val flexboxLayoutManager = FlexboxLayoutManager(context)
            flexboxLayoutManager.flexDirection = FlexDirection.ROW
            flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexboxLayoutManager.alignItems = AlignItems.CENTER
            flexboxLayoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行

            layoutManager = flexboxLayoutManager
        }


        setAdapter(adapter)


//        if(parent is ShadowConstraintLayout) {
//            (parent as ShadowConstraintLayout).setShadow()
//        }
//        LogHelper.e(
//            "折叠列表",
//            "${parent.javaClass.simpleName}"
//        )
        shadowConstraintLayout.setShadow(viewModel = viewModel, addBottomPadding = false)


        // 添加点击监听器到 RecyclerView 本身
//        this.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
//            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                val childView = rv.findChildViewUnder(e.x, e.y)
//                if(childView == null) {
//                    emptyAreaClickListener()
//                    return true
//                }
//                return false
//            }
//        })

//        this.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
//            private var startX = 0f
//            private var startY = 0f
//            private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
//
//            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                when(e.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        startX = e.x
//                        startY = e.y
//                        // 不拦截 ACTION_DOWN 事件
//                        return false
//                    }
//
//                    MotionEvent.ACTION_UP -> {
//                        val endX = e.x
//                        val endY = e.y
//                        val deltaX = abs(endX - startX)
//                        val deltaY = abs(endY - startY)
//
//                        // 只有当是点击事件（移动距离小于 touchSlop）并且点击在空白区域时才处理
//                        if(deltaX < touchSlop && deltaY < touchSlop) {
//                            val childView = rv.findChildViewUnder(e.x, e.y)
//                            if(childView == null) {
//                                // 在 post 中执行回调，避免阻塞触摸事件处理
//                                rv.post {
//                                    emptyAreaClickListener()
//                                }
//                                return true
//                            }
//                        }
//                        return false
//                    }
//
//                    else -> return false // 不拦截其他事件，保证滚动和越界回弹正常工作
//                }
//            }
//        })

//        this.setOnClickListener {
//            // 检查当前是否正在触摸 RecyclerView 本身而不是子项
//            if(this.isEmpty() ||
//                (this.getChildAt(0).y >= this.height) ||
//                (this.getChildAt(this.childCount - 1).y + this.getChildAt(this.childCount - 1).height <= 0)
//            ) {
//                emptyAreaClickListener()
//            }
//        }
    }


    fun setGroups(groups: List<ExpandableGroup>) {
        adapter.setGroups(groups)
    }


    fun inSearchMode(): Boolean {
//        return viewModel.searchMode.value == true
//        Toast.makeText(context, "inSearchMode: ${this@ExpandableRecyclerView.alpha}", Toast.LENGTH_SHORT).show()
//        return viewModel.searchMode.value == true && this@ExpandableRecyclerView.alpha != 1f
//        return searchMode
        return if(isSchemeList) {
            viewModel.searchModeSchemeList.value == true
        } else {
            viewModel.searchModeToolList.value == true
        }
    }


    /**
     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
     * 组内元素整体上移并逐渐透明的动画效果，精确控制避免越界回弹
     */
    inner class ExpandableItemAnimator : DefaultItemAnimator() {
        override fun animateAdd(holder: ViewHolder): Boolean {
            // 取消可能正在进行的动画
            holder.itemView.cancelAnimation()

            // 展开时：从透明和稍微向下位置开始，然后移动到正常位置
            holder.itemView.alpha = 0f
//            holder.itemView.translationY = 30f

            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 600
                // 使用加速减速插值器
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    holder.itemView.alpha = fraction
//                    holder.itemView.translationY = 30f * (1 - fraction)
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        holder.itemView.alpha = 1f
                        holder.itemView.translationY = 0f
                        dispatchAddFinished(holder)
                    }
                })
            }

            animator.start()
            dispatchAddStarting(holder)
            return true
        }

        override fun animateRemove(holder: ViewHolder): Boolean {
            // 取消可能正在进行的动画
            holder.itemView.cancelAnimation()

            // 折叠时：向上移动并逐渐变透明
            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 600
                // 使用加速减速插值器
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    holder.itemView.alpha = 1f - fraction
//                    holder.itemView.translationY = -30f * fraction
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        holder.itemView.alpha = 1f
                        holder.itemView.translationY = 0f
                        dispatchRemoveFinished(holder)
                    }
                })
            }

            animator.start()
            dispatchRemoveStarting(holder)
            return true
        }

        override fun isRunning(): Boolean {
            return false
        }
    }


    inner class ExpandableAdapter(
        private var actionOnClick: ((Tool) -> Unit),
    ) : Adapter<ViewHolder>() {

        private var groups = listOf<ExpandableGroup>()
        private var collapsedGroups = mutableSetOf<String>()

        @SuppressLint("NotifyDataSetChanged")
        fun setGroups(newGroups: List<ExpandableGroup>) {
            groups = newGroups

            // 初始化展开状态
//            collapsedGroups = configValue.toMutableSet()
            collapsedGroups.clear()
//            Toast.makeText(context, "获取${!inSearchMode()}", Toast.LENGTH_SHORT).show()
            if(!inSearchMode()) {
                getSavedCollapsedGroups()
            }

//            groups.forEachIndexed { index, group ->
//                if(!group.isExpanded) {
//                    collapsedGroups.add(group.titleString)
//                }
//            }

//            Toast.makeText(context, "刷新${groups.size}", Toast.LENGTH_SHORT).show()

            notifyDataSetChanged()
        }


        override fun getItemViewType(position: Int): Int {
            val (item, _) = getItemWithGroupIndex(position)
            return when(item) {
                is ExpandableGroup -> {
                    when(item.titleString) {
                        context.getString(R.string.no_result) -> {
                            ViewTypes.ToolList.NO_RESULT
                        }

                        context.getString(R.string.search_history),
                        context.getString(R.string.search_suggestion),
                            -> {
                            ViewTypes.ToolList.SEARCH_TITLE
                        }

                        context.getString(R.string.app_tool) -> {
                            ViewTypes.ToolList.GROUP_NO_MARGIN_TOP
                        }

                        ToolID.BLANK -> {
                            ViewTypes.ToolList.BOTTOM_SPACE
                        }

                        else -> {
                            ViewTypes.ToolList.GROUP_WITH_MARGIN_TOP
                        }
                    }
                }

                is Tool -> {
                    when(item.id) {
                        ToolID.BLANK -> {
                            ViewTypes.ToolList.SEPARATOR
                        }

                        else -> {
                            if(isSchemeList) {
                                ViewTypes.ToolList.CHILD_ITEM_SWITCH
                            } else {
                                ViewTypes.ToolList.CHILD_ITEM
                            }
                        }
                    }
                }

                is String -> {
                    ViewTypes.ToolList.HISTORY_OR_SUGGESTION
                }

                else -> {
                    ViewTypes.ToolList.SEPARATOR
                }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when(viewType) {
                ViewTypes.ToolList.GROUP_NO_MARGIN_TOP -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool_title_no_margin_top, parent, false)
                    GroupViewHolder(view)
                }

                ViewTypes.ToolList.GROUP_WITH_MARGIN_TOP -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool_title_with_margin_top, parent, false)
                    GroupViewHolder(view)
                }

                ViewTypes.ToolList.CHILD_ITEM -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool, parent, false)
                    ChildViewHolder(view)
                }

                ViewTypes.ToolList.CHILD_ITEM_SWITCH -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_installed_app, parent, false)
                    ChildSwitchViewHolder(view)
                }

                ViewTypes.ToolList.SEPARATOR -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool_separator, parent, false)
                    SeparatorViewHolder(view)
                }

                ViewTypes.ToolList.NO_RESULT -> {
                    NoResultViewHolder(
                        parent = parent,
                        actionOnClick = {
                            emptyAreaClickAction()
                        },
                    )
                }

                ViewTypes.ToolList.HISTORY_OR_SUGGESTION -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_history_or_suggestion, parent, false)
                    HistoryOrSuggestionViewHolder(view)
                }

                ViewTypes.ToolList.SEARCH_TITLE -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_search_title, parent, false)
                    SearchTitleViewHolder(view)
                }

                ViewTypes.ToolList.BOTTOM_SPACE -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_bottom_space, parent, false)
                    BottomSpaceViewHolder(view)
                }

                else -> throw IllegalArgumentException("未知的类型 type: $viewType")
            }
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (item, groupIndex) = getItemWithGroupIndex(position)

            when(holder) {
                is GroupViewHolder -> {
                    val group = item as ExpandableGroup
                    holder.bind(
                        group = group,
                        groupIndex = groupIndex,
                        onGroupClick = { toggleGroup(groupIndex) },
                    )
                }

                is ChildViewHolder -> {
                    val child = item as Tool
                    holder.bind(
                        tool = child,
                        actionOnClick = {
                            actionOnClick.invoke(child)
                        },
                    )
                }


                is ChildSwitchViewHolder -> {
                    val child = item as Tool
                    holder.bind(
                        tool = child,
                        actionOnClick = {
                            actionOnClick.invoke(child)
                        },
                    )
                }


                is SeparatorViewHolder -> {
//                    val separator = item as AppSeparator
//                    holder.bind(separator)
                }

                is NoResultViewHolder -> {
                    holder.bind()
                }

                is HistoryOrSuggestionViewHolder -> {
                    holder.bind(item as String)
                }

                is SearchTitleViewHolder -> {
                    holder.bind((item as ExpandableGroup).titleString)
                }
            }


            // 为第一个项目设置顶部 margin
//            if(position == 0) {
//                val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
//                layoutParams.topMargin = 0
//                holder.itemView.layoutParams = layoutParams
//            }

//            // 为最后一个项目设置底部 margin
//            if(isSchemeList) {
//                if(position == itemCount - 1) {
//                    val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
//                    layoutParams.bottomMargin = getLayoutSize(
//                        context = context,
//                        layoutDimensionId = R.dimen.layout_size_bottom_sheet_cancel_margin,
//                    )
//                    holder.itemView.layoutParams = layoutParams
//                } else {
//                    val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
//                    layoutParams.bottomMargin = 0
//                    holder.itemView.layoutParams = layoutParams
//                }
//            }
        }

        override fun getItemCount(): Int {
            var count = 0
            groups.forEachIndexed { _, group ->
                // 只有当组内有元素或组名需要显示时才计算
                if(shouldShowGroup(group)) {
                    // Always count the group header if it should be shown
                    count++

                    // Count children if group is expanded
                    if(!collapsedGroups.contains(group.titleString)) {
                        // 使用限制后的子项数量
                        count += getDisplayedItemCount(group)
                    }
                }
//                LogHelper.e("${group.titleString}数量", "groupIndex: $index, count: $count")
            }
            return count
        }


        private fun getDisplayedItemCount(group: ExpandableGroup): Int {
            // 如果设置了最大显示数量且数据量超过最大显示数量，则返回最大显示数量，否则返回实际数据量
            val maxDisplayedItems = group.maxDisplayedCount
            return if(maxDisplayedItems != null && group.getSize() > maxDisplayedItems) {
                maxDisplayedItems
            } else {
                group.getSize()
            }
        }


        private fun shouldShowGroup(group: ExpandableGroup): Boolean {
            // 如果设置了最大显示数量且为0，则不显示该组
            if(group.maxDisplayedCount != null && group.maxDisplayedCount == 0) {
                return false
            }

            // 对于没有子元素的组，不显示组标题，除非是特殊组
            return group.dataList.isNotEmpty() ||
                    group.titleString == context.getString(R.string.no_result) ||
                    group.titleString == ToolID.BLANK
        }


        private fun getItemWithGroupIndex(position: Int): Pair<Any, Int> {
            var currentPosition = 0

            for((groupIndex, group) in groups.withIndex()) {
                // 只处理应该显示的组
                if(!shouldShowGroup(group)) {
                    continue
                }

                // Check if current position is the group header
                if(currentPosition == position) {
                    return Pair(group, groupIndex)
                }

                currentPosition++

                // If group is expanded, check children
                if(!collapsedGroups.contains(group.titleString)) {
                    // 使用限制后的子项数量
                    val childrenCount = getDisplayedItemCount(group)
                    if(position < currentPosition + childrenCount) {
                        val childIndex = position - currentPosition
                        return Pair(group.dataList[childIndex], groupIndex)
                    }
                    currentPosition += childrenCount
                }
            }

            throw IndexOutOfBoundsException("位置 $position 越界")
        }


        private fun toggleGroup(groupIndex: Int) {
            // 加 1 是因为不通知组标题变化
            val startPosition = getGroupStartPosition(groupIndex) + 1
            val displayedItemCount = getDisplayedItemCount(groups[groupIndex])
//            Toast.makeText(
//                context,
//                "start: $startPosition, cnt=${groups[groupIndex].getSize()},${displayedItemCount}",
//                Toast.LENGTH_SHORT
//            ).show()

            if(collapsedGroups.contains(groups[groupIndex].titleString)) {
                collapsedGroups.remove(groups[groupIndex].titleString)
                // 只通知该组范围内的项目变化
                notifyItemRangeInserted(
                    startPosition,

                    displayedItemCount,
                )
            } else {
                collapsedGroups.add(groups[groupIndex].titleString)
                // 只通知该组范围内的项目变化
                notifyItemRangeRemoved(
                    startPosition,
                    displayedItemCount,
                )
            }
//            notifyDataSetChanged()

            if(!inSearchMode()) {
                saveCollapsedGroups()
            }

            this@ExpandableRecyclerView.shadowConstraintLayout.updateShadow()

        }


        // 获取当前折叠的组
        fun getCollapsedGroups(): List<String> {
            return collapsedGroups.toList()
        }


        // 获取保存的折叠组
        fun getSavedCollapsedGroups() {
//            if(viewModel.isWatch) {
//                return
//            }
            val configValue = viewModel.getUiStateConfigValue(
                key = configKey,
            ).toStringList()

            configValue.map {
                collapsedGroups.add(it)
            }
//            Toast.makeText(context, "${configValue!!::class.java.simpleName }${collapsedGroups}", Toast.LENGTH_SHORT).show()
        }


        // 保存折叠状态到配置中
        private fun saveCollapsedGroups() {
//            if(viewModel.isWatch) {
//                return
//            }
            viewModel.updateUiStateConfigValue(
                key = configKey,
                value = getCollapsedGroups(),
            )
            viewModel.saveUiStateConfig()
//            Toast.makeText(context, "保存状态${getCollapsedGroups()}", Toast.LENGTH_SHORT).show()
        }


        // 更新搜索历史
        private fun updateSearchHistory(text: String) {
            if(inSearchMode()) {
                updateSearchHistoryAndSuggestion(text)
            }
        }


        private fun getGroupStartPosition(groupIndex: Int): Int {
            var position = 0
            for(i in 0 until groupIndex) {
                // 只计算应该显示的组
                if(shouldShowGroup(groups[i])) {
                    position++ // Group header
                    if(!collapsedGroups.contains(groups[i].titleString)) {
                        // 使用限制后的子项数量
                        position += getDisplayedItemCount(groups[i]) // Expanded children
                    }
                }
            }
            return position
        }


        inner class GroupViewHolder(itemView: View) : ViewHolder(itemView) {
            private val textViewName: TextView = itemView.findViewById(R.id.textView_name)
            private val imageViewGroupIndicator: ImageView =
                itemView.findViewById(R.id.group_indicator)

            fun bind(
                group: ExpandableGroup,
                groupIndex: Int,
                onGroupClick: () -> Unit,
            ) {
                // 如果组内没有元素，隐藏组标题
                if(!shouldShowGroup(group)) {
                    itemView.visibility = GONE
                    return
                } else {
                    itemView.visibility = VISIBLE
                }


                textViewName.text = group.titleString

                imageViewGroupIndicator.setImageResource(R.drawable.ic_arrow_right)
                // 根据展开状态设置图标旋转角度
                imageViewGroupIndicator.rotation = getRotation(groupIndex)

                itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    // 执行指示器旋转动画
                    animateIndicator(!getExpandedState(groupIndex))
                    onGroupClick()

//                    val delayMillis = if(isSchemeList) {
//                        0L
//                    } else {
//                        ANIMATION_DURATION
//                    }
//                    // 延迟展开/折叠，保证涟漪动画流畅
//                    itemView.postDelayed({
//                        onGroupClick()
//                    }, delayMillis)
                }
//                }
            }


            private fun getExpandedState(groupIndex: Int): Boolean {
                return !collapsedGroups.contains(groups[groupIndex].titleString)
            }

            private fun getRotation(groupIndex: Int): Float {
                return if(getExpandedState(groupIndex)) 90f else 0f
            }

            private fun getRotation(expanded: Boolean): Float {
                return if(expanded) 90f else 0f
            }


            fun animateIndicator(expanded: Boolean) {
                val endRotation = getRotation(expanded)
//                Toast.makeText(context, "expanded: $expanded", Toast.LENGTH_SHORT).show()
                // 如果是手表，直接设置旋转角度，不使用动画
                if(viewModel.isWatch) {
                    imageViewGroupIndicator.rotation = endRotation
                } else {
                    // 取消之前的动画，避免冲突
                    imageViewGroupIndicator.cancelAnimation()

                    imageViewGroupIndicator.rotate(
                        rotationTo = endRotation,
                    )
                }
            }


        }


        inner class ChildViewHolder(itemView: View) : ViewHolder(itemView) {

            val imageViewIcon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
            val imageViewLikeIcon: ImageView = itemView.findViewById(R.id.imageView_heart)
            val textViewName: TextView = itemView.findViewById(R.id.textView_app_name)
            val textViewDescription: TextView = itemView.findViewById(R.id.textView_activity_name)


            init {
                if(viewModel.isWatch) {
                    itemView.layoutParams.width = LayoutParams.MATCH_PARENT
                } else {
                    val layoutParams = itemView.layoutParams as? FlexboxLayoutManager.LayoutParams
                    layoutParams?.flexGrow = 1f
                }

                // 设置最大宽度
                textViewName.maxWidth = viewModel.screenWidth - getLayoutSize(
                    context = context,
                    layoutDimensionId = R.dimen.layout_size_1_large,
                    rate = 1.5f,
                )
                textViewDescription.maxWidth = textViewName.maxWidth
            }


            fun bind(tool: Tool, actionOnClick: (Tool) -> Unit) {

//                imageViewIcon.setImageDrawable(viewModel.getCircularDrawable(tool))

                imageViewIcon.loadImage(
                    viewModel = viewModel,
                    tool = tool,
                    progressBar = itemView.findViewById(R.id.progressBar),
                )


//                CoroutineScope(Dispatchers.Main).launch {
//                    val drawable = withContext(Dispatchers.IO) {
//                        viewModel.iconCacheHelper.getCircularDrawable(tool)
//                    }
//                    imageViewIcon.load(drawable)
//                }

//                viewModel.viewModelScope.launch {
//                    val drawable = withContext(Dispatchers.IO) {
//                        viewModel.iconCacheHelper.getCircularDrawable(tool)
//                    }
//                    imageViewIcon.load(drawable)
//                }

//                imageViewIcon.load(viewModel.iconCacheHelper.getCircularDrawable(tool))


// 取消之前的加载任务
//                loadJob?.cancel()
//
//                // 使用协程异步加载图片
//                loadJob = viewModel.viewModelScope.launch {
//                    try {
//                        val drawable = withContext(Dispatchers.IO) {
//                            viewModel.iconCacheHelper.getCircularDrawable(tool)
//                        }
//                        // 确保在主线程更新 UI
//                        withContext(Dispatchers.Main) {
//                            imageViewIcon.setImageDrawable(drawable)
//                        }
//                    } catch (_: Exception) {
//                        // 处理异常情况
//                    }
//                }


                textViewName.text = SearchHelper.highlightSearchTermForDevice(
                    viewModel = viewModel,
                    text = tool.name,
                    query = getQuery(),
                )

                val descriptionText = if(isSchemeList) {
                    SchemeHelper.getSchemeFromId(tool.id)
                } else {
                    tool.description.orEmpty().trim()
                }
                if(descriptionText.isNotBlank()) {
                    textViewDescription.text = SearchHelper.highlightSearchTermForDevice(
                        viewModel = viewModel,
                        text = descriptionText,
                        query = getQuery(),
                    )
                    textViewDescription.visibility = VISIBLE
                } else {
                    textViewDescription.visibility = GONE
                }

                updateLikeIcon(tool = tool, imageViewLikeIcon = imageViewLikeIcon)

                itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    updateSearchHistory(tool.name)
                    if(isSchemeList) {
                        Toast.makeText(
                            myActivity,
                            myActivity.getString(R.string.long_press_can_copy_scheme),
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        LaunchHelper.launch(myActivity, viewModel, tool)
                    }
                    actionOnClick(tool)
                }


                // 取消长按震动
                itemView.isHapticFeedbackEnabled = false
                itemView.setOnLongClickListener { _ ->

                    VibrationHelper.vibrateOnLongPress(viewModel)
                    updateSearchHistory(tool.name)
                    if(isSchemeList) {
                        ClipboardHelper.copyToClipboard(
                            context = myActivity,
                            text = SchemeHelper.getSchemeFromId(tool.id),
                            toastString = tool.name,
                            secondToastString = myActivity.getString(
                                R.string.link
                            ),
                        )
                    } else {
                        BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                            viewModel = viewModel,
                            activity = myActivity,
                            tool = tool,
                            menuList = if(viewModel.isWatch) {
                                MenuList.tool_watch
                            } else {
                                MenuList.tool
                            },
                            onMenuItemClick = {
                                when(it) {
                                    MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
//                                        Toast.makeText(
//                                            myActivity,
//                                            "更新图标",
//                                            Toast.LENGTH_SHORT,
//                                        ).show()
                                        updateLikeIcon(
                                            tool = tool,
                                            imageViewLikeIcon = imageViewLikeIcon,
                                            showAnimation = true,
                                        )
                                    }


                                    MenuType.SAVE_IMAGE, MenuType.SAVE_ICON -> {
                                        saveImage(
                                            drawable = viewModel.getDrawable(tool),
                                            fileName = tool.nickname,
                                            viewModel = viewModel,
                                            context = myActivity,
                                        )
                                    }
                                }
                            }
                        )
                    }
                    true
                }
            }


            private fun updateLikeIcon(
                tool: Tool,
                imageViewLikeIcon: ImageView,
                // 是否显示动画
                showAnimation: Boolean = false,
            ) {
                val shouldShowLikeIcon = shouldShowLikeIcon(tool)

                if(showAnimation) {
                    if(shouldShowLikeIcon && !imageViewLikeIcon.isVisible) {
                        imageViewLikeIcon.visibility = VISIBLE
                        // 放大出现动画
                        imageViewLikeIcon.scaleUp()
                    } else if(!shouldShowLikeIcon && imageViewLikeIcon.isVisible) {
                        // 缩小消失动画
                        imageViewLikeIcon.scaleDown()
                    }
                } else {
                    if(shouldShowLikeIcon) {
                        imageViewLikeIcon.visibility = VISIBLE
                    } else {
                        imageViewLikeIcon.visibility = INVISIBLE
                    }
                }
            }


            private fun shouldShowLikeIcon(tool: Tool): Boolean {
                return !isSchemeList
                        && viewModel.inSizeChangeMap(tool.id)
                        && viewModel.getConfigValue(
                    Config.ConfigKeys.Display.SHOW_LIKE_ICON
                ) == true
            }
        }


        inner class ChildSwitchViewHolder(itemView: View) : ViewHolder(itemView) {

            val imageViewIcon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
            val textViewName: TextView = itemView.findViewById(R.id.textView_app_name)
            val textViewDescription: TextView = itemView.findViewById(R.id.textView_activity_name)

            val switch: SwitchMaterial = itemView.findViewById(R.id.switch_whether_show_in_home)


            fun bind(tool: Tool, actionOnClick: (Tool) -> Unit) {

//                imageViewIcon.setImageDrawable(viewModel.getCircularDrawable(tool))

                imageViewIcon.loadImage(
                    viewModel = viewModel,
                    tool = tool,
                    progressBar = itemView.findViewById(R.id.progressBar),
                )

                textViewName.text = SearchHelper.highlightSearchTermForDevice(
                    viewModel = viewModel,
                    text = tool.name,
                    query = getQuery(),
                )

                val descriptionText = if(isSchemeList) {
                    SchemeHelper.getSchemeFromId(tool.id)
                } else {
                    tool.description.orEmpty().trim()
                }
                if(descriptionText.isNotBlank()) {
                    textViewDescription.text = SearchHelper.highlightSearchTermForDevice(
                        viewModel = viewModel,
                        text = descriptionText,
                        query = getQuery(),
                    )
                    textViewDescription.visibility = VISIBLE
                } else {
                    textViewDescription.visibility = GONE
                }

                switch.isChecked = viewModel.inToolInDynamicShortcutList(tool.id)

                setDimmingEffect(this, !switch.isChecked)

                itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    updateSearchHistory(tool.name)

                    if(!switch.isChecked && viewModel.reachShortcutMaxCnt()) {

                        Toast.makeText(
                            myActivity,
                            myActivity.getString(
                                R.string.reach_max_shortcut_count,
                                viewModel.getMaxShortcutCount(),
                            ),
                            Toast.LENGTH_SHORT,
                        ).show()

                    } else {

                        switch.isChecked = !switch.isChecked
                        setDimmingEffect(this, !switch.isChecked)

                        if(switch.isChecked) {
                            viewModel.addDynamicShortcutTool(tool.id)
                        } else {
                            viewModel.removeDynamicShortcutTool(tool.id)
                        }
                    }

                    actionOnClick(tool)
                }


                // 取消长按震动
                itemView.isHapticFeedbackEnabled = false
                itemView.setOnLongClickListener { _ ->

                    VibrationHelper.vibrateOnLongPress(viewModel)
                    updateSearchHistory(tool.name)
                    ClipboardHelper.copyToClipboard(
                        context = myActivity,
                        text = SchemeHelper.getSchemeFromId(tool.id),
                        toastString = tool.name,
                        secondToastString = myActivity.getString(
                            R.string.link
                        ),
                    )
                    true
                }
            }


            private fun setDimmingEffect(holder: ViewHolder, shouldDim: Boolean) {
                // 调整亮度为原来的比例(透明度)
                val alpha = UI.Alpha.ALPHA_7
                val originAlpha = UI.Alpha.ALPHA_10

                if(shouldDim) {
                    holder.itemView.alpha = alpha
                } else {
                    holder.itemView.alpha = originAlpha
                }
            }

        }


        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            if(holder is ChildViewHolder) {
                holder.imageViewIcon.cancelLoadImage()
            }
        }


        inner class SeparatorViewHolder(itemView: View) : ViewHolder(itemView) {
//            private val textViewSeparator: TextView = itemView.findViewById(R.id.textView_separator)
//
//            fun bind(separator: AppSeparator) {
//                textViewSeparator.text = separator.titleString
//            }
        }


        inner class BottomSpaceViewHolder(itemView: View) : ViewHolder(itemView) {
//
        }


        inner class HistoryOrSuggestionViewHolder(itemView: View) : ViewHolder(itemView) {
            private val textView: TextView = itemView.findViewById(R.id.textView_name)

            fun bind(text: String) {
                textView.text = text
                itemView.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    setSearchBoxText(text)
                    updateSearchHistoryAndSuggestion(text)
                }
            }
        }


        inner class SearchTitleViewHolder(itemView: View) : ViewHolder(itemView) {
            private val textView: TextView = itemView.findViewById(R.id.textView_name)
            private val imageButtonClear: ImageButton =
                itemView.findViewById(R.id.imageButton_clear)

            fun bind(text: String) {
                textView.text = text
                itemView.setOnClickListener {
                    emptyAreaClickAction()
                }
                imageButtonClear.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    DialogHelper.setAndShowDefaultDialog(
                        context = context,
                        viewModel = viewModel,
                        messageText = context.getString(
                            R.string.confirm_clear_something,
                            text,
                        ),
                        positiveAction = {
                            clearSearchHistoryOrSuggestion(text)
                        },
                    )
                }
            }
        }

    }

}
