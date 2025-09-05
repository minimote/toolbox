/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import androidx.recyclerview.widget.RecyclerView
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
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.EditTextHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.LogHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.SearchHelper
import cn.minimote.toolbox.helper.SearchHelper.updateSearchHistoryAndSuggestionMaxDisplayedCount
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.ExpandableRecyclerView
import cn.minimote.toolbox.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


@AndroidEntryPoint
class SchemeListFragment(
    val isSchemeList: Boolean = true,
) : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    private val myActivity get() = requireActivity() as MainActivity

    val viewPager: ViewPager2 get() = myActivity.viewPager

    private lateinit var dataList: List<ExpandableGroup>

    private lateinit var expandableRecyclerView: ExpandableRecyclerView

    private var expandableRecyclerViewScrollState: Parcelable? = null

    private lateinit var configList: List<Boolean>

    // 新增的搜索相关视图组件
    private lateinit var constraintLayoutAddApp: ConstraintLayout
    private lateinit var constraintLayoutSearchBox: ConstraintLayout
    private lateinit var editTextSearchBox: EditText
    private lateinit var imageButtonClear: ImageButton
    private lateinit var buttonCancel: Button
    private lateinit var imageViewSearchIcon: ImageView
    private lateinit var textViewName: TextView

    private lateinit var searchModeObserver: Observer<Boolean>
    private var ignoreSearchModeChange = false
    var searchQuery = ""

    val searchHistoryConfigKey = if(isSchemeList) {
        Config.ConfigKeys.SearchHistory.SCHEME_LIST
    } else {
        Config.ConfigKeys.SearchHistory.TOOL_LIST
    }
    val searchSuggestionConfigKey = if(isSchemeList) {
        Config.ConfigKeys.SearchSuggestion.SCHEME_LIST
    } else {
        Config.ConfigKeys.SearchSuggestion.TOOL_LIST
    }

    private lateinit var searchHistoryExpandableGroup: ExpandableGroup

    private lateinit var searchSuggestionJSONObject: JSONObject
    private lateinit var searchSuggestionExpandableGroup: ExpandableGroup

    private lateinit var searchHistoryAndSuggestionList: List<ExpandableGroup>


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
//        refreshLayout = fragmentView.findViewById(R.id.refreshLayout)
//        refreshLayout.setOnClickListener {
//            Toast.makeText(myActivity, "点击", Toast.LENGTH_SHORT).show()
//        }

        expandableRecyclerView.setParameters(
            viewModel = viewModel,
            myActivity = myActivity,
            isSchemeList = isSchemeList,
            fragment = this,
            emptyAreaClickListener = {
                hideKeyboardAndClearFocus()
            },
            getQuery = {
                searchQuery
            },
            updateSearchHistoryAndSuggestion = {
                updateSearchHistoryAndSuggestion(it)
            },
            clearSearchHistoryOrSuggestion = {
                clearSearchHistoryOrSuggestion(it)
            },
            setSearchBoxText = {
                setSearchBoxText(it)
            },
        )

        // 设置数据
        expandableRecyclerView.setGroups(getGroupList())

        // 添加滚动监听器来自动隐藏键盘
        expandableRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // 当列表开始滚动时，隐藏键盘并清除焦点
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboardAndClearFocus()
                }
            }
        })


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
                    viewModel = viewModel,
                    activity = myActivity,
                )
                exitSearchMode()
            }
        }


        // 获取搜索历史和建议
        getSearchHistoryAndSuggestion()


        // 设置搜索框和取消按钮
        setSearchBoxAndCancelButton()


        // 设置观察者
        setupObservers()


        // 预加载应用图标
        preloadAppIcons()

        return fragmentView
    }


    // 获取搜索历史和建议
    private fun getSearchHistoryAndSuggestion() {
        searchHistoryExpandableGroup = SearchHelper.getSearchHistoryExpandableGroup(
            viewModel = viewModel,
            configKey = searchHistoryConfigKey,
        )

//        LogHelper.e("获取搜索历史", "搜索历史:${searchHistoryExpandableGroup.dataList}")
        searchSuggestionJSONObject = SearchHelper.getSearchSuggestionJSONObject(
            viewModel = viewModel,
            configKey = searchSuggestionConfigKey,
        )
        searchSuggestionExpandableGroup = SearchHelper.getSearchSuggestionExpandableGroup(
            viewModel = viewModel,
            searchSuggestionJSONObject = searchSuggestionJSONObject,
        )

        searchHistoryAndSuggestionList = listOf(
            searchHistoryExpandableGroup,
            searchSuggestionExpandableGroup,
        )
//        LogHelper.e("搜索历史", "${searchHistoryExpandableGroup.dataList}")
//        LogHelper.e("搜索建议", "${searchSuggestionExpandableGroup.dataList}")
    }


    // 隐藏键盘并清除焦点
    private fun hideKeyboardAndClearFocus() {
        // 检查搜索框是否有焦点
        if(editTextSearchBox.hasFocus()) {
            editTextSearchBox.clearFocus()

            // 隐藏软键盘
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editTextSearchBox.windowToken, 0)
        }
    }


    // 设置搜索框和取消按钮
    private fun setSearchBoxAndCancelButton() {

        updateSearchModeWithoutNotify(false)
        searchQuery = ""


        if(isSchemeList) {
            editTextSearchBox.visibility = View.VISIBLE

            val layoutParamsSearchBox = constraintLayoutSearchBox.layoutParams
            layoutParamsSearchBox.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            constraintLayoutSearchBox.layoutParams = layoutParamsSearchBox
//            constraintLayoutSearchBox.background = R.color.transparent.toDrawable()

        } else {
            editTextSearchBox.visibility = View.GONE


            // 设置搜索图标点击事件
            constraintLayoutSearchBox.setOnClickListener {
//                if(!isSearchMode) {
//                     VibrationHelper.vibrateOnClick(viewModel)
                enterSearchMode()
//                    isSearchMode = true
//                }
            }
        }


        EditTextHelper.setEditTextAndClearButton(
            editText = editTextSearchBox,
            viewModel = viewModel,
            stringHint = getString(
                R.string.hint_search_activities,
                dataList.sumOf { it.getSize() },
            ),
            afterTextChanged = { s ->
                // 获取输入的文本
                searchQuery = s.toString().trim()

                // 检查文本框内容是否为空
                if(searchQuery.isEmpty()) {
//                    expandableRecyclerView.alpha = alpha
                    showSearchHistoryAndSuggestion(saveState = false)
                } else {
                    // 恢复 RecyclerView 的不透明度
//                    expandableRecyclerView.alpha = originalAlpha
                    showSearchResult()
//                    expandableRecyclerView.updateSearchMode(true)
                }
//                refreshToolList()
            },
            onFocusGained = {
                LogHelper.e("获取焦点", "searchMode:${viewModel.searchMode.value}")
                VibrationHelper.vibrateOnClick(viewModel)
                if(isSchemeList) {
                    if(viewModel.searchMode.value != true) {
//                        updateSearchModeWithoutNotify(true)
//                    } else {
                        enterSearchMode()
                    }
                }
            },
            onFocusLost = {
//                // 只有当当前是搜索模式且不是isSchemeList时才设置为false
//                if (isSearchMode == true && !isSchemeList) {
//                    Toast.makeText(context, "失去焦点", Toast.LENGTH_SHORT).show()
//                    isSearchMode = false
//                }
            },
            onClick = {
                LogHelper.e("点击", "点击")
                VibrationHelper.vibrateOnClick(viewModel)
            },
            onEditorAction = {
                updateSearchHistoryAndSuggestion(
                    editTextSearchBox.text.toString().trim()
                )
            },
            imageButtonClear = imageButtonClear,
            onClickClearButton = {
                VibrationHelper.vibrateOnClick(viewModel)
//                if(editTextSearchBox.hasFocus()) {
//                } else {
//                    editTextSearchBox.requestFocus()
//                }
            }
        )


        imageButtonClear.visibility = View.GONE
        buttonCancel.visibility = View.GONE
        // 设置取消按钮点击事件
        buttonCancel.setOnClickListener {
            LogHelper.e("取消按钮点击", "取消按钮点击")
            VibrationHelper.vibrateOnClick(viewModel)
//            isSearchMode = false
            exitSearchMode()
        }

    }


    // 显示搜索历史和搜索建议
    private fun showSearchHistoryAndSuggestion(
        saveState: Boolean,
    ) {
//        expandableRecyclerView.visibility = View.GONE
        if(saveState) {
            saveScrollState()
        }
        updateSearchHistoryAndSuggestionMaxDisplayedCount(
            viewModel = viewModel,
            historyExpandableGroup = searchHistoryExpandableGroup,
            suggestionExpandableGroup = searchSuggestionExpandableGroup,
        )
        refreshToolList(searchHistoryAndSuggestionList)
//        Toast.makeText(context, "显示搜索历史", Toast.LENGTH_SHORT).show()
    }
    // 显示搜索结果
    private fun showSearchResult() {
//        expandableRecyclerView.visibility = View.VISIBLE
        refreshToolList()
//        Toast.makeText(context, "显示搜索结果", Toast.LENGTH_SHORT).show()
    }
    // 显示原始列表
    private fun showOriginalList() {
        refreshToolList()
        restoreScrollState()
//        Toast.makeText(context, "显示原始列表", Toast.LENGTH_SHORT).show()
    }


    // 更新搜索历史
    fun updateSearchHistoryAndSuggestion(newWord: String) {
        SearchHelper.updateSearchHistoryAndSuggestion(
            viewModel = viewModel,
            searchHistoryConfigKey = searchHistoryConfigKey,
            searchHistoryExpandableGroup = searchHistoryExpandableGroup,
            searchSuggestionConfigKey = searchSuggestionConfigKey,
            searchSuggestionJSONObject = searchSuggestionJSONObject,
            searchSuggestionExpandableGroup = searchSuggestionExpandableGroup,
            newWord = newWord,
        )
    }


    // 清空搜索历史或搜索建议
    fun clearSearchHistoryOrSuggestion(name: String) {
        SearchHelper.clearSearchHistoryOrSuggestion(
            viewModel = viewModel,
            searchHistoryConfigKey = searchHistoryConfigKey,
            searchHistoryExpandableGroup = searchHistoryExpandableGroup,
            searchSuggestionConfigKey = searchSuggestionConfigKey,
            searchSuggestionJSONObject = searchSuggestionJSONObject,
            searchSuggestionExpandableGroup = searchSuggestionExpandableGroup,
            name = name,
        )
        refreshToolList(searchHistoryAndSuggestionList)
    }


    // 保存滚动状态
    private fun saveScrollState() {
        expandableRecyclerViewScrollState =
            expandableRecyclerView.layoutManager?.onSaveInstanceState()
    }
    // 恢复滚动状态
    private fun restoreScrollState() {
        expandableRecyclerView.layoutManager?.onRestoreInstanceState(
            expandableRecyclerViewScrollState
        )
    }


    // 设置搜索框文本
    fun setSearchBoxText(text: String) {
        editTextSearchBox.setText(text)
        editTextSearchBox.setSelection(text.length)
        editTextSearchBox.clearFocus()
    }


    // 进入搜索模式
    private fun enterSearchMode() {

        searchQuery = ""
        updateSearchModeWithoutNotify(true)

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

//            LogHelper.e("进入搜索模式请求焦点", "isSchemeList:${false}")
//            VibrationHelper.vibrateOnClick(viewModel)
            // 请求焦点并弹出软键盘
            editTextSearchBox.requestFocus()
        }


        // 显示取消按钮
        buttonCancel.visibility = View.VISIBLE

        // 清空之前的搜索文本
        editTextSearchBox.setText("")


//        val imm =
//            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(editTextSearchBox, InputMethodManager.SHOW_IMPLICIT)


        // 设置 RecyclerView 半透明
//        expandableRecyclerView.alpha = alpha
        showSearchHistoryAndSuggestion(saveState = true)
    }


    // 退出搜索模式
    private fun exitSearchMode() {

        searchQuery = ""
        updateSearchModeWithoutNotify(false)

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
//        expandableRecyclerView.alpha = originalAlpha
//        expandableRecyclerView.updateSearchMode(false)
//        Toast.makeText(context, "退出搜索模式", Toast.LENGTH_SHORT).show()+
//        refreshToolList()
        showOriginalList()
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


        // 获取实时配置
        val showUnavailableTools =
            viewModel.getConfigValue(
                Config.ConfigKeys.Display.SHOW_UNAVAILABLE_TOOLS
            ) as Boolean

        val nowList = dataList.map { group ->
//            val query = searchQuery!!
            val filteredDataList = group.dataList.filter { data ->
                val tool = data as Tool
                val descriptionText = if(isSchemeList) {
                    SchemeHelper.getSchemeFromId(tool.id)
                } else {
                    tool.description.orEmpty().trim()
                }
                // 搜索过滤
                val matchesSearchQuery = SearchHelper.isAnyTextMatchQuery(
                    textList = listOf(
                        tool.name, descriptionText
                    ),
                    query = searchQuery,
                )

                // 可用性过滤
                val isAvailable = LaunchHelper.isToolAvailable(viewModel.myContext, tool)
                if(!isAvailable) {
                    tool.iconKey = Icon.IconKey.UNAVAILABLE
                }
                val showTool = showUnavailableTools || isAvailable || tool.id == ToolID.BLANK

                matchesSearchQuery && showTool
            }

            // 在不同包名的应用工具之间插入空白工具（仅在appTool组中）
            val dataListWithSeparators =
                if(group.titleString == getString(ToolList.GroupNameId.appTool)) {
                    var lastPackageName: String? = null
                    val toolListWithSeparators = mutableListOf<Tool>()

                    filteredDataList.filter { it is Tool && it.id != ToolID.BLANK }
                        .forEach { data ->
                            val tool = data as Tool
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
    fun refreshToolList(dataList: List<ExpandableGroup> = getGroupList()) {
        expandableRecyclerView.setGroups(dataList)
    }


    override fun onResume() {
        super.onResume()
//        LogHelper.e("onResume", "onResume")
        val newConfigList = getConfigList()
        if(newConfigList != configList) {
            configList = newConfigList
            refreshToolList()
        }
        preloadAppIcons()
    }

//    override fun onPause() {
//        super.onPause()
////        isSearchMode = false
////        exitSearchMode()
//    }

    // 设置观察者
    private fun setupObservers() {
        searchModeObserver = Observer { isSearchMode ->
//            Toast.makeText(myActivity, "$isSearchMode", Toast.LENGTH_SHORT).show()
            when(viewModel.getFragmentName()) {
//                FragmentName.WIDGET_LIST_FRAGMENT,
                FragmentName.SCHEME_LIST_FRAGMENT,
                    -> {
                    // 忽略搜索模式变化
                    if(ignoreSearchModeChange) {
                        ignoreSearchModeChange = false
                        return@Observer
                    }
                    if(isSearchMode) {
                        enterSearchMode()
                    } else {
                        exitSearchMode()
                    }
                }
            }
        }
        addObservers()
    }


    // 添加观察者
    private fun addObservers() {
//        if(!viewModel.searchMode.hasObservers()) {
            LogHelper.e("添加观察者", "添加观察者")
            viewModel.searchMode.observe(viewLifecycleOwner, searchModeObserver)
//        }
    }


    // 移除观察者
    private fun removeObservers() {
        LogHelper.e("移除观察者", "移除观察者")
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
                group.dataList.forEach { data ->
                    val tool = data as Tool
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


    // 更新搜索模式但不触发观察者
    fun updateSearchModeWithoutNotify(value: Boolean) {
        ignoreSearchModeChange = true
        viewModel.searchMode.value = value
    }

}