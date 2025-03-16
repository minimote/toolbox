/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import cn.minimote.toolbox.R


object ClipboardHelper {

    // 复制到剪切板
    fun copyToClipboard(
        context: Context,
        text: String,
        toastString: String? = "",
        label: String = "",
    ) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            label,
            text,
        )
        clipboard.setPrimaryClip(clip)
        if(toastString != null) {
            Toast.makeText(
                context,
                toastString + context.getString(R.string.clipboard_copy_success),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}