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
import cn.minimote.toolbox.constant.LaunchTypes
import cn.minimote.toolbox.dataClass.ToolActivity
import java.io.Serializable

object ActivityLaunchHelper {

    fun launch(context: Context, toolActivity: ToolActivity): Boolean {

        if(!packageWasInstalled(context, toolActivity.packageName)) {
            Toast.makeText(
                context,
                context.getString(R.string.package_not_installed, toolActivity.packageName),
                Toast.LENGTH_SHORT,
            ).show()
            return false
        }

        val intent = getIntent(context = context, toolActivity = toolActivity)

        if(intent != null && isIntentAvailable(context, intent)) {
            startActivity(context, intent, null)
            return true
        } else {
            // 启动失败，显示错误信息
            Toast.makeText(
                context,
                context.getString(R.string.start_fail, toolActivity.name),
                Toast.LENGTH_SHORT,
            ).show()
            return false
        }
    }


    // 获取 Intent
    fun getIntent(context: Context, toolActivity: ToolActivity): Intent? {

        return when(toolActivity.launchType) {
            LaunchTypes.PACKAGE_AND_ACTIVITY -> createPackageIntent(
                context = context, toolActivity = toolActivity,
            )

            LaunchTypes.SCHEME -> createSchemeIntent(toolActivity)

            LaunchTypes.PACKAGE -> createPackageIntent(
                context = context, toolActivity = toolActivity,
            )

            LaunchTypes.ACTION -> createPackageIntent(
                context = context, toolActivity = toolActivity,
            )

            else -> null
        }
    }


    private fun createPackageIntent(
        context: Context,
        toolActivity: ToolActivity,
    ): Intent? {
        val packageName = toolActivity.packageName
        val activityName = toolActivity.activityName

        return (if(activityName != null) {
            Intent().apply {
                component = ComponentName(packageName, activityName)
            }
        } else {
            context.packageManager.getLaunchIntentForPackage(packageName)
        })?.setParams(toolActivity)
    }

    private fun createSchemeIntent(toolActivity: ToolActivity): Intent? {
        val uriString = toolActivity.intentUri ?: return null
        val uri = uriString.toUri()

        return Intent(Intent.ACTION_VIEW, uri).apply {
        }.setParams(toolActivity)
    }

//    private fun createActionIntent(storedActivity: StoredActivity): Intent? {
//        val action = storedActivity.intentAction ?: return null
//
//        return Intent(action).apply {
//            addCategory(storedActivity.intentCategory)
//            flags = storedActivity.intentFlags.sum()
//        }
//    }

    fun Intent.setParams(toolActivity: ToolActivity): Intent {
        action = toolActivity.intentAction
        addCategory(toolActivity.intentCategory)
        flags = toolActivity.intentFlag
        toolActivity.intentExtras?.forEach { (key, value) ->
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


    // 判断是否有活动可以响应该 Intent
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intent, 0)
        return activities.isNotEmpty()
    }
}
