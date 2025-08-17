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
        if(actualQuery.isEmpty()) return SpannableString(text)

        val spannableString = SpannableString(text)

        // 查找所有匹配项并高亮
        val matches = findAllMatches(text, actualQuery)
        for(match in matches) {
            spannableString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(viewModel.myContext, highlightColorId)
                ),
                match.start,
                match.end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

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
        if(actualQuery.isEmpty()) return true

        return findAllMatches(text, actualQuery).isNotEmpty()
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
     * 查找文本中所有匹配项的位置
     *
     * @param text 原始文本
     * @param query 搜索关键词
     * @return 匹配项位置列表
     */
    private fun findAllMatches(text: String, query: String): List<TextMatch> {
        if(query.isEmpty()) return emptyList()

        val matches = mutableSetOf<TextMatch>()

        // 1. 直接文本匹配
        addDirectMatches(matches, text, query)

        // 2. 拼音匹配
        addPinyinMatches(matches, text, query)

        // 3. 拼音首字母匹配
        addPinyinFirstLetterMatches(matches, text, query)

        // 合并重叠的匹配项
        return mergeOverlappingMatches(matches.toList())
    }

    /**
     * 添加直接文本匹配项
     */
    private fun addDirectMatches(matches: MutableSet<TextMatch>, text: String, query: String) {
        val lowerCaseText = text.lowercase(Locale.getDefault())
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        var index = lowerCaseText.indexOf(lowerCaseQuery)
        while(index >= 0) {
            matches.add(TextMatch(index, index + query.length))
            index = lowerCaseText.indexOf(lowerCaseQuery, index + 1)
        }
    }

    /**
     * 添加拼音匹配项
     */
    private fun addPinyinMatches(matches: MutableSet<TextMatch>, text: String, query: String) {
        val pinyinChars = generatePinyinChars(text)
        val lowerCaseQuery = query.lowercase(Locale.getDefault())

        // 查找连续拼音组合的前缀匹配
        for(i in pinyinChars.indices) {
            val combinedPinyin = StringBuilder()
            for(j in i until pinyinChars.size) {
                combinedPinyin.append(pinyinChars[j].pinyin)
                val combinedPinyinStr = combinedPinyin.toString().lowercase(Locale.getDefault())

                // 如果查询词是组合拼音的前缀，则高亮对应的字符范围
                if(combinedPinyinStr.startsWith(lowerCaseQuery)) {
                    matches.add(
                        TextMatch(
                            pinyinChars[i].originalIndex,
                            pinyinChars[j].originalIndex + 1
                        )
                    )
                    break // 找到匹配后跳出内层循环
                }

                // 如果组合拼音已经比查询词长且不匹配，则跳出内层循环
                if(combinedPinyinStr.length >= lowerCaseQuery.length) {
                    break
                }
            }
        }

        // 查找单个字符的拼音前缀匹配
        for(pinyinChar in pinyinChars) {
            if(pinyinChar.pinyin.lowercase(Locale.getDefault()).startsWith(lowerCaseQuery)) {
                matches.add(TextMatch(pinyinChar.originalIndex, pinyinChar.originalIndex + 1))
            }
        }
    }

    /**
     * 添加拼音首字母匹配项
     */
    private fun addPinyinFirstLetterMatches(
        matches: MutableSet<TextMatch>,
        text: String,
        query: String
    ) {
        val pinyinFirstLetters = generatePinyinFirstLetters(text)
        val lowerCaseQuery = query.lowercase(Locale.getDefault())
        val lowerCasePinyinFirstLetters = pinyinFirstLetters.lowercase(Locale.getDefault())

        // 查找拼音首字母匹配
        var index = lowerCasePinyinFirstLetters.indexOf(lowerCaseQuery)
        while(index >= 0) {
            // 计算在原始文本中的实际位置
            var textStartIndex = 0
            var firstLetterCount = 0

            for(i in text.indices) {
                val char = text[i]
                if(Pinyin.isChinese(char) || char.isLetter()) {
                    if(firstLetterCount == index) {
                        textStartIndex = i
                        break
                    }
                    firstLetterCount++
                }
            }

            // 计算结束位置
            var textEndIndex = textStartIndex
            var matchedLetterCount = 0

            for(i in textStartIndex until text.length) {
                val char = text[i]
                if(Pinyin.isChinese(char) || char.isLetter()) {
                    matchedLetterCount++
                    textEndIndex = i + 1
                    if(matchedLetterCount == query.length) {
                        break
                    }
                }
            }

            matches.add(TextMatch(textStartIndex, textEndIndex))

            index = lowerCasePinyinFirstLetters.indexOf(lowerCaseQuery, index + 1)
        }
    }

    /**
     * 合并重叠的匹配项
     */
    private fun mergeOverlappingMatches(matches: List<TextMatch>): List<TextMatch> {
        if(matches.isEmpty()) return emptyList()

        // 按起始位置排序
        val sortedMatches = matches.sortedBy { it.start }
        val mergedMatches = mutableListOf<TextMatch>()
        var currentMatch = sortedMatches[0]

        for(i in 1 until sortedMatches.size) {
            val nextMatch = sortedMatches[i]
            if(currentMatch.end >= nextMatch.start) {
                // 重叠，合并区间
                currentMatch = TextMatch(currentMatch.start, maxOf(currentMatch.end, nextMatch.end))
            } else {
                // 不重叠，添加当前区间，更新当前区间
                mergedMatches.add(currentMatch)
                currentMatch = nextMatch
            }
        }

        mergedMatches.add(currentMatch)
        return mergedMatches
    }

    /**
     * 生成拼音字符映射
     *
     * @param text 原始文本
     * @return 拼音字符列表
     */
    private fun generatePinyinChars(text: String): List<PinyinChar> {
        val pinyinChars = mutableListOf<PinyinChar>()

        for(i in text.indices) {
            val char = text[i]
            if(Pinyin.isChinese(char)) {
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
     * 生成拼音首字母字符串
     *
     * @param text 原始文本
     * @return 拼音首字母字符串
     */
    private fun generatePinyinFirstLetters(text: String): String {
        val firstLetters = StringBuilder()

        for(i in text.indices) {
            val char = text[i]
            if(Pinyin.isChinese(char)) {
                // 对于中文字符，获取拼音首字母
                val pinyin = Pinyin.toPinyin(char)
                if(pinyin.isNotEmpty()) {
                    firstLetters.append(pinyin[0])
                }
            } else if(char.isLetter()) {
                // 对于非中文字符，直接添加字符本身
                firstLetters.append(char)
            }
        }

        return firstLetters.toString()
    }

    /**
     * 文本匹配项数据类
     *
     * @param start 匹配项起始位置
     * @param end 匹配项结束位置
     */
    private data class TextMatch(val start: Int, val end: Int)

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
