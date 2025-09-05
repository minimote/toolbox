/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter


//open class ExpandableAdapter(
//    protected val viewModel: MyViewModel,
//    protected val configKey: String,
//) : RecyclerView.Adapter<ViewHolder>() {
//
//    protected val context = viewModel.myContext
//
//    protected var groups = listOf<ExpandableGroup>()
//    protected var collapsedGroups = mutableSetOf<String>()
//    protected var onItemClickListener: ((Any) -> Unit)? = null
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun setGroups(newGroups: List<ExpandableGroup>) {
//        groups = newGroups
//
//        // 初始化展开状态
//        collapsedGroups.clear()
//        if(!inSearchMode()) {
//            getSavedCollapsedGroups()
//        }
//
//        notifyDataSetChanged()
//    }
//
//    fun inSearchMode(): Boolean {
//        return viewModel.searchMode.value == true
//    }
//
//    // 获取当前折叠的组
//    fun getCollapsedGroups(): List<String> {
//        return collapsedGroups.toList()
//    }
//
//    // 获取保存的折叠组
//    private fun getSavedCollapsedGroups() {
//        val configValue = viewModel.getConfigValue(
//            key = configKey,
//        )
//        if(configValue is JSONArray) {
//            // 如果是JSONArray类型，逐个提取字符串
//            for(i in 0 until configValue.length()) {
//                val item = configValue.opt(i)
//                if(item is String) {
//                    collapsedGroups.add(item)
//                }
//            }
//        }
//    }
//
//    fun setOnItemClickListener(listener: (Any) -> Unit) {
//        onItemClickListener = listener
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        val (item, _) = getItemWithGroupIndex(position)
//        return when(item) {
//            is ExpandableGroup -> {
//                when(item.titleString) {
//                    context.getString(R.string.no_result) -> {
//                        ViewTypes.ToolList.NO_RESULT
//                    }
//
//                    context.getString(R.string.search_history),
//                    context.getString(R.string.search_suggestion),
//                        -> {
//                        ViewTypes.ToolList.SEARCH_TITLE
//                    }
//
//                    else -> {
//                        ViewTypes.ToolList.GROUP
//                    }
//                }
//            }
//
//            is String -> {
//                ViewTypes.ToolList.HISTORY_OR_SUGGESTION
//            }
//
//            else -> {
//                getViewTypeForItem(item)
//            }
//        }
//    }
//
//    protected open fun getViewTypeForItem(item: Any): Int {
//        return ViewTypes.ToolList.SEPARATOR
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return when(viewType) {
//            ViewTypes.ToolList.GROUP -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_tool_title, parent, false)
//                GroupViewHolder(view)
//            }
//
//            ViewTypes.ToolList.NO_RESULT -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_setting_title, parent, false)
//                NoResultViewHolder(view)
//            }
//
//            ViewTypes.ToolList.HISTORY_OR_SUGGESTION -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_search_history_or_suggestion, parent, false)
//                HistoryOrSuggestionViewHolder(view)
//            }
//
//            ViewTypes.ToolList.SEARCH_TITLE -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_search_title, parent, false)
//                SearchTitleViewHolder(view)
//            }
//
//            else -> {
//                createCustomViewHolder(parent, viewType)
//            }
//        }
//    }
//
//    protected open fun createCustomViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_tool_separator, parent, false)
//        return SeparatorViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val (item, groupIndex) = getItemWithGroupIndex(position)
//
//        when(holder) {
//            is GroupViewHolder -> {
//                val group = item as ExpandableGroup
//                holder.bind(
//                    group,
//                    groupIndex,
//                ) {
//                    toggleGroup(groupIndex)
//                }
//            }
//
//            is NoResultViewHolder -> {
//                holder.bind()
//            }
//
//            is HistoryOrSuggestionViewHolder -> {
//                holder.bind(item as String)
//            }
//
//            is SearchTitleViewHolder -> {
//                holder.bind((item as ExpandableGroup).titleString)
//            }
//
//            else -> {
//                bindCustomViewHolder(holder, item, position)
//            }
//        }
//    }
//
//    protected open fun bindCustomViewHolder(holder: ViewHolder, item: Any, position: Int) {
//        // 子类可以重写此方法以绑定自定义视图
//    }
//
//    override fun getItemCount(): Int {
//        var count = 0
//        groups.forEachIndexed { index, group ->
//            // 只有当组内有元素或组名需要显示时才计算
//            if(shouldShowGroup(group)) {
//                // Always count the group header if it should be shown
//                count++
//
//                // Count children if group is expanded
//                if(!collapsedGroups.contains(group.titleString)) {
//                    count += group.dataList.size
//                }
//            }
//        }
//        return count
//    }
//
//    protected open fun shouldShowGroup(group: ExpandableGroup): Boolean {
//        // 对于没有子元素的组，不显示组标题，除非是特殊组
//        return group.dataList.isNotEmpty() ||
//                group.titleString == context.getString(R.string.no_result)
//    }
//
//    private fun getItemWithGroupIndex(position: Int): Pair<Any, Int> {
//        var currentPosition = 0
//
//        for((groupIndex, group) in groups.withIndex()) {
//            // 只处理应该显示的组
//            if(!shouldShowGroup(group)) {
//                continue
//            }
//
//            // Check if current position is the group header
//            if(currentPosition == position) {
//                return Pair(group, groupIndex)
//            }
//
//            currentPosition++
//
//            // If group is expanded, check children
//            if(!collapsedGroups.contains(group.titleString)) {
//                val childrenCount = group.dataList.size
//                if(position < currentPosition + childrenCount) {
//                    val childIndex = position - currentPosition
//                    return Pair(group.dataList[childIndex], groupIndex)
//                }
//                currentPosition += childrenCount
//            }
//        }
//
//        throw IndexOutOfBoundsException("位置 $position 越界")
//    }
//
//    protected open fun toggleGroup(groupIndex: Int) {
//        val startPosition = getGroupStartPosition(groupIndex)
//
//        if(!collapsedGroups.contains(groups[groupIndex].titleString)) {
//            collapsedGroups.add(groups[groupIndex].titleString)
//            notifyItemRangeInserted(
//                startPosition + 1,
//                groups[groupIndex].dataList.size,
//            )
//        } else {
//            collapsedGroups.remove(groups[groupIndex].titleString)
//            notifyItemRangeRemoved(
//                startPosition + 1,
//                groups[groupIndex].dataList.size,
//            )
//        }
//
//        // 更新组标题本身
//        notifyItemChanged(startPosition)
//        if(!inSearchMode()) {
//            saveCollapsedGroups()
//        }
//    }
//
//    // 保存折叠状态到配置中
//    private fun saveCollapsedGroups() {
//        viewModel.updateConfigValue(
//            key = configKey,
//            value = getCollapsedGroups(),
//        )
//        viewModel.saveUserConfig()
//    }
//
//    private fun getGroupStartPosition(groupIndex: Int): Int {
//        var position = 0
//        for(i in 0 until groupIndex) {
//            // 只计算应该显示的组
//            if(shouldShowGroup(groups[i])) {
//                position++ // Group header
//                if(!collapsedGroups.contains(groups[i].titleString)) {
//                    position += groups[i].dataList.size // Expanded children
//                }
//            }
//        }
//        return position
//    }
//
//    inner class GroupViewHolder(itemView: View) : ViewHolder(itemView) {
//        private val textViewName: TextView = itemView.findViewById(R.id.textView_name)
//        private val imageViewGroupIndicator: ImageView =
//            itemView.findViewById(R.id.group_indicator)
//
//        fun bind(
//            group: ExpandableGroup,
//            groupIndex: Int,
//            onGroupClick: () -> Unit,
//        ) {
//            // 如果组内没有元素，隐藏组标题
//            if(!shouldShowGroup(group)) {
//                itemView.visibility = GONE
//                return
//            } else {
//                itemView.visibility = VISIBLE
//            }
//
//            textViewName.text = group.titleString
//            imageViewGroupIndicator.setImageResource(R.drawable.ic_arrow_right)
//            // 根据展开状态设置图标旋转角度
//            val isExpanded = !collapsedGroups.contains(groups[groupIndex].titleString)
//            imageViewGroupIndicator.rotation = if(isExpanded) 90f else 0f
//
//            itemView.setOnClickListener {
//                VibrationHelper.vibrateOnClick(viewModel)
//                onGroupClick()
//            }
//        }
//    }
//
//    inner class SeparatorViewHolder(itemView: View) : ViewHolder(itemView)
//
//    inner class NoResultViewHolder(itemView: View) : ViewHolder(itemView) {
//        private val textViewNoResult: TextView = itemView.findViewById(R.id.textView_name)
//
//        fun bind() {
//            textViewNoResult.text = context.getString(R.string.no_result)
//            // 设置字体样式为正常
//            textViewNoResult.setTypeface(null, Typeface.NORMAL)
//            // 设置字体颜色为灰色
//            textViewNoResult.setTextColor(context.getColor(R.color.mid_gray))
//
//            itemView.setOnClickListener {
//                onItemClickListener?.invoke("")
//            }
//        }
//    }
//
//    inner class HistoryOrSuggestionViewHolder(itemView: View) : ViewHolder(itemView) {
//        private val textView: TextView = itemView.findViewById(R.id.textView_name)
//
//        fun bind(text: String) {
//            textView.text = text
//            itemView.setOnClickListener {
//                VibrationHelper.vibrateOnClick(viewModel)
//                onItemClickListener?.invoke(text)
//            }
//        }
//    }
//
//    inner class SearchTitleViewHolder(itemView: View) : ViewHolder(itemView) {
//        private val textView: TextView = itemView.findViewById(R.id.textView_name)
//        private val imageButtonClear: ImageButton = itemView.findViewById(R.id.imageButton_clear)
//
//        fun bind(text: String) {
//            textView.text = text
//            itemView.setOnClickListener {
//                onItemClickListener?.invoke("")
//            }
//            imageButtonClear.setOnClickListener {
//                VibrationHelper.vibrateOnClick(viewModel)
//                onItemClickListener?.invoke("clear:$text")
//            }
//        }
//    }
//}
