/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

// import com.heytap.wearable.support.recycler.widget.RecyclerView
// import com.heytap.wearable.support.recycler.widget.LinearLayoutManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.InstalledAppListAdapter
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.helper.EditTextHelper
import cn.minimote.toolbox.helper.SearchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class InstalledAppListFragment : Fragment() {

    private val viewModel: MyViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InstalledAppListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingTextView: TextView
    lateinit var editTextSearchBox: EditText
    lateinit var imageButtonClear: ImageButton
    lateinit var buttonCancel: Button

    private val alpha = UI.ALPHA_3
    val originalAlpha = UI.ALPHA_10

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // 观察者
    private lateinit var searchModeObserver: Observer<Boolean>


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

        // 在后台线程获取应用列表
        uiScope.launch {
            viewModel.getInstalledAppCoroutine()

            // 初始化 RecyclerView
            recyclerView = view.findViewById(R.id.recyclerView_activity_list)
            recyclerView.layoutManager = LinearLayoutManager(context)

            // 设置 Adapter
            adapter = InstalledAppListAdapter(
                context = requireContext(),
                fragment = this@InstalledAppListFragment,
                viewModel = viewModel,
            )
            recyclerView.adapter = adapter

            setupSearchBoxAndCancelButton(view)

            setupObservers()

//            val dividerItemDecoration = DividerItemDecoration(requireContext(), viewModel)
//            recyclerView.addItemDecoration(dividerItemDecoration)

            // 隐藏加载信息
            progressBar.visibility = View.GONE
            loadingTextView.visibility = View.GONE
        }


        return view
    }


    // 设置搜索框和取消按钮
    private fun setupSearchBoxAndCancelButton(view: View) {
        editTextSearchBox = view.findViewById(R.id.editText_searchBox)
        editTextSearchBox.visibility = View.VISIBLE

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
                val query = s.toString().trim()

                // 更新搜索词
                viewModel.searchQuery.value = query

                // 过滤数据
                if(viewModel.searchMode.value == true and query.isNotEmpty()) {
                    val filteredList = viewModel.installedAppList.value?.filter { activity ->
//                        if(viewModel.searchMode.value == true and query.isNotEmpty()) {
//                        val activityName = if(viewModel.isWatch) {
////                            activity.activityName.substringAfterLast('.')
////                        } else {
//                            activity.activityName
////                        }
                        SearchHelper.isAnyTextMatchQuery(
                            viewModel = viewModel,
                            textList = listOf(activity.name, activity.activityName),
                        )
//                        } else {
//                            true
//                        }
                    } ?: emptyList()

                    // 更新 Adapter 的数据
                    adapter.submitList(filteredList)
                } else {
                    adapter.submitList()
                }
                // 检查文本框内容是否为空
                if(query.isEmpty()) {
                    recyclerView.alpha = alpha
                } else {
                    // 恢复 RecyclerView 的不透明度
                    recyclerView.alpha = originalAlpha
                }
            },
            onFocusGained = {
                // 只有当当前不是搜索模式时才设置为true，避免重复触发
                if(viewModel.searchMode.value != true) {
                    viewModel.searchMode.value = true
                }
            },
            onFocusLost = {
//                viewModel.searchMode.value = false
            },
            imageButtonClear = imageButtonClear,
        )


        buttonCancel = view.findViewById(R.id.button_negative)
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
        recyclerView.alpha = alpha
    }


    // 退出搜索模式
    fun exitSearchMode() {

        viewModel.searchQuery.value = ""

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
        recyclerView.alpha = originalAlpha
    }


    // 设置观察者
    private fun setupObservers() {
        searchModeObserver = Observer { isSearchMode ->
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
        // 重建 _toolListSizeChangeMap 以确保数据一致性
        viewModel.builtChangeSizeMap()
        viewModel.updateToolListSizeChanged()
    }
}
