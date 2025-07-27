/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.LaunchType
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

        if(!packageWasInstalled(myActivity, tool.packageName)) {
            Toast.makeText(
                myActivity,
                myActivity.getString(R.string.package_not_installed, tool.packageName),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        if(!tool.warningMessage.isNullOrBlank()) {
            VibrationHelper.vibrateOnDangerousOperation(viewModel)
            DialogHelper.showConfirmDialog(
                context = myActivity,
                viewModel = viewModel,
                titleText = myActivity.getString(R.string.warning),
                titleTextColor = myActivity.getColor(R.color.red),
                messageText = tool.warningMessage,
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


    // 获取 Intent 并启动
    private fun getIntentAndLaunch(
        myActivity: MainActivity,
        viewModel: MyViewModel,
        tool: Tool,
    ): Boolean {
        val intent = getIntent(context = myActivity, tool = tool)

        try {
//            if(intent != null && isIntentAvailable(context, intent)) {
            startActivity(myActivity, intent!!, null)
            // 使用计数器加 1
            if(tool is StoredTool) {
                tool.useCount += 1u
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
                Toast.LENGTH_SHORT,
            ).show()
            return false
        }
    }


    // 是否在启动后退出
    fun exitAfterLaunch(
        viewModel: MyViewModel,
    ): Boolean {
        return viewModel.getConfigValue(Config.ConfigKeys.EXIT_AFTER_LAUNCH) as Boolean
    }


    // 获取 Intent
    private fun getIntent(context: Context, tool: Tool): Intent? {

        return when(tool.intentType) {
            LaunchType.PACKAGE_AND_ACTIVITY -> createPackageIntent(
                context = context, tool = tool,
            )

            LaunchType.SCHEME -> createSchemeIntent(tool)

            LaunchType.PACKAGE -> createPackageIntent(
                context = context, tool = tool,
            )

            LaunchType.ACTION -> createPackageIntent(
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
        if(!tool.warningMessage.isNullOrBlank() && tool.id in ToolMap.idToTool) {
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
                is String -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                is Double -> putExtra(key, value)
                is Float -> putExtra(key, value)
                is Long -> putExtra(key, value)
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


//    // 判断是否有活动可以响应该 Intent
//    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
//        val packageManager = context.packageManager
//        val activities = packageManager.queryIntentActivities(intent, 0)
//        return activities.isNotEmpty()
//    }
}
