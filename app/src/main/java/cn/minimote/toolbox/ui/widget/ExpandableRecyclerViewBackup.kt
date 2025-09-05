/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

//class ExpandableRecyclerViewBackup @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0,
//) : RecyclerView(context, attrs, defStyleAttr) {
//
//
//    private lateinit var adapter: ExpandableAdapter
//    private lateinit var viewModel: MyViewModel
//    private lateinit var myActivity: MainActivity
//    private lateinit var fragment: Fragment
//    private val query: String get() = fragment.searchQuery
//    private var isSchemeList: Boolean = false
//    private lateinit var configKey: String
//    var emptyAreaClickAction: () -> Unit = {}
////    var searchMode: Boolean = false
//
//
//    fun setParameters(
//        viewModel: MyViewModel,
//        myActivity: MainActivity,
//        isSchemeList: Boolean,
//        fragment: Fragment,
//        emptyAreaClickListener: () -> Unit,
//    ) {
//        this.viewModel = viewModel
//        this.myActivity = myActivity
//        this.isSchemeList = isSchemeList
//        this.fragment = fragment
//        this.emptyAreaClickAction = emptyAreaClickListener
//
//        configKey = if(isSchemeList) {
//            Config.ConfigKeys.CollapsedGroups.SCHEME_LIST
//        } else {
//            Config.ConfigKeys.CollapsedGroups.TOOL_LIST
//        }
//
//        // 初始化适配器
//        adapter = ExpandableAdapter()
//
//        // 手表使用 LinearLayoutManager
//        if(viewModel.isWatch) {
//            layoutManager = LinearLayoutManager(context)
//        } else {
//            // 使用 FlexboxLayoutManager 实现自适应列数
//            val flexboxLayoutManager = FlexboxLayoutManager(context)
//            flexboxLayoutManager.flexDirection = FlexDirection.ROW
//            flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
//            flexboxLayoutManager.alignItems = AlignItems.CENTER
//            flexboxLayoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行
//
//            layoutManager = flexboxLayoutManager
//        }
//
//
//        // 设置带有展开/折叠动画的 ItemAnimator
////        itemAnimator = ExpandableItemAnimator()
//        itemAnimator = null
//
//        setAdapter(adapter)
//
//        // 添加点击监听器到 RecyclerView 本身
////        this.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
////            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
////                val childView = rv.findChildViewUnder(e.x, e.y)
////                if(childView == null) {
////                    emptyAreaClickListener()
////                    return true
////                }
////                return false
////            }
////        })
//
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
//
////        this.setOnClickListener {
////            // 检查当前是否正在触摸 RecyclerView 本身而不是子项
////            if(this.isEmpty() ||
////                (this.getChildAt(0).y >= this.height) ||
////                (this.getChildAt(this.childCount - 1).y + this.getChildAt(this.childCount - 1).height <= 0)
////            ) {
////                emptyAreaClickListener()
////            }
////        }
//    }
//
//
//    fun setGroups(groups: List<ExpandableGroup>) {
//        adapter.setGroups(groups)
//    }
//
//
//    fun inSearchMode(): Boolean {
////        return viewModel.searchMode.value == true
////        Toast.makeText(context, "inSearchMode: ${this@ExpandableRecyclerView.alpha}", Toast.LENGTH_SHORT).show()
////        return viewModel.searchMode.value == true && this@ExpandableRecyclerView.alpha != 1f
////        return searchMode
//        return viewModel.searchMode.value == true
//    }
//
//
////    fun updateSearchMode(searchMode: Boolean) {
////        this.searchMode = searchMode
////    }
//
////    fun setOnItemClickListener(listener: (Tool) -> Unit) {
////        adapter.setOnItemClickListener(listener)
////    }
//
////    /**
////     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
////     * 使用透明度和垂直缩放组合动画，模拟抽屉效果
////     */
////    inner class ExpandableItemAnimator : DefaultItemAnimator() {
////        override fun animateAdd(holder: ViewHolder): Boolean {
////            holder.itemView.alpha = 0f
////            holder.itemView.scaleY = 0f
////
////            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
////                duration = 250
////                interpolator = DecelerateInterpolator()
////                addUpdateListener { animation ->
////                    val fraction = animation.animatedFraction
////                    holder.itemView.alpha = fraction
////                    holder.itemView.scaleY = fraction
////                }
////                addListener(object : AnimatorListenerAdapter() {
////                    override fun onAnimationEnd(animation: Animator) {
////                        holder.itemView.alpha = 1f
////                        holder.itemView.scaleY = 1f
////                        dispatchAddFinished(holder)
////                    }
////                })
////            }
////
////            animator.start()
////            dispatchAddStarting(holder)
////            return true
////        }
////
////        override fun animateRemove(holder: ViewHolder): Boolean {
////            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
////                duration = 250
////                interpolator = DecelerateInterpolator()
////                addUpdateListener { animation ->
////                    val fraction = animation.animatedFraction
////                    holder.itemView.alpha = 1f - fraction
////                    holder.itemView.scaleY = 1f - fraction
////                }
////                addListener(object : AnimatorListenerAdapter() {
////                    override fun onAnimationEnd(animation: Animator) {
////                        holder.itemView.alpha = 1f
////                        holder.itemView.scaleY = 1f
////                        dispatchRemoveFinished(holder)
////                    }
////                })
////            }
////
////            animator.start()
////            dispatchRemoveStarting(holder)
////            return true
////        }
////
////        override fun isRunning(): Boolean {
////            return false
////        }
////    }
//
//
////    /**
////     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
////     * 组内元素整体上移并逐渐透明的动画效果，精确控制避免越界回弹
////     */
////    inner class ExpandableItemAnimator : DefaultItemAnimator() {
////        override fun animateAdd(holder: ViewHolder): Boolean {
////            // 取消可能正在进行的动画
////            holder.itemView.animate().cancel()
////
////            // 展开时：从透明和稍微向下位置开始，然后移动到正常位置
////            holder.itemView.alpha = 0f
//////            holder.itemView.translationY = 30f
////
////            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
////                duration = 600
////                // 使用加速减速插值器
////                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
////                addUpdateListener { animation ->
////                    val fraction = animation.animatedFraction
////                    holder.itemView.alpha = fraction
//////                    holder.itemView.translationY = 30f * (1 - fraction)
////                }
////                addListener(object : AnimatorListenerAdapter() {
////                    override fun onAnimationEnd(animation: Animator) {
////                        holder.itemView.alpha = 1f
////                        holder.itemView.translationY = 0f
////                        dispatchAddFinished(holder)
////                    }
////                })
////            }
////
////            animator.start()
////            dispatchAddStarting(holder)
////            return true
////        }
////
////        override fun animateRemove(holder: ViewHolder): Boolean {
////            // 取消可能正在进行的动画
////            holder.itemView.animate().cancel()
////
////            // 折叠时：向上移动并逐渐变透明
////            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
////                duration = 400
////                // 使用加速减速插值器
////                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
////                addUpdateListener { animation ->
////                    val fraction = animation.animatedFraction
////                    holder.itemView.alpha = 1f - fraction
//////                    holder.itemView.translationY = -30f * fraction
////                }
////                addListener(object : AnimatorListenerAdapter() {
////                    override fun onAnimationEnd(animation: Animator) {
////                        holder.itemView.alpha = 1f
////                        holder.itemView.translationY = 0f
////                        dispatchRemoveFinished(holder)
////                    }
////                })
////            }
////
////            animator.start()
////            dispatchRemoveStarting(holder)
////            return true
////        }
////
////        override fun isRunning(): Boolean {
////            return false
////        }
////    }
//
//
////
//
//    /**
//     * 自定义 ItemAnimator，为 ExpandableRecyclerView 提供展开和折叠动画
//     * 组内元素整体上移并逐渐透明的动画效果，使用线性插值避免越界回弹
//     */
////    inner class ExpandableItemAnimator : DefaultItemAnimator() {
////        override fun animateAdd(holder: ViewHolder): Boolean {
////            // 展开时：从透明和稍微向下位置开始，然后移动到正常位置
////            holder.itemView.alpha = 0f
////            holder.itemView.translationY = 0f
////
////            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
////                duration = 300
////                interpolator = android.view.animation.LinearInterpolator() // 使用线性插值器
////                addUpdateListener { animation ->
////                    val fraction = animation.animatedFraction
////                    holder.itemView.alpha = fraction
////                    holder.itemView.translationY = 0f * (1 - fraction)
////                }
////                addListener(object : AnimatorListenerAdapter() {
////                    override fun onAnimationEnd(animation: Animator) {
////                        holder.itemView.alpha = 1f
////                        holder.itemView.translationY = 0f
////                        dispatchAddFinished(holder)
////                    }
////                })
////            }
////
////            animator.start()
////            dispatchAddStarting(holder)
////            return true
////        }
////
////        override fun animateRemove(holder: ViewHolder): Boolean {
////            // 折叠时：向上移动并逐渐变透明
////            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
////                duration = 300
////                interpolator = android.view.animation.LinearInterpolator() // 使用线性插值器
////                addUpdateListener { animation ->
////                    val fraction = animation.animatedFraction
////                    holder.itemView.alpha = 1f - fraction
////                    holder.itemView.translationY = -30f * fraction
////                }
////                addListener(object : AnimatorListenerAdapter() {
////                    override fun onAnimationEnd(animation: Animator) {
////                        holder.itemView.alpha = 1f
////                        holder.itemView.translationY = 0f
////                        dispatchRemoveFinished(holder)
////                    }
////                })
////            }
////
////            animator.start()
////            dispatchRemoveStarting(holder)
////            return true
////        }
////
////        override fun isRunning(): Boolean {
////            return false
////        }
////    }
//
//
//    inner class ExpandableAdapter : Adapter<ViewHolder>() {
//
//        private var groups = listOf<ExpandableGroup>()
//        private var collapsedGroups = mutableSetOf<String>()
//        private var onItemClickListener: ((Tool) -> Unit)? = null
//
//        @SuppressLint("NotifyDataSetChanged")
//        fun setGroups(newGroups: List<ExpandableGroup>) {
//            groups = newGroups
//
//            // 初始化展开状态
////            collapsedGroups = configValue.toMutableSet()
//            collapsedGroups.clear()
////            Toast.makeText(context, "获取${!inSearchMode()}", Toast.LENGTH_SHORT).show()
//            if(!inSearchMode()) {
//                getSavedCollapsedGroups()
//            }
//
////            groups.forEachIndexed { index, group ->
////                if(!group.isExpanded) {
////                    collapsedGroups.add(group.titleString)
////                }
////            }
//
//            notifyDataSetChanged()
//        }
//
//        // 获取当前折叠的组
//        fun getCollapsedGroups(): List<String> {
//            return collapsedGroups.toList()
//        }
//
//        // 获取保存的折叠组
//        fun getSavedCollapsedGroups() {
//            val configValue = viewModel.getConfigValue(
//                key = configKey,
//            )
//            if(configValue is JSONArray) {
//                // 如果是JSONArray类型，逐个提取字符串
//                for(i in 0 until configValue.length()) {
//                    val item = configValue.opt(i)
//                    if(item is String) {
//                        collapsedGroups.add(item)
//                    }
//                }
//            }
//        }
//
////        fun setOnItemClickListener(listener: (Tool) -> Unit) {
////            onItemClickListener = listener
////        }
//
//        override fun getItemViewType(position: Int): Int {
//            val (item, _) = getItemWithGroupIndex(position)
//            return when(item) {
//                is ExpandableGroup -> {
//                    when(item.titleString) {
//                        context.getString(R.string.no_result) -> {
//                            ViewTypes.ToolList.NO_RESULT
//                        }
//
//                        context.getString(R.string.search_history),
//                        context.getString(R.string.search_suggestion),
//                            -> {
//                            ViewTypes.ToolList.SEARCH_TITLE
//                        }
//
//                        else -> {
//                            ViewTypes.ToolList.GROUP
//                        }
//                    }
//                }
//
//                is Tool -> {
//                    if(item.id != ToolID.BLANK) {
//                        ViewTypes.ToolList.CHILD
//                    } else {
//                        ViewTypes.ToolList.SEPARATOR
//                    }
//                }
//
//                is String -> {
//                    ViewTypes.ToolList.HISTORY_OR_SUGGESTION
//                }
//
//                else -> {
//                    ViewTypes.ToolList.SEPARATOR
//                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            return when(viewType) {
//                ViewTypes.ToolList.GROUP -> {
//                    val view = LayoutInflater.from(parent.context)
//                        .inflate(R.layout.item_tool_title, parent, false)
//                    GroupViewHolder(view)
//                }
//
//                ViewTypes.ToolList.CHILD -> {
//                    val view = LayoutInflater.from(parent.context)
//                        .inflate(R.layout.item_tool, parent, false)
//                    ChildViewHolder(view)
//                }
//
//                ViewTypes.ToolList.SEPARATOR -> {
//                    val view = LayoutInflater.from(parent.context)
//                        .inflate(R.layout.item_tool_separator, parent, false)
//                    SeparatorViewHolder(view)
//                }
//
//                ViewTypes.ToolList.NO_RESULT -> {
//                    val view = LayoutInflater.from(parent.context)
//                        .inflate(R.layout.item_setting_title, parent, false)
//                    NoResultViewHolder(view)
//                }
//
//                ViewTypes.ToolList.HISTORY_OR_SUGGESTION -> {
//                    val view = LayoutInflater.from(parent.context)
//                        .inflate(R.layout.item_search_history_or_suggestion, parent, false)
//                    HistoryOrSuggestionViewHolder(view)
//                }
//
//                ViewTypes.ToolList.SEARCH_TITLE -> {
//                    val view = LayoutInflater.from(parent.context)
//                        .inflate(R.layout.item_search_title, parent, false)
//                    SearchTitleViewHolder(view)
//                }
//
//                else -> throw IllegalArgumentException("未知的类型 type: $viewType")
//            }
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            val (item, groupIndex) = getItemWithGroupIndex(position)
//
//            when(holder) {
//                is GroupViewHolder -> {
//                    val group = item as ExpandableGroup
//                    holder.bind(
//                        group,
//                        groupIndex,
//                    ) {
//                        toggleGroup(groupIndex)
//                    }
//                }
//
//                is ChildViewHolder -> {
//                    val child = item as Tool
//                    holder.bind(child) {
//                        onItemClickListener?.invoke(child)
//                    }
//                }
//
//                is SeparatorViewHolder -> {
////                    val separator = item as AppSeparator
////                    holder.bind(separator)
//                }
//
//                is NoResultViewHolder -> {
//                    holder.bind()
//                }
//
//                is HistoryOrSuggestionViewHolder -> {
//                    holder.bind(item as String)
//                }
//
//                is SearchTitleViewHolder -> {
//                    holder.bind((item as ExpandableGroup).titleString)
//                }
//            }
//        }
//
//
//        override fun getItemCount(): Int {
//            var count = 0
//            groups.forEachIndexed { index, group ->
//                // 只有当组内有元素或组名需要显示时才计算
//                if(shouldShowGroup(group)) {
//                    // Always count the group header if it should be shown
//                    count++
//
//                    // Count children if group is expanded
//                    if(!collapsedGroups.contains(group.titleString)) {
//                        count += group.dataList.size
//                    }
//                }
//            }
//            return count
//        }
//
//
//        private fun shouldShowGroup(group: ExpandableGroup): Boolean {
//            // 对于没有子元素的组，不显示组标题，除非是特殊组
//            return group.dataList.isNotEmpty() ||
//                    group.titleString == context.getString(R.string.no_result)
//        }
//
//
//        private fun getItemWithGroupIndex(position: Int): Pair<Any, Int> {
//            var currentPosition = 0
//
//            for((groupIndex, group) in groups.withIndex()) {
//                // 只处理应该显示的组
//                if(!shouldShowGroup(group)) {
//                    continue
//                }
//
//                // Check if current position is the group header
//                if(currentPosition == position) {
//                    return Pair(group, groupIndex)
//                }
//
//                currentPosition++
//
//                // If group is expanded, check children
//                if(!collapsedGroups.contains(group.titleString)) {
//                    val childrenCount = group.dataList.size
//                    if(position < currentPosition + childrenCount) {
//                        val childIndex = position - currentPosition
//                        return Pair(group.dataList[childIndex], groupIndex)
//                    }
//                    currentPosition += childrenCount
//                }
//            }
//
//            throw IndexOutOfBoundsException("位置 $position 越界")
//        }
//
//
//        private fun toggleGroup(groupIndex: Int) {
//            val startPosition = getGroupStartPosition(groupIndex)
////            Toast.makeText(
////                context,
////                "startPosition: $startPosition, cnt=${groups[groupIndex].dataList.size}",
////                Toast.LENGTH_SHORT
////            ).show()
//
//            if(!collapsedGroups.contains(groups[groupIndex].titleString)) {
//                collapsedGroups.add(groups[groupIndex].titleString)
//                notifyItemRangeInserted(
//                    startPosition + 1,
//                    groups[groupIndex].dataList.size,
//                )
//                // 只通知该组范围内的项目变化
//            } else {
//                collapsedGroups.remove(groups[groupIndex].titleString)
//                // 只通知该组范围内的项目变化
//                notifyItemRangeRemoved(
//                    startPosition + 1,
//                    groups[groupIndex].dataList.size,
//                )
//            }
//
//            // 使用 notifyDataSetChanged() 代替范围通知，避免闪烁
////            adapter.notifyDataSetChanged()
//            // 更新组标题本身
//            notifyItemChanged(startPosition)
////            Toast.makeText(context, "保存:${!inSearchMode()}", Toast.LENGTH_SHORT).show()
//            if(!inSearchMode()) {
//                saveCollapsedGroups()
//            }
//
//        }
//
//
//        // 保存折叠状态到配置中
//        private fun saveCollapsedGroups() {
//            viewModel.updateConfigValue(
//                key = configKey,
//                value = getCollapsedGroups(),
//            )
//            viewModel.saveUserConfig()
//        }
//
//
//        // 更新搜索历史
//        private fun updateSearchHistory(text: String) {
//            if(inSearchMode()) {
//                fragment.updateSearchHistoryAndSuggestion(text)
//            }
//        }
//
//
//        private fun getGroupStartPosition(groupIndex: Int): Int {
//            var position = 0
//            for(i in 0 until groupIndex) {
//                // 只计算应该显示的组
//                if(shouldShowGroup(groups[i])) {
//                    position++ // Group header
//                    if(!collapsedGroups.contains(groups[i].titleString)) {
//                        position += groups[i].dataList.size // Expanded children
//                    }
//                }
//            }
//            return position
//        }
//
//
//        inner class GroupViewHolder(itemView: View) : ViewHolder(itemView) {
//            private val textViewName: TextView = itemView.findViewById(R.id.textView_name)
//            private val imageViewGroupIndicator: ImageView =
//                itemView.findViewById(R.id.group_indicator)
//
//            fun bind(
//                group: ExpandableGroup,
//                groupIndex: Int,
//                onGroupClick: () -> Unit,
//            ) {
//                // 如果组内没有元素，隐藏组标题
//                if(!shouldShowGroup(group)) {
//                    itemView.visibility = GONE
//                    return
//                } else {
//                    itemView.visibility = VISIBLE
//                }
//
//                textViewName.text = group.titleString
////                if(group.titleString == context.getString(ToolList.GroupNameId.addLocalApp)) {
////                    // 如果是添加本机软件，将图标设置为加号
////                    imageViewGroupIndicator.setImageResource(R.drawable.ic_add)
////
////                    // 点击后跳转到软件列表
////                    itemView.setOnClickListener {
////                        VibrationHelper.vibrateOnClick(viewModel)
////                        FragmentHelper.switchFragment(
////                            fragmentName = FragmentName.INSTALLED_APP_LIST_FRAGMENT,
////                            fragmentManager = myActivity.supportFragmentManager,
////                            viewModel = viewModel,
////                            viewPager = myActivity.viewPager,
////                            constraintLayoutOrigin = myActivity.constraintLayoutOrigin,
////                        )
////                    }
////                } else {
//                imageViewGroupIndicator.setImageResource(R.drawable.ic_arrow_right)
//                // 根据展开状态设置图标旋转角度
//                val isExpanded = !collapsedGroups.contains(groups[groupIndex].titleString)
//                imageViewGroupIndicator.rotation = if(isExpanded) 90f else 0f
//
//                itemView.setOnClickListener {
//                    VibrationHelper.vibrateOnClick(viewModel)
//                    // 执行指示器旋转动画
////                    animateIndicator(!isExpanded)
//                    onGroupClick()
//                }
////                }
//            }
//
//
////            fun animateIndicator(expanded: Boolean) {
////                val endRotation = if(expanded) 90f else 0f
//////                Toast.makeText(context, "expanded: $expanded", Toast.LENGTH_SHORT).show()
////                // 取消之前的动画，避免冲突
//////                imageViewGroupIndicator.animate().cancel()
////
////                imageViewGroupIndicator.animate()
////                    .rotation(endRotation)
////                    .setDuration(250) // 动画时长
////                    .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator()) // 使用更平滑的插值器
////                    .start()
////            }
//
//
//        }
//
//
//        inner class ChildViewHolder(itemView: View) : ViewHolder(itemView) {
//
//            val imageViewIcon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
//            val imageViewLikeIcon: ImageView = itemView.findViewById(R.id.imageView_heart)
//            val textViewName: TextView = itemView.findViewById(R.id.textView_app_name)
//            val textViewDescription: TextView = itemView.findViewById(R.id.textView_activity_name)
//
//
//            init {
//                if(viewModel.isWatch) {
//                    itemView.layoutParams.width = LayoutParams.MATCH_PARENT
//                } else {
//                    val layoutParams = itemView.layoutParams as? FlexboxLayoutManager.LayoutParams
//                    layoutParams?.flexGrow = 1f
//                }
//
//                // 设置最大宽度
//                textViewName.maxWidth =
//                    (viewModel.screenWidth - resources.getDimensionPixelSize(R.dimen.layout_size_1_large) * 1.5).toInt()
//                textViewDescription.maxWidth = textViewName.maxWidth
//            }
//
//
//            fun bind(tool: Tool, onItemClick: () -> Unit) {
//
//                imageViewIcon.setImageDrawable(viewModel.iconCacheHelper.getCircularDrawable(tool))
//                textViewName.text = SearchHelper.highlightSearchTermForDevice(
//                    viewModel = viewModel,
//                    text = tool.name,
//                    query = query,
//                )
//
//                val descriptionText = if(isSchemeList) {
//                    SchemeHelper.getSchemeFromId(tool.id)
//                } else {
//                    tool.description.orEmpty().trim()
//                }
//                if(descriptionText.isNotBlank()) {
//                    textViewDescription.text = SearchHelper.highlightSearchTermForDevice(
//                        viewModel = viewModel,
//                        text = descriptionText,
//                        query = query,
//                    )
//                    textViewDescription.visibility = VISIBLE
//                } else {
//                    textViewDescription.visibility = GONE
//                }
//
//                updateLikeIcon(tool = tool, imageViewLikeIcon = imageViewLikeIcon)
//
//                itemView.setOnClickListener {
//                    VibrationHelper.vibrateOnClick(viewModel)
//                    updateSearchHistory(tool.name)
//                    if(isSchemeList) {
//                        Toast.makeText(
//                            myActivity,
//                            myActivity.getString(R.string.long_press_can_copy_scheme),
//                            Toast.LENGTH_SHORT,
//                        ).show()
//                    } else {
//                        LaunchHelper.launch(myActivity, viewModel, tool)
//                    }
//                    onItemClick()
//                }
//
//
//                // 取消长按震动
//                itemView.isHapticFeedbackEnabled = false
//                itemView.setOnLongClickListener { _ ->
//                    if(this@ExpandableRecyclerViewBackup.alpha != 1f) {
//                        return@setOnLongClickListener true
//                    }
//
//                    VibrationHelper.vibrateOnLongPress(viewModel)
//                    updateSearchHistory(tool.name)
//                    if(isSchemeList) {
//                        ClipboardHelper.copyToClipboard(
//                            context = myActivity,
//                            text = SchemeHelper.getSchemeFromId(tool.id),
//                            toastString = tool.name + myActivity.getString(R.string.some_scheme),
//                        )
//                    } else {
//                        BottomSheetDialogHelper.setAndShowBottomSheetDialog(
//                            viewModel = viewModel,
//                            activity = myActivity,
//                            tool = tool,
//                            menuList = if(viewModel.isWatch) MenuList.tool_watch else MenuList.tool,
//                            onMenuItemClick = {
//                                when(it) {
//                                    MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
//                                        updateLikeIcon(
//                                            tool = tool,
//                                            imageViewLikeIcon = imageViewLikeIcon,
//                                        )
//                                    }
//                                }
//                            }
//                        )
//                    }
//                    true
//                }
//            }
//
//
//            private fun updateLikeIcon(tool: Tool, imageViewLikeIcon: ImageView) {
//                if(shouldShowLikeIcon(tool)) {
//                    imageViewLikeIcon.visibility = VISIBLE
//                } else {
//                    imageViewLikeIcon.visibility = INVISIBLE
//                }
//            }
//
//
//            private fun shouldShowLikeIcon(tool: Tool): Boolean {
//                return !isSchemeList
//                        && viewModel.inSizeChangeMap(tool.id)
//                        && viewModel.getConfigValue(
//                    Config.ConfigKeys.Display.SHOW_LIKE_ICON
//                ) == true
//            }
//        }
//
//
//        inner class SeparatorViewHolder(itemView: View) : ViewHolder(itemView) {
////            private val textViewSeparator: TextView = itemView.findViewById(R.id.textView_separator)
////
////            fun bind(separator: AppSeparator) {
////                textViewSeparator.text = separator.titleString
////            }
//        }
//
//
//        inner class NoResultViewHolder(itemView: View) : ViewHolder(itemView) {
//            private val textViewNoResult: TextView = itemView.findViewById(R.id.textView_name)
//
//            fun bind() {
//                textViewNoResult.text = context.getString(R.string.no_result)
//                // 设置字体样式为正常
//                textViewNoResult.setTypeface(null, Typeface.NORMAL)
//                // 设置字体颜色为灰色
//                textViewNoResult.setTextColor(context.getColor(R.color.mid_gray))
//
//                itemView.setOnClickListener {
//                    emptyAreaClickAction()
//                }
//            }
//        }
//
//
//        inner class HistoryOrSuggestionViewHolder(itemView: View) : ViewHolder(itemView) {
//            private val textView: TextView = itemView.findViewById(R.id.textView_name)
//
//            fun bind(text: String) {
//                textView.text = text
//                itemView.setOnClickListener {
//                    VibrationHelper.vibrateOnClick(viewModel)
//                    fragment.setSearchBoxText(text)
//                    fragment.updateSearchHistoryAndSuggestion(text)
//                }
//            }
//        }
//
//
//        inner class SearchTitleViewHolder(itemView: View) : ViewHolder(itemView) {
//            private val textView: TextView = itemView.findViewById(R.id.textView_name)
//            private val imageButtonClear: ImageButton = itemView.findViewById(R.id.imageButton_clear)
//
//            fun bind(text: String) {
//                textView.text = text
//                itemView.setOnClickListener {
//                    emptyAreaClickAction()
//                }
//                imageButtonClear.setOnClickListener {
//                    VibrationHelper.vibrateOnClick(viewModel)
//                    DialogHelper.showConfirmDialog(
//                        context = context,
//                        viewModel = viewModel,
//                        messageText = context.getString(
//                            R.string.confirm_clear_something,
//                            text,
//                        ),
//                        positiveAction = {
//                            fragment.clearSearchHistoryOrSuggestion(text)
//                        },
//                    )
//                }
//            }
//        }
//
//    }
//
//}
