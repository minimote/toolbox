/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.IntentType
import cn.minimote.toolbox.constant.ToolID
import cn.minimote.toolbox.constant.ToolMap
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.Serializable

object LaunchHelper {

    fun launch(
        myActivity: MainActivity,
        viewModel: MyViewModel,
        tool: Tool,
    ) {
        if(tool.intentType == IntentType.FRAGMENT) {
            launchFragment(
                myActivity = myActivity,
                viewModel = viewModel,
                tool = tool,
            )
            return
        }

//        if(!packageWasInstalled(myActivity, tool.packageName)) {
//            Toast.makeText(
//                myActivity,
//                myActivity.getString(R.string.package_not_installed, tool.packageName),
//                Toast.LENGTH_SHORT,
//            ).show()
//            return
//        }

        if(!tool.warningMessage.isNullOrBlank()) {
            VibrationHelper.vibrateOnDangerousOperation(viewModel)
            DialogHelper.setAndShowDefaultDialog(
                context = myActivity,
                viewModel = viewModel,
                titleText = myActivity.getString(R.string.warning),
                titleTextColor = myActivity.getColor(R.color.red),
                messageTextList = listOf(tool.warningMessage.trim()),
                positiveAction = {
                    getIntentAndLaunch(
                        myActivity = myActivity,
                        viewModel = viewModel,
                        tool = tool,
                    )
                },
                positiveButtonTextColor = myActivity.getColor(R.color.red),
            )
        } else {
            getIntentAndLaunch(
                myActivity = myActivity,
                viewModel = viewModel,
                tool = tool,
            )
        }
    }


    fun launch(
        myActivity: MainActivity,
        viewModel: MyViewModel,
        intent: Intent,
        packageName: String? = null,
    ) {

        if(!packageName.isNullOrBlank()) {
            if(!packageWasInstalled(myActivity, packageName)) {
                Toast.makeText(
                    myActivity,
                    myActivity.getString(R.string.package_not_installed, packageName),
                    Toast.LENGTH_SHORT,
                ).show()
                return
            }
        }
        try {
            myActivity.startActivity(intent)
            if(exitAfterLaunch(viewModel)) {
                myActivity.finish()
            }
        } catch(e: Exception) {
            // 启动失败，显示错误信息
            Toast.makeText(
                myActivity,
                myActivity.getString(
                    R.string.start_fail,
                    e.javaClass.simpleName,
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }


    // 运行 Fragment
    fun launchFragment(
        myActivity: MainActivity,
        viewModel: MyViewModel,
        tool: Tool,
    ) {
        when(tool.id) {
            ToolID.Other.WOODEN_FISH -> {
                FragmentHelper.switchFragment(
                    fragmentName = FragmentName.WOODEN_FISH_FRAGMENT,
                    activity = myActivity,
                    viewModel = viewModel,
                )
            }
        }
    }


    // 获取 Intent 并启动
    private fun getIntentAndLaunch(
        myActivity: MainActivity,
        viewModel: MyViewModel,
        tool: Tool,
    ): Boolean {
        val intent = getIntent(context = myActivity, tool = tool)

        try {

//            // 创建实时活动显示"点击返回工具箱"
//            createLiveActivityForReturn(myActivity, tool)

//            if(intent != null && isIntentAvailable(context, intent)) {
            myActivity.startActivity(intent)

            // 使用计数器加 1
            if(tool is StoredTool) {
                tool.useCount += 1
                tool.lastUsedTime = System.currentTimeMillis()
                viewModel.saveWidgetList()
            }
            if(exitAfterLaunch(viewModel)) {
                myActivity.finish()
            }
            return true
//            }
//            // 启动失败，显示错误信息
//            Toast.makeText(
//                context,
//                context.getString(R.string.start_fail, tool.name),
//                Toast.LENGTH_SHORT,
//            ).show()
//            return false
        } catch(e: Exception) {
            // 启动失败，显示错误信息
            Toast.makeText(
                myActivity,
                myActivity.getString(
                    R.string.start_fail,
                    e.javaClass.simpleName,
                ),
                Toast.LENGTH_LONG,
            ).show()

            LogHelper.e(
                "启动失败",
                e.toString(),
            )

            return false
        }
    }


    private fun createLiveActivityForReturn(context: Context, tool: Tool) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // 在应用启动时或需要发送通知前初始化
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            SnackbarNotificationManager.initialize(context.applicationContext, notificationManager)
            // 调用start方法开始发送通知序列
            SnackbarNotificationManager.start()

        }
//        try {
//            showLiveActivityNotification(context, tool)
//        } catch(e: Exception) {
//            // 实时活动创建失败，可以记录日志
//            e.printStackTrace()
//        }
    }


    private fun showLiveActivityNotification(context: Context, tool: Tool) {

    }


    // 是否在启动后退出
    fun exitAfterLaunch(
        viewModel: MyViewModel,
    ): Boolean {
        return viewModel.getConfigValue(
            Config.ConfigKeys.Launch.EXIT_AFTER_LAUNCH
        ) as? Boolean ?: false
    }


    // 获取 Intent
    private fun getIntent(context: Context, tool: Tool): Intent? {

        return when(tool.intentType) {
            IntentType.PACKAGE_AND_ACTIVITY -> createPackageIntent(
                context = context, tool = tool,
            )

            IntentType.SCHEME -> createSchemeIntent(tool)

            IntentType.PACKAGE -> createPackageIntent(
                context = context, tool = tool,
            )

            IntentType.ACTION -> createPackageIntent(
                context = context, tool = tool,
            )

            else -> null
        }
    }


    private fun createPackageIntent(
        context: Context,
        tool: Tool,
    ): Intent? {
        val packageName = tool.packageName
        val activityName = tool.activityName

        return (if(activityName != null) {
            Intent().apply {
                component = ComponentName(packageName, activityName)
            }
        } else {
            context.packageManager.getLaunchIntentForPackage(packageName)
        })?.setParams(tool)
    }


    private fun createSchemeIntent(tool: Tool): Intent? {
        val uriString = tool.intentUri ?: return null
        val uri = uriString.toUri()

        return Intent(tool.intentAction, uri).setParams(tool)
    }


    // 获取快捷方式的 Intent
    fun getShortcutIntent(context: Context, tool: Tool): Intent? {
        // 存在警告信息的工具只能通过本软件启动
        if(
            (!tool.warningMessage.isNullOrBlank() && tool.id in ToolMap.idToTool)
            || tool.intentType == IntentType.FRAGMENT
        ) {
            val scheme = SchemeHelper.getSchemeFromId(tool.id)
            return Intent(Intent.ACTION_VIEW, scheme.toUri())
        }
        return getIntent(context = context, tool = tool)
    }


    fun Intent.setParams(tool: Tool): Intent {
        action = tool.intentAction
        addCategory(tool.intentCategory)
        flags = tool.intentFlag
        tool.intentExtras?.forEach { (key, value) ->
            when(value) {
                is Int -> putExtra(key, value)
                is Long -> putExtra(key, value)
                is Float -> putExtra(key, value)
                is Double -> putExtra(key, value)
                is String -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                is Serializable -> putExtra(key, value)
                else -> Unit // 可记录日志或抛出异常
            }
        }
        return this
    }


    // 判断该应用是否安装
    fun packageWasInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch(_: Exception) {
            false
        }
    }


    // 判断工具是否可用
    fun isToolAvailable(context: Context, tool: Tool): Boolean {
        if(tool.intentType == IntentType.FRAGMENT) {
            return true
        }
        val intent = getIntent(context = context, tool = tool)
        return intent != null && isIntentAvailable(context, intent)
    }


    // 判断是否有活动可以响应该 Intent
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)
        return activities.isNotEmpty()
    }
}
