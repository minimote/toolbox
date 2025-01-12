/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.ActivityListAdapter
import cn.minimote.toolbox.view_model.ActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ActivityListFragment(
    private val viewModel: ActivityViewModel
) : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingTextView: TextView

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.i("AppListFragment", "onCreateView")
        val view = inflater.inflate(R.layout.fragment_activity_list, container, false)
//        this::class.simpleName?.let { viewModel.updateFragmentName(it) }
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_activity_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
//        // 检查权限并获取应用列表
//        checkPermissionAndGetAppList()
        progressBar = view.findViewById(R.id.progressBar)
        loadingTextView = view.findViewById(R.id.textView_loading)
        // 显示加载信息
        progressBar.visibility = View.VISIBLE
        loadingTextView.visibility = View.VISIBLE
        // 在后台线程获取应用列表
        uiScope.launch {
            viewModel.getInstalledActivitiesCoroutine()
            // 隐藏加载信息
            progressBar.visibility = View.GONE
            loadingTextView.visibility = View.GONE
            // 设置 Adapter
            adapter = ActivityListAdapter(
                context = requireContext(),
                viewModel = viewModel,
            )
            recyclerView.adapter = adapter
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
