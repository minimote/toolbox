/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.SeekBarConstants.SEARCH_HISTORY_MAX_COUNT_LIMIT
import cn.minimote.toolbox.constant.SeekBarConstants.SEARCH_SUGGESTION_MAX_COUNT_LIMIT
import cn.minimote.toolbox.dataClass.ExpandableGroup
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.deleteSearchHistoryConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.deleteSearchSuggestionConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.getSearchHistoryConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.getSearchSuggestionConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.saveSearchHistoryConfig
import cn.minimote.toolbox.helper.OtherConfigHelper.saveSearchSuggestionConfig
import cn.minimote.toolbox.helper.OtherConfigHelper.updateSearchHistoryConfigValue
import cn.minimote.toolbox.helper.OtherConfigHelper.updateSearchSuggestionConfigValue
import cn.minimote.toolbox.helper.TypeConversionHelper.toStringList
import cn.minimote.toolbox.viewModel.MyViewModel
import com.github.promeg.pinyinhelper.Pinyin
import org.json.JSONObject
import java.util.Locale

object SearchHelper {

    const val HISTORY_MAX_COUNT_KEY = Config.ConfigKeys.SearchHistory.MAX_COUNT
    const val SUGGESTION_MAX_COUNT_KEY = Config.ConfigKeys.SearchSuggestion.MAX_COUNT


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
        query: String,
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
        query: String,
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
                    viewModel.myContext.getColor(highlightColorId)
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
     * @param text 需要检查的文本
     * @param query 搜索关键词，如果为空则使用 viewModel 中的查询词
     * @return 如果匹配返回true，否则返回false
     */
    fun isTextMatchQuery(
        text: String,
        query: String,
    ): Boolean {
        val actualQuery = query.trim()
        if(actualQuery.isEmpty()) return true

        return findAllMatches(text, actualQuery).isNotEmpty()
    }


    /**
     * 检查字符串列表中是否有任何一个与搜索词匹配（支持拼音匹配）
     * @param textList 需要检查的字符串列表
     * @param query 搜索关键词，如果为空则使用 viewModel 中的查询词
     * @return 如果有任何一个匹配返回true，否则返回false
     */
    fun isAnyTextMatchQuery(
        textList: List<String>,
        query: String,
    ): Boolean {
        val actualQuery = query.trim()
        return textList.any { text -> isTextMatchQuery(text, actualQuery) }
    }


    /**
     * 查找文本中所有匹配项的位置
     *
     * @param text 原始文本
     * @param query 搜索关键词
     * @return 匹配项位置列表
     */
    private fun findAllMatches(
        text: String,
        query: String,
    ): List<TextMatch> {
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
    private fun addDirectMatches(
        matches: MutableSet<TextMatch>,
        text: String,
        query: String,
    ) {
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
    private fun addPinyinMatches(
        matches: MutableSet<TextMatch>,
        text: String,
        query: String,
    ) {
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
        query: String,
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


    /**
     * 获取搜索历史
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @return 搜索历史列表
     */
    private fun getSearchHistoryList(
        viewModel: MyViewModel,
        configKey: String,
    ): List<String> {
        val historyList = viewModel.getSearchHistoryConfigValue(configKey).toStringList()
        // 截取前 n 个元素
        return historyList.take(SEARCH_HISTORY_MAX_COUNT_LIMIT)
    }


    /**
     * 获取搜索历史的 ExpandableGroup
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @return 搜索历史的 ExpandableGroup
     */
    fun getSearchHistoryExpandableGroup(
        viewModel: MyViewModel,
        configKey: String,
    ): ExpandableGroup {
        val maxDisplayedCount = viewModel.getConfigValue(
            HISTORY_MAX_COUNT_KEY
        ) as? Int? ?: SEARCH_HISTORY_MAX_COUNT_LIMIT

        return ExpandableGroup(
            titleString = viewModel.myContext.getString(R.string.search_history),
            dataList = getSearchHistoryList(
                viewModel = viewModel,
                configKey = configKey,
            ),
            maxDisplayedCount = maxDisplayedCount,
        )
    }


    /**
     * 更新搜索历史
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @param searchHistoryExpandableGroup 搜索历史的 ExpandableGroup
     * @param newWord 新增词
     */
    private fun updateSearchHistory(
        viewModel: MyViewModel,
        configKey: String,
        searchHistoryExpandableGroup: ExpandableGroup,
        newWord: String,
    ) {

        val trimmedNewWord = newWord.trim()
        if(trimmedNewWord.isEmpty()) {
            return
        }

        val oldList = searchHistoryExpandableGroup.dataList

        // 创建新的列表
        val newList = oldList.toMutableList()

        // 如果新词已存在，先移除它
        newList.remove(trimmedNewWord)

        // 将新词添加到最前面
        newList.add(0, trimmedNewWord)

        // 如果超过最大数量，只保留前面的部分
//        LogHelper.e("更新搜索历史", "新列表:${newList}")
        searchHistoryExpandableGroup.dataList = newList.take(
            SEARCH_HISTORY_MAX_COUNT_LIMIT
        )
        // 更新配置
        viewModel.updateSearchHistoryConfigValue(
            key = configKey,
            value = searchHistoryExpandableGroup.dataList,
        )
        return
    }


    /**
     * 获取搜索建议的 JSONObject
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @return 搜索建议的 JSONObject
     */
    fun getSearchSuggestionJSONObject(
        viewModel: MyViewModel,
        configKey: String,
    ): JSONObject {
        val configValue = viewModel.getSearchSuggestionConfigValue(configKey)
        return if(configValue != null) {
            JSONObject(configValue.toString())
        } else {
            // 如果配置值为空，返回一个空的JSONObject
            JSONObject()
        }
    }


    /**
     * 从JSONObject获取按频率排序的搜索建议列表
     *
     * @param searchSuggestionJSONObject 包含搜索词和频率的JSONObject
     * @return 按频率从高到低排序的搜索词列表
     */
    private fun getSearchSuggestionListFromJSONObject(
        searchSuggestionJSONObject: JSONObject,
    ): List<String> {

        val pairList = searchSuggestionJSONObject.keys().asSequence().map { key ->
            val frequency = searchSuggestionJSONObject.getInt(key)
            key to frequency
        }.toList()

        // 按频率从大到小排序
        val suggestionList = pairList.sortedByDescending { it.second }.map { it.first }

        if(suggestionList.size > SEARCH_SUGGESTION_MAX_COUNT_LIMIT) {
            // 取前maxCount个元素
            val finalList = suggestionList.take(SEARCH_SUGGESTION_MAX_COUNT_LIMIT)

            // 从JSONObject中移除多余的项
            val keysToRemove = suggestionList.drop(SEARCH_SUGGESTION_MAX_COUNT_LIMIT)
            for(key in keysToRemove) {
                searchSuggestionJSONObject.remove(key)
            }

            return finalList
        }

        return suggestionList
    }


    /**
     * 更新搜索建议，按频率排序并限制数量
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @param searchSuggestionJSONObject 搜索建议的 JSONObject
     * @param searchSuggestionExpandableGroup 搜索建议的 ExpandableGroup
     * @param newWord 被使用的搜索词
     * @return 更新后的搜索建议列表
     */
    private fun updateSearchSuggestion(
        viewModel: MyViewModel,
        configKey: String,
        searchSuggestionJSONObject: JSONObject,
        searchSuggestionExpandableGroup: ExpandableGroup,
        newWord: String,
    ) {

        // 获取当前频率，如果不存在则为0
        val currentFrequency = if(searchSuggestionJSONObject.has(newWord)) {
            searchSuggestionJSONObject.getInt(newWord)
        } else {
            0
        }

        // 增加频率并重新添加到 JSONObject
        searchSuggestionJSONObject.put(newWord, currentFrequency + 1)

        // 转换为列表并按频率排序
        val searchSuggestionList = getSearchSuggestionListFromJSONObject(
            searchSuggestionJSONObject = searchSuggestionJSONObject,
        )

        searchSuggestionExpandableGroup.dataList = searchSuggestionList

        // 更新配置
        viewModel.updateSearchSuggestionConfigValue(
            key = configKey,
            value = searchSuggestionJSONObject,
        )
//        LogHelper.e("JSONObject:${searchSuggestionJSONObject}", "List:${finalSuggestionList}")

        return
    }


    /**
     * 获取搜索建议的 ExpandableGroup
     *
     * @param viewModel MyViewModel 实例
     * @param searchSuggestionJSONObject 搜索建议的 JSONObject
     * @return 搜索建议的 ExpandableGroup
     */
    fun getSearchSuggestionExpandableGroup(
        viewModel: MyViewModel,
        searchSuggestionJSONObject: JSONObject,
    ): ExpandableGroup {
        val maxDisplayedCount = viewModel.getConfigValue(
            SUGGESTION_MAX_COUNT_KEY
        ) as? Int ?: SEARCH_SUGGESTION_MAX_COUNT_LIMIT
        return ExpandableGroup(
            titleString = viewModel.myContext.getString(R.string.search_suggestion),
            dataList = getSearchSuggestionListFromJSONObject(
                searchSuggestionJSONObject = searchSuggestionJSONObject,
            ),
            maxDisplayedCount = maxDisplayedCount,
        )
    }

    /**
     * 更新搜索历史和搜索建议
     *
     * @param viewModel MyViewModel 实例
     * @param searchHistoryConfigKey 搜索历史的配置键
     * @param searchHistoryExpandableGroup 搜索历史的 ExpandableGroup
     * @param searchSuggestionJSONObject 当前的搜索建议JSONObject
     * @param newWord 被使用的搜索词
     * @return 更新后的搜索建议列表
     */
    fun updateSearchHistoryAndSuggestion(
        viewModel: MyViewModel,
        searchHistoryConfigKey: String,
        searchHistoryExpandableGroup: ExpandableGroup,
        searchSuggestionConfigKey: String,
        searchSuggestionJSONObject: JSONObject,
        searchSuggestionExpandableGroup: ExpandableGroup,
        newWord: String,
    ) {
        updateSearchHistory(
            viewModel = viewModel,
            configKey = searchHistoryConfigKey,
            searchHistoryExpandableGroup = searchHistoryExpandableGroup,
            newWord = newWord,
        )
        viewModel.saveSearchHistoryConfig()

        updateSearchSuggestion(
            viewModel = viewModel,
            configKey = searchSuggestionConfigKey,
            searchSuggestionJSONObject = searchSuggestionJSONObject,
            searchSuggestionExpandableGroup = searchSuggestionExpandableGroup,
            newWord = newWord,
        )
        viewModel.saveSearchSuggestionConfig()
    }


    /**
     * 更新搜索历史搜索建议的最大显示数量
     *
     * @param viewModel MyViewModel 实例
     * @param historyExpandableGroup 搜索历史的 ExpandableGroup
     * @param suggestionExpandableGroup 搜索建议的 ExpandableGroup
     */
    fun updateSearchHistoryAndSuggestionMaxDisplayedCount(
        viewModel: MyViewModel,
        historyExpandableGroup: ExpandableGroup,
        suggestionExpandableGroup: ExpandableGroup,
    ) {
        val historyMaxCount = viewModel.getConfigValue(
            HISTORY_MAX_COUNT_KEY
        ) as? Int ?: SEARCH_HISTORY_MAX_COUNT_LIMIT
        historyExpandableGroup.maxDisplayedCount = historyMaxCount

        val suggestionMaxCount = viewModel.getConfigValue(
            SUGGESTION_MAX_COUNT_KEY
        ) as? Int ?: SEARCH_SUGGESTION_MAX_COUNT_LIMIT
        suggestionExpandableGroup.maxDisplayedCount = suggestionMaxCount
    }


    /**
     * 清空搜索历史
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @param searchHistoryExpandableGroup 搜索历史的 ExpandableGroup
     */
    fun clearSearchHistory(
        viewModel: MyViewModel,
        configKey: String,
        searchHistoryExpandableGroup: ExpandableGroup,
    ) {
        // 清空搜索历史列表
        searchHistoryExpandableGroup.dataList = emptyList()

        // 更新配置
        viewModel.deleteSearchHistoryConfigValue(configKey)
        viewModel.saveSearchHistoryConfig()
    }


    /**
     * 清空搜索建议
     *
     * @param viewModel MyViewModel 实例
     * @param configKey 配置键
     * @param searchSuggestionJSONObject 搜索建议的 JSONObject
     * @param searchSuggestionExpandableGroup 搜索建议的 ExpandableGroup
     */
    fun clearSearchSuggestion(
        viewModel: MyViewModel,
        configKey: String,
        searchSuggestionJSONObject: JSONObject,
        searchSuggestionExpandableGroup: ExpandableGroup,
    ) {
        // 收集所有键到一个列表中，避免在迭代时修改集合
        val keysToRemove = searchSuggestionJSONObject.keys().asSequence().toList()

        // 清空搜索建议 JSONObject
        for(key in keysToRemove) {
            searchSuggestionJSONObject.remove(key)
        }

        // 清空搜索建议列表
        searchSuggestionExpandableGroup.dataList = emptyList()

        // 更新配置
        viewModel.deleteSearchSuggestionConfigValue(configKey)
        viewModel.saveSearchSuggestionConfig()
    }


    /**
     * 清空搜索历史和搜索建议
     *
     * @param viewModel MyViewModel 实例
     * @param searchHistoryConfigKey 搜索历史的配置键
     * @param searchHistoryExpandableGroup 搜索历史的 ExpandableGroup
     * @param searchSuggestionConfigKey 搜索建议的配置键
     * @param searchSuggestionJSONObject 搜索建议的 JSONObject
     * @param searchSuggestionExpandableGroup 搜索建议的 ExpandableGroup
     * @param name 要清空的名称
     */
    fun clearSearchHistoryOrSuggestion(
        viewModel: MyViewModel,
        searchHistoryConfigKey: String,
        searchHistoryExpandableGroup: ExpandableGroup,
        searchSuggestionConfigKey: String,
        searchSuggestionJSONObject: JSONObject,
        searchSuggestionExpandableGroup: ExpandableGroup,
        name: String,
    ) {
        val context = viewModel.myContext
        when(name) {
            context.getString(R.string.search_history) -> {
                clearSearchHistory(
                    viewModel = viewModel,
                    configKey = searchHistoryConfigKey,
                    searchHistoryExpandableGroup = searchHistoryExpandableGroup,
                )
            }

            context.getString(R.string.search_suggestion) -> {
                clearSearchSuggestion(
                    viewModel = viewModel,
                    configKey = searchSuggestionConfigKey,
                    searchSuggestionJSONObject = searchSuggestionJSONObject,
                    searchSuggestionExpandableGroup = searchSuggestionExpandableGroup,
                )
            }
        }
    }

}
