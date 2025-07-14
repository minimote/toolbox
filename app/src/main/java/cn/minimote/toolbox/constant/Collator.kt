/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

import java.text.Collator
import java.util.Locale

object Collator {
    val chineseCollator: Collator = Collator.getInstance(Locale.CHINA)
}