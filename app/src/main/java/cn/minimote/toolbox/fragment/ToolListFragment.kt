/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.DeviceType
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.Icon
import cn.minimote.toolbox.constant.ToolID
import cn.minimote.toolbox.constant.ToolList
import cn.minimote.toolbox.constant.Tools
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.EditTextHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.SearchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.ExpandableRecyclerView
import cn.minimote.toolbox.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ToolListFragment(
    val isSchemeList: Boolean = false,
) : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    private val myActivity get() = requireActivity() as MainActivity

    val viewPager: ViewPager2
        get() = myActivity.viewPager
    val constraintLayoutOrigin: ConstraintLayout
        get() = myActivity.constraintLayoutOrigin

    private lateinit var dataList: List<ExpandableGroup>

    private lateinit var expandableRecyclerView: ExpandableRecyclerView

    private lateinit var configList: List<Boolean>

    // 新增的搜索相关视图组件
    private lateinit var constraintLayoutAddApp: ConstraintLayout
    private lateinit var constraintLayoutSearchBox: ConstraintLayout
    private lateinit var editTextSearchBox: EditText
    private lateinit var imageButtonClear: ImageButton
    private lateinit var buttonCancel: Button
    private lateinit var imageViewSearchIcon: ImageView
    private lateinit var textViewName: TextView

    private val alpha = UI.ALPHA_3
    private val originalAlpha = UI.ALPHA_10

    private lateinit var searchModeObserver: Observer<Boolean>

    private var dataListSize = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentView = inflater.inflate(
            if(isSchemeList) {
                R.layout.fragment_scheme_list
            } else {
                R.layout.fragment_tool_list
            },
            container, false
        )

        expandableRecyclerView = fragmentView.findViewById(R.id.expandableRecyclerView)

        expandableRecyclerView.setParameters(
            viewModel = viewModel,
            myActivity = myActivity,
            isSchemeList = isSchemeList,
        )

        // 设置数据
        expandableRecyclerView.setGroups(getGroupList())

        // 初始化 configList
        configList = getConfigList()

        constraintLayoutSearchBox = fragmentView.findViewById(R.id.constraintLayout_searchBox)
        editTextSearchBox = fragmentView.findViewById(R.id.editText_searchBox)
        imageButtonClear = fragmentView.findViewById(R.id.imageButton_clear)
        buttonCancel = fragmentView.findViewById(R.id.button_negative)


        if(isSchemeList) {
//
        } else {
            imageViewSearchIcon = fragmentView.findViewById(R.id.imageView_searchIcon)
            textViewName = fragmentView.findViewById(R.id.textView_name)
            if(viewModel.isWatch) {
                textViewName.text = getString(R.string.add_local_app_watch)
            }

            constraintLayoutAddApp = fragmentView.findViewById(R.id.constraintLayout_add_app)

            constraintLayoutAddApp.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                FragmentHelper.switchFragment(
                    fragmentName = FragmentName.INSTALLED_APP_LIST_FRAGMENT,
                    fragmentManager = myActivity.supportFragmentManager,
                    viewModel = viewModel,
                    viewPager = myActivity.viewPager,
                    constraintLayoutOrigin = myActivity.constraintLayoutOrigin,
                )
                exitSearchMode()
            }
        }


        // 设置搜索框和取消按钮
        setSearchBoxAndCancelButton()


        // 设置观察者
        setupObservers()


        // 预加载应用图标
        preloadAppIcons()

        return fragmentView
    }


    // 设置搜索框和取消按钮
    private fun setSearchBoxAndCancelButton() {

        viewModel.searchMode.value = false
        viewModel.searchQuery.value = ""


        if(isSchemeList) {
            editTextSearchBox.visibility = View.VISIBLE
            imageButtonClear.visibility = View.VISIBLE

            val layoutParamsSearchBox = constraintLayoutSearchBox.layoutParams
            layoutParamsSearchBox.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            constraintLayoutSearchBox.layoutParams = layoutParamsSearchBox
//            constraintLayoutSearchBox.background = R.color.transparent.toDrawable()

        } else {
            editTextSearchBox.visibility = View.GONE
            imageButtonClear.visibility = View.GONE

            // 设置搜索图标点击事件
            constraintLayoutSearchBox.setOnClickListener {
                if(viewModel.searchMode.value != true) {
//                     VibrationHelper.vibrateOnClick(viewModel)
                    viewModel.searchMode.value = true
                }
            }
        }

        EditTextHelper.setEditTextAndClearButton(
            editText = editTextSearchBox,
            viewModel = viewModel,
            stringHint = getString(
                R.string.hint_search_activities,
                dataListSize,
            ),
            afterTextChanged = { s ->
                // 获取输入的文本
                val query = s.toString().trim()

                // 更新搜索词
                viewModel.searchQuery.value = query

                // 检查文本框内容是否为空
                if(query.isEmpty()) {
                    expandableRecyclerView.alpha = alpha
                } else {
                    // 恢复 RecyclerView 的不透明度
                    expandableRecyclerView.alpha = originalAlpha
                }
                refreshToolList()
            },
            onFocusGained = {
                if(isSchemeList) {
                    viewModel.searchMode.value = true
//                    VibrationHelper.vibrateOnClick(viewModel)
                }
            },
            onFocusLost = {
//                // 只有当当前是搜索模式且不是isSchemeList时才设置为false
//                if (viewModel.searchMode.value == true && !isSchemeList) {
//                    Toast.makeText(context, "失去焦点", Toast.LENGTH_SHORT).show()
//                    viewModel.searchMode.value = false
//                }
            },
            imageButtonClear = imageButtonClear,
        )

        buttonCancel.visibility = View.GONE
        // 设置取消按钮点击事件
        buttonCancel.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            viewModel.searchMode.value = false
        }

    }


    // 进入搜索模式
    private fun enterSearchMode() {

        viewModel.searchQuery.value = ""

        if(isSchemeList) {
//
        } else {
            // 隐藏左侧文本
            textViewName.visibility = View.GONE

            // 调整左侧布局宽度
            val layoutParams = constraintLayoutAddApp.layoutParams
            layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            constraintLayoutAddApp.layoutParams = layoutParams

            // 调整右侧布局宽度
            val layoutParamsSearchBox = constraintLayoutSearchBox.layoutParams
            layoutParamsSearchBox.width = 0
            constraintLayoutSearchBox.layoutParams = layoutParamsSearchBox
            constraintLayoutSearchBox.background = R.color.transparent.toDrawable()

            // 显示搜索相关控件
            editTextSearchBox.visibility = View.VISIBLE
            imageViewSearchIcon.visibility = View.GONE

            VibrationHelper.vibrateOnClick(viewModel)
        }

        // 显示取消按钮
        buttonCancel.visibility = View.VISIBLE

        editTextSearchBox.hint = getString(
            R.string.hint_search_activities,
            dataListSize,
        )
        // 清空之前的搜索文本
        editTextSearchBox.setText("")
        // 请求焦点并弹出软键盘
        editTextSearchBox.requestFocus()


//        val imm =
//            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(editTextSearchBox, InputMethodManager.SHOW_IMPLICIT)


        // 设置 RecyclerView 半透明
        expandableRecyclerView.alpha = alpha
    }


    // 退出搜索模式
    private fun exitSearchMode() {

        viewModel.searchQuery.value = ""

        if(isSchemeList) {
//
        } else {
            // 显示左侧文本
            textViewName.visibility = View.VISIBLE

            // 恢复左侧布局宽度
            val layoutParams = constraintLayoutAddApp.layoutParams
            layoutParams.width = 0
            constraintLayoutAddApp.layoutParams = layoutParams

            // 恢复右侧布局宽度
            val layoutParamsSearchBox = constraintLayoutSearchBox.layoutParams
            layoutParamsSearchBox.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
            constraintLayoutSearchBox.layoutParams = layoutParamsSearchBox
            constraintLayoutSearchBox.background =
                AppCompatResources.getDrawable(
                    myActivity,
                    R.drawable.background_widget_full
                )


            // 隐藏搜索相关控件
            editTextSearchBox.visibility = View.GONE
            imageViewSearchIcon.visibility = View.VISIBLE
            imageButtonClear.visibility = View.GONE
        }

        // 隐藏取消按钮
        buttonCancel.visibility = View.GONE

        // 清空搜索文本
        editTextSearchBox.setText("")
        editTextSearchBox.clearFocus()

//        // 隐藏软键盘
//        val imm =
//            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(editTextSearchBox.windowToken, 0)

        // 恢复 RecyclerView 不透明
        expandableRecyclerView.alpha = originalAlpha
    }


    // 获取组列表
    fun getGroupList(): List<ExpandableGroup> {
        // dataList 未初始化
        if(!::dataList.isInitialized) {
            dataList = ToolList.getToolListByDeviceType(
                viewModel = viewModel,
                deviceType = if(viewModel.isWatch) {
                    DeviceType.WATCH
                } else {
                    DeviceType.PHONE
                },
            )
        }

        dataListSize = 0

        // 获取实时配置
        val showUnavailableTools =
            viewModel.getConfigValue(
                Config.ConfigKeys.Display.SHOW_UNAVAILABLE_TOOLS
            ) as Boolean

        val nowList = dataList.map { group ->
//            val query = viewModel.searchQuery.value!!
            val filteredDataList = group.dataList.filter { tool ->
                val descriptionText = if(isSchemeList) {
                    SchemeHelper.getSchemeFromId(tool.id)
                } else {
                    tool.description.orEmpty().trim()
                }
                // 搜索过滤
                val matchesSearchQuery = SearchHelper.isAnyTextMatchQuery(
                    viewModel = viewModel,
                    textList = listOf(
                        tool.name, descriptionText
                    ),
                )

                // 可用性过滤
                val isAvailable = LaunchHelper.isToolAvailable(viewModel.myContext, tool)
                if(!isAvailable) {
                    tool.iconKey = Icon.IconKey.UNAVAILABLE
                }
                val showTool = showUnavailableTools || isAvailable || tool.id == ToolID.BLANK

                matchesSearchQuery && showTool
            }

            dataListSize += filteredDataList.size

            // 在不同包名的应用工具之间插入空白工具（仅在appTool组中）
            val dataListWithSeparators =
                if(group.titleString == getString(ToolList.GroupNameId.appTool)) {
                    var lastPackageName: String? = null
                    val toolListWithSeparators = mutableListOf<Tool>()

                    filteredDataList.filter { it.id != ToolID.BLANK }.forEach { tool ->
                        if(lastPackageName != null && lastPackageName != tool.packageName) {
                            toolListWithSeparators.add(Tools.blank)
                        }
                        toolListWithSeparators.add(tool)
                        lastPackageName = tool.packageName
                    }
                    toolListWithSeparators
                } else {
                    filteredDataList
                }

            ExpandableGroup(group.titleString, dataListWithSeparators)
        }.filter {
            // 移除空的组
            it.dataList.isNotEmpty()
        }

        return nowList.ifEmpty {
            getNoResult()
        }
    }


    // 获取无结果提示
    private fun getNoResult(): List<ExpandableGroup> {
        return listOf(
            ExpandableGroup(
                getString(R.string.no_result),
                listOf()
            )
        )
    }


    // 刷新工具列表
    fun refreshToolList() {
        expandableRecyclerView.setGroups(getGroupList())
    }


    override fun onResume() {
        super.onResume()
        val newConfigList = getConfigList()
        if(newConfigList != configList) {
            configList = newConfigList
            refreshToolList()
        }
        preloadAppIcons()
    }

    override fun onPause() {
        super.onPause()
        viewModel.searchMode.value = false
    }

    // 设置观察者
    private fun setupObservers() {
        searchModeObserver = Observer { isSearchMode ->
//            Toast.makeText(myActivity, "$isSearchMode", Toast.LENGTH_SHORT).show()
            if(isSearchMode) {
                enterSearchMode()
            } else {
                exitSearchMode()
            }
        }
        viewModel.searchMode.observe(viewLifecycleOwner, searchModeObserver)
    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.searchMode.removeObserver(searchModeObserver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }


    // 预加载应用图标
    private fun preloadAppIcons() {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val groupList = getGroupList()
            // 遍历所有工具并预加载图标
            groupList.forEach { group ->
                group.dataList.forEach { tool ->
                    // 跳过空白工具
                    if(tool.id != ToolID.BLANK) {
                        // 预加载图标到缓存中
                        viewModel.iconCacheHelper.getDrawable(tool)
                    }
                }
            }
        }
    }


    // 获取配置列表
    private fun getConfigList(): List<Boolean> {
        val keyList = listOf(
            Config.ConfigKeys.Display.SHOW_UNAVAILABLE_TOOLS,
            Config.ConfigKeys.Display.SHOW_LIKE_ICON,
        )
        return keyList.map { viewModel.getConfigValue(it) as Boolean }
    }

}