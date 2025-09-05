/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter


//class ToolGroupAdapter(
//    private var toolList: List<Tool>,
//    private val myActivity: MainActivity,
//    private val viewModel: MyViewModel,
//    val isSchemeList: Boolean = false,
//) : RecyclerView.Adapter<ToolGroupAdapter.ToolViewHolder>() {
//
////    init {
////        // 启用稳定ID
////        setHasStableIds(true)
////    }
//
//    inner class ToolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val icon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
//        val likeIcon: ImageView = itemView.findViewById(R.id.imageView_heart)
//        val name: TextView = itemView.findViewById(R.id.textView_app_name)
//        val description: TextView = itemView.findViewById(R.id.textView_activity_name)
//
//        // 添加一个标志，表示这个ViewHolder是否已经初始化过不会改变的内容
////        var isInitialized = false
//
//        init {
//            val layoutParams = itemView.layoutParams as? FlexboxLayoutManager.LayoutParams
//            layoutParams?.flexGrow = 1f
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
//        val view =
//            LayoutInflater.from(parent.context).inflate(R.layout.item_tool, parent, false)
//        return ToolViewHolder(view)
//    }
//
//
//    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
//        val tool = toolList[position]
//
//        // 只有当ViewHolder未初始化或者数据发生变化时才绑定所有数据
////        if(!holder.isInitialized) {
////            // 标记为已初始化
////            holder.isInitialized = true
//
//            holder.icon.setImageDrawable(viewModel.iconCacheHelper.getCircularDrawable(tool))
//
//            holder.name.text = tool.name
//            val descriptionText = if(isSchemeList) {
//                SchemeHelper.getSchemeFromId(tool.id)
//            } else {
//                tool.description?.trim()
//            }
//            if(!descriptionText.isNullOrBlank()) {
//                holder.description.text = descriptionText
//            } else {
//                holder.description.visibility = View.GONE
//            }
////        }
//
//        updateLikeIcon(tool = tool, likeIcon = holder.likeIcon)
//
//        holder.itemView.setOnClickListener {
//            VibrationHelper.vibrateOnClick(viewModel)
//            if(isSchemeList) {
//                Toast.makeText(
//                    myActivity,
//                    myActivity.getString(R.string.long_press_can_copy_scheme),
//                    Toast.LENGTH_SHORT,
//                ).show()
//            } else {
//                LaunchHelper.launch(myActivity, viewModel, tool)
//            }
//        }
//
//        // 取消长按震动
//        holder.itemView.isHapticFeedbackEnabled = false
//        holder.itemView.setOnLongClickListener { _ ->
//            VibrationHelper.vibrateOnLongPress(viewModel)
//            if(isSchemeList) {
//                ClipboardHelper.copyToClipboard(
//                    context = myActivity,
//                    text = SchemeHelper.getSchemeFromId(tool.id),
//                    toastString = tool.name + myActivity.getString(R.string.scheme),
//                )
//            } else {
//                BottomSheetDialogHelper.setAndShowBottomSheetDialog(
//                    viewModel = viewModel,
//                    activity = myActivity,
//                    tool = tool,
//                    menuList = if(viewModel.isWatch) MenuList.tool_watch else MenuList.tool,
//                    onMenuItemClick = {
//                        when(it) {
//                            MenuType.ADD_TO_HOME_OR_REMOVE_FROM_HOME -> {
//                                updateLikeIcon(tool = tool, likeIcon = holder.likeIcon)
//                            }
//                        }
//                    }
//                )
//            }
//            true
//        }
//
//    }
//
//
////    // 返回每个item的稳定ID
////    override fun getItemId(position: Int): Long {
////        // 使用Tool的id的hashCode作为稳定ID
////        return toolList[position].id.hashCode().toLong()
////    }
//
//
//    override fun getItemCount(): Int = toolList.size
//
//
//    private fun updateLikeIcon(tool: Tool, likeIcon: ImageView) {
//        if(shouldShowLikeIcon(tool)) {
//            likeIcon.visibility = View.VISIBLE
//        } else {
//            likeIcon.visibility = View.INVISIBLE
//        }
//    }
//
//
//    private fun shouldShowLikeIcon(tool: Tool): Boolean {
//        return !isSchemeList && viewModel.inSizeChangeMap(tool.id)
//                && viewModel.getConfigValue(Config.ConfigKeys.Display.SHOW_LIKE_ICON) == true
//    }
//
//}
