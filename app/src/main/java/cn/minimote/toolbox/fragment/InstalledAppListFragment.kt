/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

// import com.heytap.wearable.support.recycler.widget.RecyclerView
// import com.heytap.wearable.support.recycler.widget.LinearLayoutManager
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.InstalledAppListAdapter
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.helper.BackgroundHelper.setBackgroundImageAndTextSearchMode
import cn.minimote.toolbox.helper.EditTextHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.SearchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.ShadowConstraintLayout
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.abs


class InstalledAppListFragment : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    val myActivity: MainActivity get() = requireActivity() as MainActivity

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InstalledAppListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingTextView: TextView
    lateinit var editTextSearchBox: EditText
    lateinit var imageButtonClear: ImageButton
    lateinit var buttonCancel: Button

    private lateinit var constraintLayoutBackground: ConstraintLayout
    private lateinit var imageViewBackground: ImageView
    private lateinit var textViewSearchMode: TextView

//    private val alpha = UI.ALPHA_3
//    val originalAlpha = UI.ALPHA_10

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // 观察者
    private lateinit var searchModeObserver: Observer<Boolean>
    var searchQuery = ""
    private var ignoreSearchModeChange = false
    private var recyclerViewScrollState: Parcelable? = null

    val searchHistoryConfigKey = Config.ConfigKeys.SearchHistory.INSTALLED_APP_LIST
    val searchSuggestionConfigKey = Config.ConfigKeys.SearchSuggestion.INSTALLED_APP_LIST

    private lateinit var searchHistoryExpandableGroup: ExpandableGroup

    private lateinit var searchSuggestionJSONObject: JSONObject
    private lateinit var searchSuggestionExpandableGroup: ExpandableGroup

    private lateinit var searchHistoryAndSuggestionList: List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.toolListSizeChanged.value = false
//        Log.e("ActivityListFragment", "onCreate")
//        viewModel = ViewModelProvider(this)[ToolboxViewModel::class.java]
//        Log.e("", "${getHash(viewModel)},${getHash(vi)}")
    }
//
//    private fun getHash(viewModel: ToolboxViewModel): Int {
//        return System.identityHashCode(viewModel)
//    }


    override fun onDestroy() {
        super.onDestroy()
//        viewModel.widgetListSizeWasModified.value = false
        job.cancel()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
//        Log.i("AppListFragment", "onCreateView")
//        val view = inflater.inflate(R.layout.fragment_activity_list, container, false)
        val view = inflater.inflate(R.layout.fragment_installed_app_list, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }


//        // 检查权限并获取应用列表
//        checkPermissionAndGetAppList()

        // 显示加载信息
        progressBar = view.findViewById(R.id.progressBar)
        loadingTextView = view.findViewById(R.id.textView_loading)
        progressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE

        val shadowConstraintLayout =
            view.findViewById<ShadowConstraintLayout>(R.id.include_shadow_constraintLayout_recyclerView)

        // 初始化 RecyclerView
        recyclerView = shadowConstraintLayout.recyclerView
        // 先隐藏 RecyclerView
        recyclerView.visibility = View.INVISIBLE

        recyclerView.layoutManager = LinearLayoutManager(context)

        // 在后台线程获取应用列表
        uiScope.launch {
            viewModel.getInstalledAppCoroutine()
//            viewModel.getInstalledApp()

            // 设置 Adapter
            adapter = InstalledAppListAdapter(
                context = requireContext(),
                fragment = this@InstalledAppListFragment,
                viewModel = viewModel,
            )
            recyclerView.adapter = adapter

            recyclerView.addOnItemTouchListener(object : SimpleOnItemTouchListener() {
                private var startX = 0f
                private var startY = 0f
                private val touchSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop

                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    when(e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = e.x
                            startY = e.y
                            // 不拦截 ACTION_DOWN 事件
                            return false
                        }

                        MotionEvent.ACTION_UP -> {
                            val endX = e.x
                            val endY = e.y
                            val deltaX = abs(endX - startX)
                            val deltaY = abs(endY - startY)

                            // 只有当是点击事件（移动距离小于 touchSlop）并且点击在空白区域时才处理
                            if(deltaX < touchSlop && deltaY < touchSlop) {
                                val childView = rv.findChildViewUnder(e.x, e.y)
                                if(childView == null) {
                                    // 在 post 中执行回调，避免阻塞触摸事件处理
                                    rv.post {
                                        hideKeyboardAndClearFocus()
                                    }
                                    return true
                                }
                            }
                            return false
                        }

                        else -> {
                            // 不拦截其他事件，保证滚动和越界回弹正常工作
                            return false
                        }
                    }
                }
            })

            // 添加滚动监听器来自动隐藏键盘
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    // 当列表开始滚动时，隐藏键盘并清除焦点
                    if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        hideKeyboardAndClearFocus()
                    }
                }
            })

            shadowConstraintLayout.setShadow(viewModel = viewModel)

            // 获取搜索历史和建议
            getSearchHistoryAndSuggestion()

            setupSearchBoxAndCancelButton(view)

            setupObservers()

//            val dividerItemDecoration = DividerItemDecoration(requireContext(), viewModel)
//            recyclerView.addItemDecoration(dividerItemDecoration)


            // 设置背景
            setBackground(view)


            // 隐藏加载信息时添加淡出动画
            progressBar.animate()
                .alpha(0f)
                .setDuration(UI.ANIMATION_DURATION)
                .setInterpolator(UI.Interpolator.LINEAR)
                .withEndAction {
                    progressBar.visibility = View.INVISIBLE
                    progressBar.alpha = 1f // 重置透明度以便下次使用
                }
                .start()

            loadingTextView.animate()
                .alpha(0f)
                .setDuration(UI.ANIMATION_DURATION)
                .setInterpolator(UI.Interpolator.LINEAR)
                .withEndAction {
                    loadingTextView.visibility = View.INVISIBLE
                    loadingTextView.alpha = 1f // 重置透明度以便下次使用
                }
                .start()

            // 显示 RecyclerView
            recyclerView.animate()
               .alpha(1f)
               .setDuration(UI.ANIMATION_DURATION)
                .setInterpolator(UI.Interpolator.LINEAR)
               .withEndAction {
                    recyclerView.visibility = View.VISIBLE
                }
               .start()
        }


        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        removeObservers()
        // 重建 _toolListSizeChangeMap 以确保数据一致性
        viewModel.builtChangeSizeMap()
        viewModel.updateToolListSizeChanged()
    }


    private fun setBackground(view: View) {

        constraintLayoutBackground = view.findViewById(R.id.constraintLayout_background)
        imageViewBackground = view.findViewById(R.id.imageView_background)
        textViewSearchMode = view.findViewById(R.id.textView_searchMode)

        setBackgroundImageAndTextSearchMode(
            imageViewBackground = imageViewBackground,
            textViewBackground = textViewSearchMode,
            viewModel = viewModel,
        )

        hideBackground()
    }


    private  fun showBackground() {
        constraintLayoutBackground.visibility = View.VISIBLE
    }


    private fun hideBackground() {
        constraintLayoutBackground.visibility = View.INVISIBLE
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

        updateSearchHistoryAndSuggestionList()
    }


    private fun updateSearchHistoryAndSuggestionList() {
        searchHistoryAndSuggestionList =
            getListFromExpandableGroup(searchHistoryExpandableGroup) +
                    getListFromExpandableGroup(searchSuggestionExpandableGroup)
    }


    @Suppress("UNCHECKED_CAST")
    private fun getListFromExpandableGroup(expandableGroup: ExpandableGroup): List<String> {
        if(expandableGroup.dataList.isNotEmpty()) {
            val count = expandableGroup.maxDisplayedCount
                ?: expandableGroup.dataList.size
            if(count > 0) {
                return listOf(expandableGroup.titleString) + expandableGroup.dataList.take(count) as List<String>
            }
        }
        return emptyList()
    }


    // 隐藏键盘并清除焦点
    fun hideKeyboardAndClearFocus() {
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
    private fun setupSearchBoxAndCancelButton(view: View) {
        updateSearchModeWithoutNotify(false)
        searchQuery = ""

        editTextSearchBox = view.findViewById(R.id.editText_searchBox)

        // 显示 editTextSearchBox
        editTextSearchBox.animate()
            .alpha(1f)
            .setDuration(UI.ANIMATION_DURATION)
            .setInterpolator(UI.Interpolator.LINEAR)
            .withEndAction {
                editTextSearchBox.visibility = View.VISIBLE
            }
            .start()

        imageButtonClear = view.findViewById(R.id.imageButton_clear)
        imageButtonClear.visibility = View.GONE

        EditTextHelper.setEditTextAndClearButton(
            editText = editTextSearchBox,
            stringHint = getString(
                R.string.hint_search_activities,
                viewModel.installedAppListSize,
            ),
            viewModel = viewModel,
            afterTextChanged = { s ->
                // 获取输入的文本
                searchQuery = s.toString().trim()

                // 检查文本框内容是否为空
                if(searchQuery.isEmpty()) {
//                    recyclerView.alpha = alpha
                    showSearchHistory(saveState = false)
                } else {
                    // 恢复 RecyclerView 的不透明度
//                    recyclerView.alpha = originalAlpha
                    showSearchResult()
                }
            },
            onFocusGained = {
                VibrationHelper.vibrateOnClick(viewModel)
                if(viewModel.searchModeInstalledAppList.value != true) {
//                        updateSearchModeWithoutNotify(true)
//                    } else {
                    enterSearchMode()
                }
            },
            onFocusLost = {
//                viewModel.searchMode.value = false
            },
            onClick = {
//                LogHelper.e("点击搜索框", "点击搜索框")
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
            }
        )


        buttonCancel = view.findViewById(R.id.button_negative)
        buttonCancel.visibility = View.GONE
        // 设置取消按钮点击事件
        buttonCancel.setOnClickListener {
//            LogHelper.e("取消按钮点击", "取消按钮点击")
            VibrationHelper.vibrateOnClick(viewModel)
//            isSearchMode = false
            exitSearchMode()
        }
    }


    // 显示搜索历史
    private fun showSearchHistory(
        saveState: Boolean,
    ) {
//        expandableRecyclerView.visibility = View.GONE
        if(saveState) {
            saveScrollState()
        }

        // 使用 FlexboxLayoutManager 实现自适应列数
        val flexboxLayoutManager = FlexboxLayoutManager(context)
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexboxLayoutManager.alignItems = AlignItems.CENTER
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP  // 确保可以换行

        recyclerView.layoutManager = flexboxLayoutManager

        updateSearchHistoryAndSuggestionList()
        refreshToolList(searchHistoryAndSuggestionList)
        showBackground()
//        Toast.makeText(context, "显示搜索历史", Toast.LENGTH_SHORT).show()
    }
    // 显示搜索结果
    private fun showSearchResult() {
//        expandableRecyclerView.visibility = View.VISIBLE
        recyclerView.layoutManager = LinearLayoutManager(context)
        refreshToolList()
        showBackground()
//        Toast.makeText(context, "显示搜索结果", Toast.LENGTH_SHORT).show()
    }
    // 显示原始列表
    private fun showOriginalList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        refreshToolList()
        restoreScrollState()
        hideBackground()
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

        updateSearchHistoryAndSuggestionList()
        refreshToolList(searchHistoryAndSuggestionList)
    }


    fun getDataList(): List<Any> {
        val filteredList = if(searchQuery.isEmpty()) {
            // 搜索词为空时，直接返回 ViewModel 中的完整列表
            viewModel.installedAppList.value ?: emptyList()
        } else {
            // 搜索词非空时，返回筛选后的列表
            viewModel.installedAppList.value?.filter { activity ->
                SearchHelper.isAnyTextMatchQuery(
                    textList = listOf(activity.name, activity.activityName),
                    query = searchQuery,
                )
            } ?: emptyList()
        }
        return filteredList.ifEmpty {
            getNoResultList()
        }
    }


    fun refreshToolList(dataList: List<Any> = getDataList()) {
        adapter.submitList(dataList)
    }


    // 设置搜索框文本
    fun setSearchBoxText(text: String) {
        editTextSearchBox.setText(text)
        editTextSearchBox.setSelection(text.length)
        editTextSearchBox.clearFocus()
    }


    // 保存滚动状态
    private fun saveScrollState() {
        recyclerViewScrollState =
            recyclerView.layoutManager?.onSaveInstanceState()
    }
    // 恢复滚动状态
    private fun restoreScrollState() {
        recyclerView.layoutManager?.onRestoreInstanceState(
            recyclerViewScrollState
        )
    }


    // 进入搜索模式
    private fun enterSearchMode() {

        searchQuery = ""
        updateSearchModeWithoutNotify(true)

        // 显示取消按钮
        buttonCancel.visibility = View.VISIBLE

        // 清空之前的搜索文本
        editTextSearchBox.setText("")
        // 请求焦点并弹出软键盘
        editTextSearchBox.requestFocus()
//        val imm =
//            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(editTextSearchBox, InputMethodManager.SHOW_IMPLICIT)


        // 设置 RecyclerView 半透明
//        recyclerView.alpha = alpha
//        recyclerView.visibility = View.INVISIBLE
        showSearchHistory(saveState = true)
    }

    // 退出搜索模式
    fun exitSearchMode() {

        searchQuery = ""
        updateSearchModeWithoutNotify(false)

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
//        recyclerView.alpha = originalAlpha
//        recyclerView.visibility = View.VISIBLE
        showOriginalList()
    }


    private fun getNoResultList(): List<Any> {
        return listOf(
            getString(R.string.no_result),
        )
    }

    // 更新搜索模式但不触发观察者
    fun updateSearchModeWithoutNotify(value: Boolean) {
        ignoreSearchModeChange = true
        viewModel.searchModeInstalledAppList.value = value
    }


    // 设置观察者
    private fun setupObservers() {
        searchModeObserver = Observer { isSearchMode ->
            FragmentHelper.updateEnableBackPressedCallback(viewModel)

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
        viewModel.searchModeInstalledAppList.observe(viewLifecycleOwner, searchModeObserver)
    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.searchModeInstalledAppList.removeObserver(searchModeObserver)
    }

}
