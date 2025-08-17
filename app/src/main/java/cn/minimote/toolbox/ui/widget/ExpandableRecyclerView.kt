/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.constant.ToolID
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.BottomSheetDialogHelper
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.ConfigHelper.saveUserConfig
import cn.minimote.toolbox.helper.ConfigHelper.updateConfigValue
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.SearchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ExpandableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {


    companion object {
        private const val VIEW_TYPE_GROUP = 0
        private const val VIEW_TYPE_CHILD = 1
        private const val VIEW_TYPE_SEPARATOR = 2
        private const val VIEW_TYPE_NO_RESULT = 3
    }


    private lateinit var adapter: ExpandableAdapter
    private lateinit var viewModel: MyViewModel
    private lateinit var myActivity: MainActivity
    private var isSchemeList: Boolean = false
    private lateinit var configKey: String
    var searchMode: Boolean = false

    fun setParameters(
        viewModel: MyViewModel,
        myActivity: MainActivity,
        isSchemeList: Boolean,
    ) {
        this.viewModel = viewModel
        this.myActivity = myActivity
        this.isSchemeList = isSchemeList

        configKey = if(isSchemeList) {
            Config.ConfigKeys.CollapsedGroups.SCHEME_LIST
        } else {
            Config.ConfigKeys.CollapsedGroups.TOOL_LIST
        }

        // 初始化适配器
        adapter = ExpandableAdapter()

        // 手表使用 LinearLayoutManager
        if(viewModel.isWatch) {
            layoutManager = LinearLayoutManager(context)
        } else {
            // 使用 FlexboxLayoutManager 实现自适应列数
            val flexboxLayoutManager = FlexboxLayoutManager(context)
            flexboxLayoutManager.flexDirection = FlexDirection.ROW
            flexboxLayoutManager.justifyContent = JustifyContent.CENTER
            flexboxLayoutManager.alignItems = AlignItems.CENTER
            flexboxLayoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行

            layoutManager = flexboxLayoutManager
        }


        // 设置带有展开/折叠动画的 ItemAnimator
//        itemAnimator = ExpandableItemAnimator()
        itemAnimator = null

        setAdapter(adapter)
    }

    fun setGroups(groups: List<ExpandableGroup>) {
        adapter.setGroups(groups)
    }


    fun inSearchMode(): Boolean {
//        return viewModel.searchMode.value == true
//        Toast.makeText(context, "inSearchMode: ${this@ExpandableRecyclerView.alpha}", Toast.LENGTH_SHORT).show()
//        return viewModel.searchMode.value == true && this@ExpandableRecyclerView.alpha != 1f
        return searchMode
    }


    fun updateSearchMode(searchMode: Boolean) {
        this.searchMode = searchMode
    }

//    fun setOnItemClickListener(listener: (Tool) -> Unit) {
//        adapter.setOnItemClickListener(listener)
//    }

//    /**
//     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
//     * 使用透明度和垂直缩放组合动画，模拟抽屉效果
//     */
//    inner class ExpandableItemAnimator : DefaultItemAnimator() {
//        override fun animateAdd(holder: ViewHolder): Boolean {
//            holder.itemView.alpha = 0f
//            holder.itemView.scaleY = 0f
//
//            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                duration = 250
//                interpolator = DecelerateInterpolator()
//                addUpdateListener { animation ->
//                    val fraction = animation.animatedFraction
//                    holder.itemView.alpha = fraction
//                    holder.itemView.scaleY = fraction
//                }
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        holder.itemView.alpha = 1f
//                        holder.itemView.scaleY = 1f
//                        dispatchAddFinished(holder)
//                    }
//                })
//            }
//
//            animator.start()
//            dispatchAddStarting(holder)
//            return true
//        }
//
//        override fun animateRemove(holder: ViewHolder): Boolean {
//            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                duration = 250
//                interpolator = DecelerateInterpolator()
//                addUpdateListener { animation ->
//                    val fraction = animation.animatedFraction
//                    holder.itemView.alpha = 1f - fraction
//                    holder.itemView.scaleY = 1f - fraction
//                }
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        holder.itemView.alpha = 1f
//                        holder.itemView.scaleY = 1f
//                        dispatchRemoveFinished(holder)
//                    }
//                })
//            }
//
//            animator.start()
//            dispatchRemoveStarting(holder)
//            return true
//        }
//
//        override fun isRunning(): Boolean {
//            return false
//        }
//    }


//    /**
//     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
//     * 组内元素整体上移并逐渐透明的动画效果，精确控制避免越界回弹
//     */
//    inner class ExpandableItemAnimator : DefaultItemAnimator() {
//        override fun animateAdd(holder: ViewHolder): Boolean {
//            // 取消可能正在进行的动画
//            holder.itemView.animate().cancel()
//
//            // 展开时：从透明和稍微向下位置开始，然后移动到正常位置
//            holder.itemView.alpha = 0f
////            holder.itemView.translationY = 30f
//
//            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                duration = 600
//                // 使用加速减速插值器
//                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
//                addUpdateListener { animation ->
//                    val fraction = animation.animatedFraction
//                    holder.itemView.alpha = fraction
////                    holder.itemView.translationY = 30f * (1 - fraction)
//                }
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        holder.itemView.alpha = 1f
//                        holder.itemView.translationY = 0f
//                        dispatchAddFinished(holder)
//                    }
//                })
//            }
//
//            animator.start()
//            dispatchAddStarting(holder)
//            return true
//        }
//
//        override fun animateRemove(holder: ViewHolder): Boolean {
//            // 取消可能正在进行的动画
//            holder.itemView.animate().cancel()
//
//            // 折叠时：向上移动并逐渐变透明
//            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                duration = 400
//                // 使用加速减速插值器
//                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
//                addUpdateListener { animation ->
//                    val fraction = animation.animatedFraction
//                    holder.itemView.alpha = 1f - fraction
////                    holder.itemView.translationY = -30f * fraction
//                }
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        holder.itemView.alpha = 1f
//                        holder.itemView.translationY = 0f
//                        dispatchRemoveFinished(holder)
//                    }
//                })
//            }
//
//            animator.start()
//            dispatchRemoveStarting(holder)
//            return true
//        }
//
//        override fun isRunning(): Boolean {
//            return false
//        }
//    }


//

    /**
     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
     * 组内元素整体上移并逐渐透明的动画效果，使用线性插值避免越界回弹
     */
//    inner class ExpandableItemAnimator : DefaultItemAnimator() {
//        override fun animateAdd(holder: ViewHolder): Boolean {
//            // 展开时：从透明和稍微向下位置开始，然后移动到正常位置
//            holder.itemView.alpha = 0f
//            holder.itemView.translationY = 0f
//
//            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                duration = 300
//                interpolator = android.view.animation.LinearInterpolator() // 使用线性插值器
//                addUpdateListener { animation ->
//                    val fraction = animation.animatedFraction
//                    holder.itemView.alpha = fraction
//                    holder.itemView.translationY = 0f * (1 - fraction)
//                }
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        holder.itemView.alpha = 1f
//                        holder.itemView.translationY = 0f
//                        dispatchAddFinished(holder)
//                    }
//                })
//            }
//
//            animator.start()
//            dispatchAddStarting(holder)
//            return true
//        }
//
//        override fun animateRemove(holder: ViewHolder): Boolean {
//            // 折叠时：向上移动并逐渐变透明
//            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
//                duration = 300
//                interpolator = android.view.animation.LinearInterpolator() // 使用线性插值器
//                addUpdateListener { animation ->
//                    val fraction = animation.animatedFraction
//                    holder.itemView.alpha = 1f - fraction
//                    holder.itemView.translationY = -30f * fraction
//                }
//                addListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        holder.itemView.alpha = 1f
//                        holder.itemView.translationY = 0f
//                        dispatchRemoveFinished(holder)
//                    }
//                })
//            }
//
//            animator.start()
//            dispatchRemoveStarting(holder)
//            return true
//        }
//
//        override fun isRunning(): Boolean {
//            return false
//        }
//    }


    inner class ExpandableAdapter : Adapter<ViewHolder>() {

        private var groups = listOf<ExpandableGroup>()
        private var collapsedGroups = mutableSetOf<String>()
        private var onItemClickListener: ((Tool) -> Unit)? = null

        @SuppressLint("NotifyDataSetChanged")
        fun setGroups(newGroups: List<ExpandableGroup>) {
            groups = newGroups

            // 初始化展开状态
//            collapsedGroups = configValue.toMutableSet()
            collapsedGroups.clear()
            if(!inSearchMode()) {
                getSavedCollapsedGroups()
            }

//            groups.forEachIndexed { index, group ->
//                if(!group.isExpanded) {
//                    collapsedGroups.add(group.titleString)
//                }
//            }

            notifyDataSetChanged()
        }

        // 获取当前折叠的组
        fun getCollapsedGroups(): List<String> {
            return collapsedGroups.toList()
        }

        // 获取保存的折叠组
        fun getSavedCollapsedGroups() {
            val configValue = viewModel.getConfigValue(
                key = configKey,
            )
            if(configValue is org.json.JSONArray) {
                // 如果是JSONArray类型，逐个提取字符串
                for(i in 0 until configValue.length()) {
                    val item = configValue.opt(i)
                    if(item is String) {
                        collapsedGroups.add(item)
                    }
                }
            }
        }

//        fun setOnItemClickListener(listener: (Tool) -> Unit) {
//            onItemClickListener = listener
//        }

        override fun getItemViewType(position: Int): Int {
            val (item, _) = getItemWithGroupIndex(position)
            return when(item) {
                is ExpandableGroup -> {
                    if(item.titleString == context.getString(R.string.no_result)) {
                        VIEW_TYPE_NO_RESULT
                    } else {
                        VIEW_TYPE_GROUP
                    }
                }

                is Tool -> {
                    if(item.id != ToolID.BLANK) {
                        VIEW_TYPE_CHILD
                    } else {
                        VIEW_TYPE_SEPARATOR
                    }
                }

                else -> {
                    VIEW_TYPE_SEPARATOR
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return when(viewType) {
                VIEW_TYPE_GROUP -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool_title, parent, false)
                    GroupViewHolder(view)
                }

                VIEW_TYPE_CHILD -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool, parent, false)
                    ChildViewHolder(view)
                }

                VIEW_TYPE_SEPARATOR -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_tool_separator, parent, false)
                    SeparatorViewHolder(view)
                }

                VIEW_TYPE_NO_RESULT -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_setting_title, parent, false)
                    NoResultViewHolder(view)
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
                        group,
                        groupIndex,
                    ) {
                        toggleGroup(groupIndex)
                    }
                }

                is ChildViewHolder -> {
                    val child = item as Tool
                    holder.bind(child) {
                        onItemClickListener?.invoke(child)
                    }
                }

                is SeparatorViewHolder -> {
//                    val separator = item as AppSeparator
//                    holder.bind(separator)
                }


                is NoResultViewHolder -> {
                    holder.bind()
                }
            }
        }

        override fun getItemCount(): Int {
            var count = 0
            groups.forEachIndexed { index, group ->
                // Always count the group header
                count++

                // Count children if group is expanded
                if(!collapsedGroups.contains(group.titleString)) {
                    count += group.dataList.size
                }
            }
            return count
        }

        private fun getItemWithGroupIndex(position: Int): Pair<Any, Int> {
            var currentPosition = 0

            for((groupIndex, group) in groups.withIndex()) {
                // Check if current position is the group header
                if(currentPosition == position) {
                    return Pair(group, groupIndex)
                }

                currentPosition++

                // If group is expanded, check children
                if(!collapsedGroups.contains(group.titleString)) {
                    val childrenCount = group.dataList.size
                    if(position < currentPosition + childrenCount) {
                        val childIndex = position - currentPosition
                        return Pair(group.dataList[childIndex], groupIndex)
                    }
                    currentPosition += childrenCount
                }
            }

            throw IndexOutOfBoundsException("Position $position is out of bounds")
        }


        private fun toggleGroup(groupIndex: Int) {
            val startPosition = getGroupStartPosition(groupIndex)
//            Toast.makeText(
//                context,
//                "startPosition: $startPosition, cnt=${groups[groupIndex].dataList.size}",
//                Toast.LENGTH_SHORT
//            ).show()

            if(!collapsedGroups.contains(groups[groupIndex].titleString)) {
                collapsedGroups.add(groups[groupIndex].titleString)
                notifyItemRangeInserted(
                    startPosition + 1,
                    groups[groupIndex].dataList.size,
                )
                // 只通知该组范围内的项目变化
            } else {
                collapsedGroups.remove(groups[groupIndex].titleString)
                // 只通知该组范围内的项目变化
                notifyItemRangeRemoved(
                    startPosition + 1,
                    groups[groupIndex].dataList.size,
                )
            }

            // 使用 notifyDataSetChanged() 代替范围通知，避免闪烁
//            adapter.notifyDataSetChanged()
            // 更新组标题本身
            notifyItemChanged(startPosition)
            if(!inSearchMode()) {
                saveCollapsedGroups()
            }

        }


        // 保存折叠状态到配置中
        private fun saveCollapsedGroups() {
            viewModel.updateConfigValue(
                key = configKey,
                value = getCollapsedGroups(),
            )
            viewModel.saveUserConfig()
        }


        private fun getGroupStartPosition(groupIndex: Int): Int {
            var position = 0
            for(i in 0 until groupIndex) {
                position++ // Group header
                if(!collapsedGroups.contains(groups[i].titleString)) {
                    position += groups[i].dataList.size // Expanded children
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
                textViewName.text = group.titleString
//                if(group.titleString == context.getString(ToolList.GroupNameId.addLocalApp)) {
//                    // 如果是添加本机软件，将图标设置为加号
//                    imageViewGroupIndicator.setImageResource(R.drawable.ic_add)
//
//                    // 点击后跳转到软件列表
//                    itemView.setOnClickListener {
//                        VibrationHelper.vibrateOnClick(viewModel)
//                        FragmentHelper.switchFragment(
//                            fragmentName = FragmentName.INSTALLED_APP_LIST_FRAGMENT,
//                            fragmentManager = myActivity.supportFragmentManager,
//                            viewModel = viewModel,
//                            viewPager = myActivity.viewPager,
//                            constraintLayoutOrigin = myActivity.constraintLayoutOrigin,
//                        )
//                    }
//                } else {
                imageViewGroupIndicator.setImageResource(R.drawable.ic_arrow_right)
                // 根据展开状态设置图标旋转角度
                val isExpanded = !collapsedGroups.contains(groups[groupIndex].titleString)
                imageViewGroupIndicator.rotation = if(isExpanded) 90f else 0f

                itemView.setOnClickListener {
                    if(this@ExpandableRecyclerView.alpha != 1f) {
                        viewModel.searchMode.value = false
                    } else {
                        VibrationHelper.vibrateOnClick(viewModel)
                        // 执行指示器旋转动画
//                        animateIndicator(!isExpanded)
                        onGroupClick()
                    }
                }
//                }
            }


//            fun animateIndicator(expanded: Boolean) {
//                val endRotation = if(expanded) 90f else 0f
////                Toast.makeText(context, "expanded: $expanded", Toast.LENGTH_SHORT).show()
//                // 取消之前的动画，避免冲突
////                imageViewGroupIndicator.animate().cancel()
//
//                imageViewGroupIndicator.animate()
//                    .rotation(endRotation)
//                    .setDuration(250) // 动画时长
//                    .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator()) // 使用更平滑的插值器
//                    .start()
//            }


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
                textViewName.maxWidth =
                    (viewModel.screenWidth - resources.getDimensionPixelSize(R.dimen.layout_size_1_large) * 1.5).toInt()
                textViewDescription.maxWidth = textViewName.maxWidth
            }


            fun bind(tool: Tool, onItemClick: () -> Unit) {

                imageViewIcon.setImageDrawable(viewModel.iconCacheHelper.getCircularDrawable(tool))
                textViewName.text = SearchHelper.highlightSearchTermForDevice(
                    viewModel = viewModel,
                    text = tool.name,
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
                    )
                    textViewDescription.visibility = VISIBLE
                } else {
                    textViewDescription.visibility = GONE
                }

                updateLikeIcon(tool = tool, imageViewLikeIcon = imageViewLikeIcon)

                itemView.setOnClickListener {
                    if(this@ExpandableRecyclerView.alpha != 1f) {
                        viewModel.searchMode.value = false
                    } else {
                        VibrationHelper.vibrateOnClick(viewModel)
                        if(isSchemeList) {
                            Toast.makeText(
                                myActivity,
                                myActivity.getString(R.string.long_press_can_copy_scheme),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            LaunchHelper.launch(myActivity, viewModel, tool)
                        }
                        onItemClick()
                    }
                }

                // 取消长按震动
                itemView.isHapticFeedbackEnabled = false
                itemView.setOnLongClickListener { _ ->
                    if(this@ExpandableRecyclerView.alpha != 1f) {
                        return@setOnLongClickListener true
                    }

                    VibrationHelper.vibrateOnLongPress(viewModel)
                    if(isSchemeList) {
                        ClipboardHelper.copyToClipboard(
                            context = myActivity,
                            text = SchemeHelper.getSchemeFromId(tool.id),
                            toastString = tool.name + myActivity.getString(R.string.some_scheme),
                        )
                    } else {
                        BottomSheetDialogHelper.setAndShowBottomSheetDialog(
                            context = myActivity,
                            viewModel = viewModel,
                            tool = tool,
                            menuList = if(viewModel.isWatch) MenuList.tool_watch else MenuList.tool,
                            viewPager = myActivity.viewPager,
                            fragmentManager = myActivity.supportFragmentManager,
                            constraintLayoutOrigin = myActivity.constraintLayoutOrigin,
                            onMenuItemClick = {
                                when(it) {
                                    MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
                                        updateLikeIcon(
                                            tool = tool,
                                            imageViewLikeIcon = imageViewLikeIcon,
                                        )
                                    }
                                }
                            }
                        )
                    }
                    true
                }
            }


            private fun updateLikeIcon(tool: Tool, imageViewLikeIcon: ImageView) {
                if(shouldShowLikeIcon(tool)) {
                    imageViewLikeIcon.visibility = VISIBLE
                } else {
                    imageViewLikeIcon.visibility = INVISIBLE
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


        inner class SeparatorViewHolder(itemView: View) : ViewHolder(itemView) {
//            private val textViewSeparator: TextView = itemView.findViewById(R.id.textView_separator)
//
//            fun bind(separator: AppSeparator) {
//                textViewSeparator.text = separator.titleString
//            }
        }


        inner class NoResultViewHolder(itemView: View) : ViewHolder(itemView) {
            private val textViewNoResult: TextView = itemView.findViewById(R.id.textView_name)

            fun bind() {
                textViewNoResult.text = context.getString(R.string.no_result)
                // 设置字体样式为正常
                textViewNoResult.setTypeface(null, Typeface.NORMAL)
                // 设置字体颜色为灰色
                textViewNoResult.setTextColor(context.getColor(R.color.mid_gray))
            }
        }


    }

}
