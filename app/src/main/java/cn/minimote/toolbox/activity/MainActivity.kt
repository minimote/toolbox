/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.Guideline
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.File.FileOperationResult
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper.loadAllConfig
import cn.minimote.toolbox.helper.ConfigHelper.saveUserConfig
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.FileHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.LogHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.ui.widget.DraggableTextView
import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.File


class MainActivity : AppCompatActivity() {

    val viewModel: MyViewModel by viewModels()


    private lateinit var buttonExit: TextView
    private lateinit var buttonSave: TextView
    private lateinit var draggableTextView: DraggableTextView

    // 观察者
    private lateinit var widgetListOrderWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetListSizeWasModifiedObserver: Observer<Boolean>
    private lateinit var widgetWasModifiedObserver: Observer<Boolean>
    private lateinit var dynamicShortcutListWasChangedObserver: Observer<Boolean>

    private lateinit var multiSelectModeObserver: Observer<Boolean>
    private lateinit var sortModeObserver: Observer<Boolean>
    private lateinit var fragmentNameObserver: Observer<String>
    private lateinit var enableBackPressedCallbackObserver: Observer<Boolean>

    private lateinit var settingWasModifiedObserver: Observer<Boolean>

    private lateinit var selectedIdsObserver: Observer<MutableSet<String>>

    private lateinit var backPressedCallback: OnBackPressedCallback

    // Navigation Component
    lateinit var navController: NavController

    lateinit var viewPager: ViewPager2


    private lateinit var importFileLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var exportFileLauncher: ActivityResultLauncher<Intent>

    // 用于存储回调的变量
    private var importCallback: ((Int) -> Unit)? = null
    private var exportCallback: ((Int, Uri?) -> Unit)? = null
    private var importDestinationFile: File? = null
    private var exportSourceFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        // 安卓12及以下的启动页
        installSplashScreen()

        super.onCreate(savedInstanceState)
//        Log.d("MainActivity", "onCreate")

        // 默认暗色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.dark(Color.YELLOW),
//            navigationBarStyle = SystemBarStyle.dark(Color.YELLOW)
        )
//        // 状态栏和导航栏颜色
//        window.statusBarColor = Color.BLACK
//        window.navigationBarColor = Color.BLACK


        setContentView(R.layout.activity_main)
//// 获取屏幕最小宽度
//        val metrics = resources.displayMetrics
//        val smallestWidth = metrics.widthPixels.coerceAtMost(metrics.heightPixels) / metrics.density
//        LogHelper.e("最小宽度: ${smallestWidth}dp", "最小宽度: ${smallestWidth}dp")


        // 加载配置文件
        viewModel.loadAllConfig()
//        viewModel.checkHashCode()
        // 加载保存的数据
        viewModel.loadStorageActivities()


        // 设置文件操作
        setFileOperationLaunchers()

        // 启动日志捕获
        LogHelper.startDebugLogCapture(viewModel)

//        constraintLayoutOrigin = findViewById(containerId)

        // 适配系统返回手势和按钮
        setupBackPressedCallback()

        // 设置观察者
        setupObservers()

        // 设置按钮
        setupButtons()

        // 设置 Navigation Component
        setupNavigation()


        val uri: Uri? = intent.data
        if(uri != null) {
            SchemeHelper.handleScheme(
                uri = uri,
                myActivity = this,
                viewModel = viewModel,
            )
        } else {
            // 直接打开应用时才检查更新
            CheckUpdateHelper.autoCheckUpdate(
                context = this, viewModel = viewModel,
            )
        }

    }


    // 后台已经有该软件的实例也能处理 Scheme
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(intent.data != null) {
            SchemeHelper.handleScheme(
                uri = intent.data!!,
                myActivity = this,
                viewModel = viewModel,
            )
        }
    }


    override fun onPause() {
//        LogHelper.e("主活动", "onPause")
//        viewModel.updateLastSelectedPosition(viewPager.currentItem)
        setLiveDates()
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
        setLiveDates()
    }


    override fun onDestroy() {
        super.onDestroy()
        // 移除观察者
        removeObservers()
    }


    private fun setLiveDates() {
        viewModel.multiselectMode.value = false
        viewModel.sortMode.value = false
        viewModel.searchModeToolList.value = false
        viewModel.searchModeInstalledAppList.value = false
        viewModel.searchModeSchemeList.value = false
    }


    // 适配系统返回手势和按钮
    private fun setupBackPressedCallback() {
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                FragmentHelper.myHandleOnBackPressed(
                    activity = this@MainActivity,
                    viewModel = viewModel,
                )
//                if(!navController.navigateUp()) {
//                    finish()
//                }
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            backPressedCallback,
        )
    }


    // 默认返回
    fun defaultReturn() {
        onBackPressedDispatcher.onBackPressed()
    }


    // 设置按钮
    private fun setupButtons() {

        if(!viewModel.isWatch) {
            draggableTextView = findViewById(R.id.draggableTextView)
            draggableTextView.visibility = View.INVISIBLE
            draggableTextView.viewModel = viewModel

            // 将手机顶部的顶部按钮间距设为 0px
            val guideline = findViewById<Guideline>(R.id.guideline4)
            guideline.setGuidelineBegin(0)

            draggableTextView.setOnClickListener {
                buttonSave.performClick()
            }
        }

        buttonExit = findViewById(R.id.button_exit)
        buttonExit.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            defaultReturn()
        }


        buttonSave = findViewById(R.id.button_save)
        buttonSave.text = getString(R.string.save_button)

        buttonSave.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            when(viewModel.getFragmentName()) {
                FragmentName.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.multiselectMode.value == true) {
                        DialogHelper.setAndShowDefaultDialog(
                            context = this,
                            viewModel = viewModel,
                            messageText = if(viewModel.getSelectedSize() == 1) {
                                getString(
                                    R.string.confirm_remove_single_tool,
                                    viewModel.getSelectedWidgetName(),
                                )
                            } else {
                                getString(
                                    R.string.confirm_remove_tools,
                                    viewModel.getSelectedSize(),
                                )
                            },
                            positiveAction = {
                                val toastString = if(viewModel.getSelectedSize() == 1) {
                                    getString(
                                        R.string.remove_success_single_tool,
                                        viewModel.getSelectedWidgetName(),
                                    )
                                } else {
                                    getString(
                                        R.string.remove_success_tools,
                                        viewModel.getSelectedSize(),
                                    )
                                }
                                viewModel.deleteSelectedItemAndSave()
//                                updateTime()
//                                buttonSave.visibility = View.INVISIBLE
                                hideSaveButton()
                                Toast.makeText(
                                    this@MainActivity,
                                    toastString,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        )
                        return@setOnClickListener
                    }
                    if(viewModel.sortMode.value == true) {
                        viewModel.saveWidgetList()
//                        updateTime()
//                        buttonSave.visibility = View.INVISIBLE
                        hideSaveButton()
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.save_success),
                            Toast.LENGTH_SHORT,
                        ).show()
                        return@setOnClickListener
                    }
                }

                FragmentName.SETTING_FRAGMENT -> {
                    viewModel.saveUserConfig()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@setOnClickListener
                }

                FragmentName.SCHEME_LIST_FRAGMENT -> {
                    viewModel.saveDynamicShortcutList()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                    return@setOnClickListener
                }

            }

            viewModel.editedTool.value?.lastModifiedTime = System.currentTimeMillis()
            viewModel.saveWidgetList()
            Toast.makeText(
                this@MainActivity,
                getString(R.string.save_success),
                Toast.LENGTH_SHORT,
            ).show()
//            updateTime()
//            buttonSave.visibility = View.INVISIBLE
            hideSaveButton()
        }

    }


    // 显示保存按钮
    private fun showSaveButton(text: String = getString(R.string.save)) {
        if(viewModel.isWatch) {
            buttonSave.visibility = View.VISIBLE
            buttonSave.text = text
        } else {
            draggableTextView.setInitialPosition()
            draggableTextView.visibility = View.VISIBLE
            draggableTextView.text = text
        }
    }


    // 隐藏保存按钮
    private fun hideSaveButton(text: String = getString(R.string.save)) {
        if(viewModel.isWatch) {
            buttonSave.visibility = View.INVISIBLE
            buttonSave.text = text
        } else {
            draggableTextView.visibility = View.INVISIBLE
            draggableTextView.text = text
        }
    }


    // 显示退出按钮
    fun showExitButton() {
        buttonExit.visibility = View.VISIBLE
    }


    // 隐藏退出按钮
    fun hideExitButton() {
        buttonExit.visibility = View.INVISIBLE
    }


    // 设置 Navigation Component
    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.constraintLayout_origin) as NavHostFragment
        navController = navHostFragment.navController
    }


    // 设置观察者
    private fun setupObservers() {
        // 列表顺序观察者
        widgetListOrderWasModifiedObserver = Observer { widgetListOrderWasModified ->
//            Log.e("顺序观察者", "$widgetListOrderWasModified")
            if(widgetListOrderWasModified) {
//                hideTimeAndRestoreClick()
//                buttonSave.visibility = View.VISIBLE
                showSaveButton()
            } else {
//                buttonSave.visibility = View.INVISIBLE
//                updateTime()
                hideSaveButton()
            }
        }
        viewModel.toolListOrderChanged.observe(this, widgetListOrderWasModifiedObserver)


        // 列表大小观察者
        widgetListSizeWasModifiedObserver = Observer { widgetListSizeWasModified ->
            // Log.e("大小观察者", "$widgetListSizeWasModified")
            when(viewModel.getFragmentName()) {
                FragmentName.EDIT_LIST_FRAGMENT -> {
                    // 编辑列表存在大小改变和组件改变，必须同时不变才取消保存按钮
                    if(widgetListSizeWasModified || viewModel.toolChanged.value == true) {
//                        buttonSave.visibility = View.VISIBLE
//                        hideTimeAndRestoreClick()
                        showSaveButton()
                    } else {
//                        buttonSave.visibility = View.INVISIBLE
//                        updateTime()
                        hideSaveButton()
                    }
                }

                else -> {
                    if(widgetListSizeWasModified) {
//                        buttonSave.visibility = View.VISIBLE
//                        hideTimeAndRestoreClick()
                        showSaveButton()
                    } else {
//                        buttonSave.visibility = View.INVISIBLE
//                        updateTime()
                        hideSaveButton()
                    }
                }
            }
        }
        viewModel.toolListSizeChanged.observe(this, widgetListSizeWasModifiedObserver)


        widgetWasModifiedObserver = Observer { widgetWasModified ->
            when(viewModel.getFragmentName()) {
                FragmentName.EDIT_LIST_FRAGMENT -> {
                    // 编辑列表存在大小改变和组件改变，必须同时不变才取消保存按钮
                    if(widgetWasModified || viewModel.toolListSizeChanged.value == true) {
//                        buttonSave.visibility = View.VISIBLE
//                        hideTimeAndRestoreClick()
                        showSaveButton()
                    } else {
//                        buttonSave.visibility = View.INVISIBLE
//                        updateTime()
                        hideSaveButton()
                    }
                }

                else -> {
                    if(widgetWasModified) {
//                        buttonSave.visibility = View.VISIBLE
//                        hideTimeAndRestoreClick()
                        showSaveButton()
                    } else {
//                        buttonSave.visibility = View.INVISIBLE
//                        updateTime()
                        hideSaveButton()
                    }
                }
            }
        }
        viewModel.toolChanged.observe(this, widgetWasModifiedObserver)


        multiSelectModeObserver = Observer { multiSelectMode ->
//             LogHelper.e(
//                 "多选观察者",
//                 "multiSelectMode: $multiSelectMode"
//             )
            if(multiSelectMode) {
//                buttonAdd.text = getString(R.string.save_button)
//                buttonExit.text = getString(R.string.return_button)
//                hideTimeAndRestoreClick(getString(R.string.delete))
                // 进入多选模式后，必然选中了一个条目
//                buttonSave.visibility = View.VISIBLE
                showSaveButton(getString(R.string.button_remove))
//                buttonSave.text = getString(R.string.delete_button)
//                buttonTime.text = getString(R.string.sort)
//                buttonTime.isClickable = true
            } else {
//                viewModel.widgetListOrderWasModified.value = false
////                buttonAdd.text = getString(R.string.add_button)
//                buttonSave.visibility = View.INVISIBLE
//                buttonExit.text = getString(R.string.exit_button)
//                buttonSave.text = getString(R.string.save_button)
                hideSaveButton()
//                updateTime()
//                buttonTime.isClickable = false
            }
//            viewModel.selectedIds.value?.clear()
            FragmentHelper.updateEnableBackPressedCallback(viewModel)
        }
        viewModel.multiselectMode.observe(this, multiSelectModeObserver)


        sortModeObserver = Observer { sortMode ->
            if(sortMode) {
//                buttonAdd.visibility = View.INVISIBLE
//                buttonExit.text = getString(R.string.return_button)
            } else {
//                viewModel.widgetListOrderWasModified.value = false
//                buttonSave.visibility = View.INVISIBLE
                hideSaveButton()
//                buttonExit.text = getString(R.string.exit_button)
            }
            FragmentHelper.updateEnableBackPressedCallback(viewModel)
        }
        viewModel.sortMode.observe(this, sortModeObserver)


        fragmentNameObserver = Observer { fragmentName ->

            FragmentHelper.updateEnableBackPressedCallback(viewModel)

//             Log.e(
//                 "fragmentNameObserver",
//                 "fragmentName: $fragmentName, isEditMode: ${viewModel.editMode.value}"
//             )
            when(fragmentName) {
                FragmentName.WIDGET_LIST_FRAGMENT -> {
                    if(viewModel.multiselectMode.value == true) {
//                        buttonTime.text = getString(R.string.sort)
//                        buttonTime.isClickable = true
                        if(viewModel.toolListOrderChanged.value == true) {
//                            buttonSave.visibility = View.VISIBLE
//                            hideTimeAndRestoreClick()
                            showSaveButton()
                        } else {
//                            buttonSave.visibility = View.INVISIBLE
//                            updateTime()
                            hideSaveButton()
                        }
                    } else {
//                        buttonAdd.text = getString(R.string.add_button)
//                        buttonSave.visibility = View.INVISIBLE
//                        updateTime()
                        hideSaveButton()
                    }
                }

                FragmentName.EDIT_LIST_FRAGMENT -> {
//                    updateTime()
//                    buttonTime.isClickable = false
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
                }

                FragmentName.INSTALLED_APP_LIST_FRAGMENT -> {
//                    updateTime()
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
//                    buttonAdd.text = getString(R.string.save_button)
                }

                FragmentName.SUPPORT_AUTHOR_FRAGMENT -> {
//                    updateTime()
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
                }

                FragmentName.ABOUT_PROJECT_FRAGMENT -> {
//                    updateTime()
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
                }

                FragmentName.SETTING_FRAGMENT -> {
//                    updateTime()
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
//                    buttonAdd.text = getString(R.string.save_button)
                }

                FragmentName.WEB_VIEW_FRAGMENT -> {
//                    updateTime()
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
                }
            }
//            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT && viewModel.multiselectMode.value != true && viewModel.sortMode.value != true) {
//                buttonExit.text = getString(R.string.exit_button)
//            } else {
//                buttonExit.text = getString(R.string.return_button)
//            }

//            if(fragmentName == FragmentName.WIDGET_LIST_FRAGMENT) {
//                tabLayout.visibility = View.VISIBLE
//            } else {
//                tabLayout.visibility = View.INVISIBLE
//            }

        }
        viewModel.fragmentName.observe(this, fragmentNameObserver)


        settingWasModifiedObserver = Observer { wasModified ->
            if(viewModel.getFragmentName() == FragmentName.SETTING_FRAGMENT) {
                if(!viewModel.isWatch) {
                    draggableTextView.setInitialPosition()
                }
                if(wasModified) {
//                    hideTimeAndRestoreClick()
//                    buttonSave.visibility = View.VISIBLE
                    showSaveButton()
                } else {
//                    updateTime()
//                    buttonSave.visibility = View.INVISIBLE
                    hideSaveButton()
                }
            }
        }
        viewModel.configChanged.observe(this, settingWasModifiedObserver)


        selectedIdsObserver = Observer { selectedIds ->
            if(viewModel.getFragmentName() == FragmentName.WIDGET_LIST_FRAGMENT) {
                if(viewModel.multiselectMode.value == true) {
                    if(selectedIds.isNotEmpty()) {
//                        hideTimeAndRestoreClick()
//                        buttonSave.visibility = View.VISIBLE
                        showSaveButton(getString(R.string.button_remove))
                    } else {
//                        updateTime()
//                        buttonSave.visibility = View.INVISIBLE
                        hideSaveButton()
                    }
                }
            }
        }
        viewModel.selectedIds.observe(this, selectedIdsObserver)


        enableBackPressedCallbackObserver = Observer { enableBackPressedCallback ->
            backPressedCallback.isEnabled = enableBackPressedCallback
            if(enableBackPressedCallback) {
                buttonExit.text = getString(R.string.return_button)
            } else {
                buttonExit.text = getString(R.string.exit_button)
            }
        }
        viewModel.enableBackPressedCallback.observe(this, enableBackPressedCallbackObserver)


        dynamicShortcutListWasChangedObserver = Observer { wasChanged ->
            if(wasChanged) {
                showSaveButton()
            } else {
                hideSaveButton()
            }
        }
        viewModel.dynamicShortcutIdListWasChanged.observe(
            this,
            dynamicShortcutListWasChangedObserver
        )
    }


    // 移除观察者
    private fun removeObservers() {
        viewModel.toolListOrderChanged.removeObserver(widgetListOrderWasModifiedObserver)
        viewModel.toolListSizeChanged.removeObserver(widgetListSizeWasModifiedObserver)
        viewModel.toolChanged.removeObserver(widgetWasModifiedObserver)
        viewModel.multiselectMode.removeObserver(multiSelectModeObserver)
        viewModel.fragmentName.removeObserver(fragmentNameObserver)
        viewModel.configChanged.removeObserver(settingWasModifiedObserver)
        viewModel.sortMode.removeObserver(sortModeObserver)
        viewModel.selectedIds.removeObserver(selectedIdsObserver)
        viewModel.enableBackPressedCallback.removeObserver(enableBackPressedCallbackObserver)
        viewModel.dynamicShortcutIdListWasChanged.removeObserver(
            dynamicShortcutListWasChangedObserver
        )
    }


    // 设置文件操作 launcher
    private fun setFileOperationLaunchers() {
        // 初始化导入文件 launcher
        importFileLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            val destinationFile = importDestinationFile
            val callback = importCallback

            when {
                uri != null && destinationFile != null -> {
                    try {
                        contentResolver.openInputStream(uri)?.use { inputStream ->
                            destinationFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        callback?.invoke(FileOperationResult.SUCCESS)
                    } catch(e: Exception) {
                        e.printStackTrace()
                        callback?.invoke(FileOperationResult.FAIL)
                    }
                }

                uri == null -> {
                    callback?.invoke(FileOperationResult.CANCEL)
                }

                else -> {
                    callback?.invoke(FileOperationResult.FAIL)
                }
            }

            // 清理临时变量
            importCallback = null
            importDestinationFile = null
        }


        // 初始化导出文件 launcher
        exportFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val sourceFile = exportSourceFile
            val callback = exportCallback

            when(result.resultCode) {
                RESULT_OK -> {
                    if(sourceFile != null) {
                        val uri = result.data?.data
                        if(uri != null) {
                            try {
                                contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    sourceFile.inputStream().use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                                callback?.invoke(FileOperationResult.SUCCESS, uri)
                            } catch(e: Exception) {
                                e.printStackTrace()
                                callback?.invoke(FileOperationResult.FAIL, null)
                            }
                        } else {
                            callback?.invoke(FileOperationResult.FAIL, null)
                        }
                    } else {
                        callback?.invoke(FileOperationResult.FAIL, null)
                    }

                }

                RESULT_CANCELED -> {
                    callback?.invoke(FileOperationResult.CANCEL, null)
                }

                else -> {
                    callback?.invoke(FileOperationResult.FAIL, null)
                }
            }

            // 清理临时变量
            exportCallback = null
            exportSourceFile = null
        }
    }


    fun exportFile(
        sourceFile: File,
        fileName: String = sourceFile.name ?: getString(R.string.unknown_file),
        mimeType: String = FileHelper.getMimeType(sourceFile.absolutePath),
        onResult: (Int, Uri?) -> Unit,
    ) {
        exportSourceFile = sourceFile
        exportCallback = onResult
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        exportFileLauncher.launch(intent)
    }


    fun importFile(
        destinationFile: File,
        mimeTypes: Array<String> = arrayOf("*/*"),
        onResult: (Int) -> Unit,
    ) {
        importDestinationFile = destinationFile
        importCallback = onResult
        importFileLauncher.launch(mimeTypes)
    }

}
