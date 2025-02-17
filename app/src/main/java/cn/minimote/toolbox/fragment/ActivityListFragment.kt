/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

// import com.heytap.wearable.support.recycler.widget.RecyclerView
// import com.heytap.wearable.support.recycler.widget.LinearLayoutManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.ActivityListAdapter
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.view_model.ToolboxViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ActivityListFragment : Fragment() {

    private val viewModel: ToolboxViewModel by activityViewModels()

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingTextView: TextView
    lateinit var searchBox: EditText
    lateinit var buttonCancel: Button

    private val alpha = 0.3f
    private val originalAlpha = 1.0f

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    // 观察者
    private lateinit var searchModeObserver: Observer<Boolean>


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
////        viewModel = ViewModelProvider(this)[ToolboxViewModel::class.java]
////        Log.e("", "${getHash(viewModel)},${getHash(vi)}")
//    }
//
//    private fun getHash(viewModel: ToolboxViewModel): Int {
//        return System.identityHashCode(viewModel)
//    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
//        Log.i("AppListFragment", "onCreateView")
//        val view = inflater.inflate(R.layout.fragment_activity_list, container, false)
        val view = inflater.inflate(R.layout.fragment_activity_list_new, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_activity_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

//        // 检查权限并获取应用列表
//        checkPermissionAndGetAppList()

        // 显示加载信息
        progressBar = view.findViewById(R.id.progressBar)
        loadingTextView = view.findViewById(R.id.textView_loading)
        progressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE

        // 在后台线程获取应用列表
        uiScope.launch {
            viewModel.getInstalledActivitiesCoroutine()

            // 设置 Adapter
            adapter = ActivityListAdapter(
                context = requireContext(),
                fragment = this@ActivityListFragment,
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
        searchBox = view.findViewById(R.id.editText_searchBox)
        searchBox.visibility = View.VISIBLE
        buttonCancel = view.findViewById(R.id.button_cancel)

        searchBox.hint = getString(
            R.string.hint_search_activities, viewModel.installedAppListSize,
        )

        // 手动请求输入法，避免第一次点击出现闪烁
        searchBox.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                VibrationHelper.vibrateOnClick(requireContext())
                val imm =
                    v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)

                // 进入搜索模式
                viewModel.searchMode.value = true
            } else {
                // 退出搜索模式
                viewModel.searchMode.value = false
            }
        }

        // 点击输入框时触发振动
        searchBox.setOnClickListener {
            VibrationHelper.vibrateOnClick(requireContext())
        }

        // 添加 TextWatcher 监听文本变化
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 恢复 RecyclerView 的不透明度
                recyclerView.alpha = originalAlpha

                // 获取输入的文本
                val query = s.toString().trim()

                // 过滤数据
                val filteredList = viewModel.installedActivityList.value?.filter { activity ->
                    if(viewModel.searchMode.value == true and query.isNotEmpty()) {
                        activity.appName.contains(
                            query, ignoreCase = true
                        ) && activity.packageName.isNotEmpty()
                    } else {
                        activity.appName.contains(
                            query, ignoreCase = true
                        )
                    }
                } ?: emptyList()

                // 更新 Adapter 的数据
                adapter.submitList(filteredList)

                // 检查文本框内容是否为空
                if(query.isEmpty()) {
                    recyclerView.alpha = alpha
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
            }
        })

        // 设置取消按钮点击事件
        buttonCancel.setOnClickListener {
            VibrationHelper.vibrateOnClick(requireContext())
            exitSearchMode()
        }
    }


    // 退出搜索模式
    private fun exitSearchMode() {
        searchBox.setText("")
        searchBox.clearFocus()
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchBox.windowToken, 0)
    }


    // 设置观察者
    private fun setupObservers() {
        searchModeObserver = Observer { isSearchMode ->
            if(isSearchMode) {
                // 显示取消按钮
                buttonCancel.visibility = View.VISIBLE

                // 设置 RecyclerView 半透明
                recyclerView.alpha = alpha
            } else {
                // 隐藏取消按钮
                buttonCancel.visibility = View.GONE
                exitSearchMode()

                // 恢复 RecyclerView 不透明
                recyclerView.alpha = originalAlpha

                adapter.submitList()
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
}

// 保存修改
//    fun saveChanges() {
//        if (originalMapActivityNameToStorageAppInfo != modifiedMapActivityNameToStorageAppInfo) {
//            Log.i("AppListFragment", "有修改，开始保存")
//            // 将 modifiedMapActivityNameToStorageAppInfo 的值生成一个 Set<StorageAppInfo>
//            val modifiedAppSet: Set<StorageAppInfo> =
//                modifiedMapActivityNameToStorageAppInfo.values.toSet()
//
//            // 使用 AppInfoStorage 类保存集合
//            AppInfoStorage.saveAppList(requireContext(), modifiedAppSet)
//            Toast.makeText(requireContext(), R.string.save_success, Toast.LENGTH_SHORT).show()
//            Log.i("AppListFragment", "保存成功")
//        } else {
//            Log.i("AppListFragment", "没有修改，无需保存")
//        }
//    }


//    // 检查权限并获取应用列表
//    private fun checkPermissionAndGetAppList() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            when {
//                ContextCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.QUERY_ALL_PACKAGES
//                ) == PackageManager.PERMISSION_GRANTED -> {
//                    // 权限已授予
//                    Log.i("AppListFragment", "已有读取应用列表的权限")
//                    // 获取已安装的应用列表
//                    getInstalledUserApps()
//                }
//
//                else -> {
//                    // 权限申请
//                    val requestPermissionLauncher = registerForActivityResult(
//                        ActivityResultContracts.RequestPermission()
//                    ) { isGranted: Boolean ->
//                        if (isGranted) {
//                            Log.i("AppListFragment", "申请权限成功")
//                            getInstalledUserApps()
//                        } else {
//                            Log.i("AppListFragment", "申请权限失败")
//                            // 权限被拒绝，显示空列表或其他提示
////            showEmptyListAndToast("权限被拒绝，无法查看应用列表")
//                        }
//                    }
//                    requestPermissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
//                }
//            }
//        } else {
//            // 对于 API < 30，直接获取应用列表
//            getInstalledUserApps()
//        }
//    }
//
//
//    // 获取已安装的应用列表并添加进列表中
//    private fun getInstalledUserApps() {
//        var installedApps: List<ApplicationInfo> = emptyList()
//        Log.i("AppListFragment", "获取应用列表")
//        try {
//            installedApps =
//                requireContext().packageManager.getInstalledApplications(0)
//        } catch (e: Exception) {
//            Log.e("AppListFragment", "获取应用信息失败: ${e.message}", e)
//        }
//        Log.i("AppListFragment", "获取到${installedApps.size}个应用")
//
//        val userApps = installedApps.filter { !isSystemPackage(it) }
//        Log.i("AppListFragment", "过滤后剩余${userApps.size}个应用")
//
//        for (app in userApps) {
//            try {
//                val label = app.loadLabel(requireContext().packageManager).toString() ?: "未知应用"
//                val packageName = app.packageName ?: "未知包名"
//                val icon = app.loadIcon(requireContext().packageManager)
//
//                if (label.isNotEmpty() && packageName.isNotEmpty()) {
//                    appList.add(AppInfo(label, packageName, icon))
//                } else {
//                    Log.w("AppListFragment", "跳过无效应用: 标签或包名为空")
//                }
//            } catch (e: Exception) {
//                Log.e("AppListFragment", "获取应用信息失败: ${e.message}")
//            }
//        }
//    }
//
//
//    private fun isSystemPackage(appInfo: ApplicationInfo): Boolean {
//        return appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
//    }
