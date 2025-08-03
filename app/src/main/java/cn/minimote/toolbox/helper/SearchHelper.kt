/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import cn.minimote.toolbox.R
import cn.minimote.toolbox.viewModel.MyViewModel
import com.github.promeg.pinyinhelper.Pinyin
import java.util.Locale

object SearchHelper {

    /**
     * 高亮显示搜索结果中的匹配项（支持拼音匹配），根据设备类型决定是否高亮
     *
     * @param viewModel MyViewModel 实例，用于获取 Context、查询词和设备类型
     * @param text 需要处理的文本
     * @param query 搜索关键词，如果为空则使用 viewModel 中的查询词
     * @param highlightColorId 高亮颜色资源 ID
     * @return 带有高亮标记的SpannableString
     */
    fun highlightSearchTermForDevice(
        viewModel: MyViewModel,
        text: String,
        query: String = viewModel.searchQuery.value.orEmpty(),
        highlightColorId: Int = R.color.primary,
    ): CharSequence {
        // 手表设备不进行高亮处理
//        if (viewModel.isWatch) {
//            return text
//        }

        return highlightSearchTerm(viewModel, text, query, highlightColorId)
    }

    /**
     * 高亮显示搜索结果中的匹配项（支持拼音匹配）
     *
     * @param viewModel MyViewModel 实例，用于获取 Context 和查询词
     * @param text 需要处理的文本
     * @param query 搜索关键词，如果为空则使用 viewModel 中的查询词
     * @param highlightColorId 高亮颜色资源 ID
     * @return 带有高亮标记的SpannableString
     */
    fun highlightSearchTerm(
        viewModel: MyViewModel,
        text: String,
        query: String = viewModel.searchQuery.value.orEmpty(),
        highlightColorId: Int = R.color.primary,
    ): SpannableString {
        val actualQuery = query.trim()
        if (actualQuery.isEmpty()) return SpannableString(text)

        val spannableString = SpannableString(text)

        // 查找普通匹配
        highlightDirectMatches(viewModel, spannableString, text, actualQuery, highlightColorId)

        // 查找拼音匹配
        highlightPinyinMatches(viewModel, spannableString, text, actualQuery, highlightColorId)

        return spannableString
    }

    /**
     * 检查文本是否与搜索词匹配（支持拼音匹配）
     *
     * @param viewModel MyViewModel 实例，用于获取查询词
     * @param text 需要检查的文本
     * @param query 搜索关键词，如果为空则使用 viewModel 中的查询词
     * @return 如果匹配返回true，否则返回false
     */
    fun isTextMatchQuery(
        viewModel: MyViewModel,
        text: String,
        query: String = viewModel.searchQuery.value.orEmpty(),
    ): Boolean {
        val actualQuery = query.trim()
        if (actualQuery.isEmpty()) return true

        val lowerCaseText = text.lowercase(Locale.getDefault())
        val lowerCaseQuery = actualQuery.lowercase(Locale.getDefault())

        // 直接文本匹配
        if (lowerCaseText.contains(lowerCaseQuery)) {
            return true
        }

        // 拼音匹配（检查文本中任何部分的拼音是否匹配查询词）
        val pinyinString = Pinyin.toPinyin(text, "").lowercase(Locale.getDefault())
        return pinyinString.contains(lowerCaseQuery)
    }

    /**
     * 检查字符串列表中是否有任何一个与搜索词匹配（支持拼音匹配）
     *
     * @param viewModel MyViewModel 实例，用于获取查询词
     * @param textList 需要检查的字符串列表
     * @param query 搜索关键词，如果为空则使用 viewModel 中的查询词
     * @return 如果有任何一个匹配返回true，否则返回false
     */
    fun isAnyTextMatchQuery(
        viewModel: MyViewModel,
        textList: List<String>,
        query: String = viewModel.searchQuery.value.orEmpty().trim(),
    ): Boolean {
        val actualQuery = query.trim()
        return textList.any { text -> isTextMatchQuery(viewModel, text, actualQuery) }
    }

    /**
     * 高亮显示直接匹配项
     *
     * @param viewModel MyViewModel 实例，用于获取 Context
     * @param spannableString SpannableString 对象
     * @param text 原始文本
     * @param query 搜索关键词
     * @param highlightColorId 高亮颜色资源 ID
     */
    private fun highlightDirectMatches(
        viewModel: MyViewModel,
        spannableString: SpannableString,
        text: String,
        query: String,
        highlightColorId: Int
    ) {
        if (query.isEmpty()) return

        val lowerCaseText = text.lowercase(Locale.getDefault())
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        var index = lowerCaseText.indexOf(lowerCaseQuery)
        while(index >= 0) {
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(viewModel.myContext, highlightColorId)
                ),
                index,
                index + query.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            index = lowerCaseText.indexOf(lowerCaseQuery, index + query.length)
        }
    }

    /**
     * 高亮显示拼音匹配项
     *
     * @param viewModel MyViewModel 实例，用于获取 Context
     * @param spannableString SpannableString 对象
     * @param text 原始文本
     * @param query 搜索关键词
     * @param highlightColorId 高亮颜色资源 ID
     */
    private fun highlightPinyinMatches(
        viewModel: MyViewModel,
        spannableString: SpannableString,
        text: String,
        query: String,
        highlightColorId: Int
    ) {
        if (query.isEmpty()) return

        // 生成每个字符的拼音映射
        val pinyinChars = generatePinyinChars(text)

        // 查找连续拼音组合的前缀匹配
        for (i in pinyinChars.indices) {
            val combinedPinyin = StringBuilder()
            for (j in i until pinyinChars.size) {
                combinedPinyin.append(pinyinChars[j].pinyin)
                val combinedPinyinStr = combinedPinyin.toString().lowercase(Locale.getDefault())
                val queryLower = query.lowercase(Locale.getDefault())

                // 如果查询词是组合拼音的前缀，则高亮对应的字符范围
                if (combinedPinyinStr.startsWith(queryLower)) {
                    spannableString.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(viewModel.myContext, highlightColorId)
                        ),
                        pinyinChars[i].originalIndex,
                        pinyinChars[j].originalIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    break // 找到匹配后跳出内层循环
                }

                // 如果组合拼音已经比查询词长且不匹配，则跳出内层循环
                if (combinedPinyinStr.length >= queryLower.length) {
                    break
                }
            }
        }

        // 查找单个字符的拼音前缀匹配
        for (pinyinChar in pinyinChars) {
            if (pinyinChar.pinyin.lowercase(Locale.getDefault()).startsWith(query.lowercase(Locale.getDefault()))) {
                // 检查该位置是否已经被高亮过
                val spans = spannableString.getSpans(
                    pinyinChar.originalIndex,
                    pinyinChar.originalIndex + 1,
                    ForegroundColorSpan::class.java
                )

                // 如果还没有被高亮，则添加拼音匹配的高亮
                if (spans.isEmpty()) {
                    // 高亮匹配的中文字符
                    spannableString.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(viewModel.myContext, highlightColorId)
                        ),
                        pinyinChar.originalIndex,
                        pinyinChar.originalIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }

    /**
     * 生成拼音字符映射
     *
     * @param text 原始文本
     * @return 拼音字符列表
     */
    private fun generatePinyinChars(text: String): List<PinyinChar> {
        val pinyinChars = mutableListOf<PinyinChar>()

        for (i in text.indices) {
            val char = text[i]
            if (Pinyin.isChinese(char)) {
                // 对于中文字符，获取拼音（不带声调）
                val pinyin = Pinyin.toPinyin(char)
                pinyinChars.add(PinyinChar(char, pinyin, i))
            } else {
                // 对于非中文字符，拼音就是字符本身
                pinyinChars.add(PinyinChar(char, char.toString(), i))
            }
        }

        return pinyinChars
    }

    /**
     * 拼音字符类
     *
     * @param char 原始字符
     * @param pinyin 拼音
     * @param originalIndex 在原始文本中的索引
     */
    private data class PinyinChar(
        val char: Char,
        val pinyin: String,
        val originalIndex: Int
    )
}
