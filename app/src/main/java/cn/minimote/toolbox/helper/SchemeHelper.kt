/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.net.Uri
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Scheme
import cn.minimote.toolbox.constant.ToolMap
import cn.minimote.toolbox.viewModel.MyViewModel

object SchemeHelper {

    // 处理传入的 Scheme
    fun handleScheme(
        uri: Uri,
        myActivity: MainActivity,
        viewModel: MyViewModel,
    ) {
        if(uri.scheme == Scheme.SCHEME && uri.host == Scheme.HOST && uri.path != null) {
            val tool = ToolMap.idToTool[uri.path?.drop(1)]
//            LogHelper.e(
//                "SchemeHelper",
//                "scheme: ${uri.scheme}, host: ${uri.host}, query: ${uri.query}, path: ${uri.path}"
//            )
            if(tool != null) {
                LaunchHelper.launch(
                    myActivity = myActivity,
                    viewModel = viewModel,
                    tool = tool,
                )
            }
        }
    }

    // 从 ID 获取 Scheme
    fun getSchemeFromId(
        id: String,
    ): String {
        if(id in ToolMap.idToTool) {
            return Scheme.SCHEME + "://" + Scheme.HOST + "/" + id
        }
        return ""
//        return context.getString(R.string.scheme_not_found)
    }
}