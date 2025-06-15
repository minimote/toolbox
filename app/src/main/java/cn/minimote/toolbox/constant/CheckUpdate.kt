/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.constant

object CheckUpdate {
    //  检查更新的超时限制
    const val TIMEOUT = 5000

    object Frequency {
        const val DAILY = 1 * 24 * 60 * 60 * 1000L
        const val WEEKLY = 7 * 24 * 60 * 60 * 1000L
        const val MONTHLY = 30 * 24 * 60 * 60 * 1000L
        const val NEVER = -1L
    }

    // 下载文件后缀
    const val FILENAME_SUFFIX = ".apk"

    // 本地版本号后缀
    const val LOCAL_VERSION_SUFFIX = "-debug"
    // 版本号分隔符
    const val VERSION_NAME_SPLIT_CHAR = "."
    // 匹配版本号其他分隔符的正则表达式
    val VERSION_NAME_SEPARATOR_REGEX = "[\\s_-]".toRegex()

    // 轮询查询下载状态的间隔时间
    const val MONITOR_DOWNLOAD_STATUS_GAP_RIME = 1000L

    const val GITEE_API_URL = "https://gitee.com/api/v5/repos/{owner}/{repo}/releases/latest"
    const val GITEE_API_OWNER = "{owner}"
    const val GITEE_API_REPO = "{repo}"
    const val GITEE_API_REQUEST_METHOD = "GET"
    const val GITEE_API_RELEASE_NOTES_KEY = "body"
    const val GITEE_API_ASSETS_KEY = "assets"
    const val GITEE_API_FILENAME_KEY = "name"
    const val GITEE_API_DOWNLOAD_URL_KEY = "browser_download_url"
}