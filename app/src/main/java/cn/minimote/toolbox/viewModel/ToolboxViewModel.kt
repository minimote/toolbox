/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.minimote.toolbox.R
import cn.minimote.toolbox.dataClass.InstalledActivity
import cn.minimote.toolbox.dataClass.StoredActivity
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.helper.IconCacheHelper
import cn.minimote.toolbox.helper.InstalledActivityHelper
import cn.minimote.toolbox.helper.StoredActivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.Collator
import java.util.Locale
import java.util.TreeMap
import javax.inject.Inject
import kotlin.math.min


@HiltViewModel
class ToolboxViewModel
@Inject constructor(
    application: Application,
//    private val iconCacheManager: IconCacheManager,
) : AndroidViewModel(application) {

    // 获取上下文
    val myContext: Context get() = getApplication<Application>().applicationContext


    // 获取 PackageManager
    private val myPackageManager: PackageManager = myContext.packageManager


    // 获取应用名
    val myAppName: String = myContext.applicationInfo.loadLabel(myPackageManager).toString()


    // 获取包名
    val myPackageName: String = myContext.packageName


    // 获取版本号
    val myVersionName: String = myPackageManager.getPackageInfo(
        myContext.packageName, 0
    ).versionName ?: getString(myContext, R.string.unknown)


    // 获取作者名
    val myAuthorName: String = getString(myContext, R.string.author_name)

    private val iconCacheHelper: IconCacheHelper = IconCacheHelper(myContext)

    // 主视图小组件的列数(1-6的最小公倍数)
//    val spanCount = 60
    val spanCount = 12
    // 最小最大组件大小
    val minWidgetSize = 1
    val maxWidgetSize = spanCount
    // 屏幕尺寸
    var screenWidth = 0
    var screenHeight = 0
    // 屏幕短边
    private var screenShortSide = 0
    var imageSize = 0

    // 最后一个推荐活动的名称(用于画分隔线)
//    var lastRecommendedActivityName = ""
    var recommendedActivitySize = 0

    // 活动列表大小
    var installedAppListSize = 0

    private val _storedActivityList = MutableLiveData<MutableList<StoredActivity>>()
    val storedActivityList: LiveData<MutableList<StoredActivity>> = _storedActivityList
//    // 原始活动列表(主要用于保存顺序)
//    private val _originStoredActivityList = MutableLiveData<MutableList<StoredActivity>>()

    private val _installedActivityList = MutableLiveData<List<InstalledActivity>>()
    val installedActivityList: LiveData<List<InstalledActivity>> = _installedActivityList

    // 用 LinkedHashMap 保持插入顺序
    private var _storageActivityMap = LinkedHashMap<String, StoredActivity>()
    // 用来记录组件的增减
    private var _modifiedSizeStorageActivityMap = LinkedHashMap<String, StoredActivity>()
    // 用来记录组件的顺序变化
    private var _modifiedOrderStorageActivityMap = LinkedHashMap<String, StoredActivity>()

    private val _installActivityMap = mutableMapOf<String, InstalledActivity>()

    // 修改组件
    var originWidget = MutableLiveData<StoredActivity>()
    var modifiedWidget = MutableLiveData<StoredActivity>()

    // 搜索词(用于高亮显示结果)
    val searchQuery = MutableLiveData("")

    // 中文排序器
    private val collator: Collator = Collator.getInstance(Locale.CHINA)

    // WebView 网址
    var webViewUrl = ""

    companion object {

        // 视图类型
        object ViewTypes {
            // 编辑视图类型
            object Edit {
                const val PACKAGE_NAME = 0
                const val ACTIVITY_NAME = 1
                const val NICKNAME = 2
                const val SHOW_NAME = 3
                const val SIZE = 4
                const val DELETE = 5
            }

            // 我的视图类型
            object My {
                const val APP_INFO = 0
                const val CLEAR_CACHE = 1
                const val CLEAR_DATA = 2
                const val SUPPORT_AUTHOR = 3
                const val ABOUT_PROJECT = 4
                const val CHECK_UPDATE = 5
                const val SETTING = 6
                const val INSTRUCTION = 7
                const val UPDATE_LOG = 8
                const val PROBLEM_FEEDBACK = 9
            }

            // 支持作者视图类型
            object SupportAuthor {
                const val WELCOME = 0
                const val NOTICE = 1
                const val QR_ALIPAY = 2
                const val OPERATE_ALIPAY = 3
                const val QR_WECHAT = 4
                const val OPERATE_WECHAT = 5
            }

            // 关于项目视图类型
            object AboutProject {
                const val NOTICE = 0
                const val PROJECT_PATH_NAME = 1
                const val PROJECT_PATH_GITEE = 2
                const val PROJECT_PATH_GITHUB = 3
            }

            // 设置视图类型
            object Setting {
                const val TITLE_CHECK_UPDATE = 0
                const val UPDATE_CHECK_FREQUENCY = 1
                const val TITLE_VIBRATION = 2
                const val VIBRATION_MODE = 3
            }
        }

        // Fragment 名称
        object FragmentNames {
            const val NO_FRAGMENT = "NoFragment"
            const val WIDGET_LIST_FRAGMENT = "WidgetListFragment"
            const val EDIT_LIST_FRAGMENT = "EditListFragment"
            const val ACTIVITY_LIST_FRAGMENT = "ActivityListFragment"
            const val SUPPORT_AUTHOR_FRAGMENT = "SupportAuthorFragment"
            const val ABOUT_PROJECT_FRAGMENT = "AboutProjectFragment"
            const val MY_LIST_FRAGMENT = "MyListFragment"
            const val SETTING_FRAGMENT = "SettingFragment"
            const val WEB_VIEW_FRAGMENT = "WebViewFragment"
        }

        // 设备类型
        object DeviceTypes {
            const val ALL = "all"
            const val PHONE = "phone"
            const val WATCH = "watch"
        }

        // 配置的键
        object ConfigKeys {
            // 上次检查更新时间
            const val LAST_CHECK_UPDATE_TIME = "last_check_update_time"
            // 振动模式
            const val VIBRATION_MODE = "vibration_mode"
            // 更新检查频率
            const val CHECK_UPDATE_FREQUENCY = "check_update_frequency"
        }

        // 配置的值
        object ConfigValues {
            // 振动模式：自动、启用、关闭
            object VibrationMode {
                const val AUTO = "auto"
                const val ON = "on"
                const val OFF = "off"
            }

            // 更新检查频率：每天、每周、每月、从不
            object CheckUpdateFrequency {
                const val DAILY = "daily"
                const val WEEKLY = "weekly"
                const val MONTHLY = "monthly"
                const val NEVER = "never"
            }
        }

    }

    // 更新检查频率列表
    val checkUpdateFrequencyList = listOf(
        ConfigValues.CheckUpdateFrequency.DAILY,
        ConfigValues.CheckUpdateFrequency.WEEKLY,
        ConfigValues.CheckUpdateFrequency.MONTHLY,
        ConfigValues.CheckUpdateFrequency.NEVER,
    )

    // 振动模式列表
    val vibrationModeList = listOf(
        ConfigValues.VibrationMode.ON,
        ConfigValues.VibrationMode.AUTO,
        ConfigValues.VibrationMode.OFF,
    )

    // viewPaper 的 Fragment 列表
    val viewPaperFragmentList = listOf(
        FragmentNames.WIDGET_LIST_FRAGMENT,
        FragmentNames.MY_LIST_FRAGMENT,
    )
    // viewPaper 的 Fragment 名称列表
    val viewPaperFragmentNameList = listOf(
        myContext.getString(R.string.fragment_name_widget_list),
        myContext.getString(R.string.fragment_name_my_list),
    )


    // 编辑列表
//    val editViewTypes = EditViewTypes
    val editList = listOf(
        ViewTypes.Edit.PACKAGE_NAME, // 包名
        ViewTypes.Edit.ACTIVITY_NAME, // 活动名
        ViewTypes.Edit.NICKNAME, // 显示的名称
        ViewTypes.Edit.SHOW_NAME, // 是否显示名称
        ViewTypes.Edit.SIZE, // 组件大小
        ViewTypes.Edit.DELETE, // 删除组件
    )

    // 我的列表
//    private val myViewTypes = MyViewTypes
    val myList = listOf(
        ViewTypes.My.APP_INFO, // 应用信息
        ViewTypes.My.CLEAR_CACHE, // 清理缓存
        ViewTypes.My.CLEAR_DATA, // 清除数据
        ViewTypes.My.SETTING, // 设置
        ViewTypes.My.SUPPORT_AUTHOR, // 支持作者
        ViewTypes.My.ABOUT_PROJECT, // 关于项目
        ViewTypes.My.INSTRUCTION, // 使用说明
        ViewTypes.My.UPDATE_LOG, // 更新日志,
        ViewTypes.My.PROBLEM_FEEDBACK, // 问题反馈
        ViewTypes.My.CHECK_UPDATE, // 检查更新
    )

    // 支持作者
//    private val supportAuthorViewTypes = SupportAuthorViewTypes
    val supportAuthorViewList = listOf(
        ViewTypes.SupportAuthor.WELCOME, // 欢迎
        ViewTypes.SupportAuthor.NOTICE, // 说明
        ViewTypes.SupportAuthor.QR_ALIPAY, // 支付宝收款码
        ViewTypes.SupportAuthor.OPERATE_ALIPAY, // 支付宝相关操作
        ViewTypes.SupportAuthor.QR_WECHAT, // 微信收款码
        ViewTypes.SupportAuthor.OPERATE_WECHAT, // 微信相关操作
    )

    // 关于项目
    val aboutProjectViewList = listOf(
        ViewTypes.AboutProject.NOTICE, // 说明
        ViewTypes.AboutProject.PROJECT_PATH_NAME, // 项目地址
        ViewTypes.AboutProject.PROJECT_PATH_GITEE, // Gitee 地址
        ViewTypes.AboutProject.PROJECT_PATH_GITHUB, // Github 地址
    )

    // 设置
    val settingViewList = listOf(
        ViewTypes.Setting.TITLE_CHECK_UPDATE, // 更新设置的标题
        ViewTypes.Setting.UPDATE_CHECK_FREQUENCY, // 更新检查频率
        ViewTypes.Setting.TITLE_VIBRATION, // 振动设置的标题
        ViewTypes.Setting.VIBRATION_MODE, // 振动模式
    )


    // 上次更新检查时间
    val lastUpdateCheckTime: Long
        get() = ConfigHelper.getConfigValue(
            key = ConfigKeys.LAST_CHECK_UPDATE_TIME,
            viewModel = this,
        )?.toString()?.toLongOrNull() ?: 0L

    // 检查更新间隔
    val updateCheckGap: Long
        get() = CheckUpdateHelper.getUpdateCheckGap(
            ConfigHelper.getConfigValue(
                key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                viewModel = this,
            ).toString()
        )

    // 下次更新检查时间
    val nextUpdateCheckTime: Long
        get() = lastUpdateCheckTime + updateCheckGap


    // 配置文件
    var defaultConfig: TreeMap<String, Any> =
        ConfigHelper.loadConfig(context = myContext, userConfigFlag = false)
    var userConfig: TreeMap<String, Any> =
        ConfigHelper.loadConfig(context = myContext, userConfigFlag = true)
    var userConfigBackup: TreeMap<String, Any> = TreeMap(userConfig)

    // 设置已经被修改
    val settingWasModified: MutableLiveData<Boolean> = MutableLiveData()


    //    private val fragmentNames = FragmentNames
//    val configKeys = ConfigKeys


    // 默认就是组件列表
    private var _fragmentName = MutableLiveData(FragmentNames.WIDGET_LIST_FRAGMENT)
    val fragmentName: LiveData<String> = _fragmentName

    // 保存路径
//    val savePath = File(
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//        myContext.getString(R.string.app_name_en),
//    )
    val savePath = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        myContext.getString(R.string.app_name_en),
    ).apply {
        if(!exists()) {
            mkdirs()
        }
    }

    // 首页拖动场景使用
    var widgetListOrderWasModified: MutableLiveData<Boolean> = MutableLiveData()
    // 首页排序使用
    var widgetListWasSorted: MutableLiveData<Boolean> = MutableLiveData()
    // 编辑列表和活动列表共用
    var widgetListSizeWasModified: MutableLiveData<Boolean> = MutableLiveData()
    // 编辑列表使用
    var widgetWasModified: MutableLiveData<Boolean> = MutableLiveData()
    var widgetNameWasModified: MutableLiveData<Boolean> = MutableLiveData()
    private var widgetShowNameWasModified: MutableLiveData<Boolean> = MutableLiveData()
    var widgetSizeWasModified: MutableLiveData<Boolean> = MutableLiveData()


    var editMode: MutableLiveData<Boolean> = MutableLiveData(false)
    var searchMode: MutableLiveData<Boolean> = MutableLiveData()


    // 判断设备是否为手表
    fun isWatch(): Boolean {
        val uiMode = myContext.resources.configuration.uiMode
        return uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH
    }

    // 将 dp 转换为 px
    fun dpToPx(dp: Float): Int {
        val density = myContext.resources.displayMetrics.density
        return (dp * density).toInt()
    }


    fun updateFragmentName(fragmentName: String?) {
//        Log.e("updateFragmentName", fragmentName.toString())
        _fragmentName.value = fragmentName
    }


    fun getFragmentName(): String {
        return _fragmentName.value ?: FragmentNames.NO_FRAGMENT
    }


    // 更新屏幕尺寸
    fun updateScreenSize(displayMetrics: DisplayMetrics) {
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        // 屏幕短边
        screenShortSide = min(screenWidth, screenHeight)
        imageSize = (0.8 * screenShortSide).toInt()
    }


    // 小组件列表顺序是否发生改变
    private fun widgetListOrderWasModified() {
        val flag = _storageActivityMap.entries.zip(
            _modifiedOrderStorageActivityMap.entries
        ).any { (a, b) -> a != b }

        if(widgetListOrderWasModified.value != flag) {
            widgetListOrderWasModified.value = flag
        }
    }


    // 小组件列表大小是否发生改变(实际还要看键值对是否改变)
    private fun widgetListSizeWasModified() {
        val flag =
            _storageActivityMap.size != _modifiedSizeStorageActivityMap.size || _storageActivityMap.entries.zip(
                _modifiedSizeStorageActivityMap.entries
            ).any { (a, b) -> a != b }
//        Log.e("大小是否发生改变", "${widgetListSizeWasModified.value}->${flag}")
        if(widgetListSizeWasModified.value != flag) {
            widgetListSizeWasModified.value = flag
            // Log.e("大小发生改变", flag.toString())
        } else {
            // Log.e("大小未发生改变", flag.toString())
        }
    }


    // 小组件是否发生改变
    fun widgetWasModified() {
        val flag = modifiedWidget.value != originWidget.value
        if(widgetWasModified.value != flag) {
            widgetWasModified.value = flag
        }
        widgetNameWasModified()
        widgetShowNameWasModified()
        widgetSizeWasModified()
    }


    // 小组件名称是否发生改变
    private fun widgetNameWasModified() {
        val flag = modifiedWidget.value?.nickName != originWidget.value?.appName
        if(widgetNameWasModified.value != flag) {
            widgetNameWasModified.value = flag
        }
    }


    // 小组件显示名称是否发生改变
    private fun widgetShowNameWasModified() {
        val flag = modifiedWidget.value?.showName != originWidget.value?.showName
        if(widgetShowNameWasModified.value != flag) {
            widgetShowNameWasModified.value = flag
        }
    }


    // 小组件大小是否发生改变
    private fun widgetSizeWasModified() {
        val flag = modifiedWidget.value?.width != originWidget.value?.width
        if(widgetSizeWasModified.value != flag) {
            widgetSizeWasModified.value = flag
        }
    }


    // 加载存储的活动信息
    fun loadStorageActivities() {
//        Log.i("ActivityViewModel", "加载活动0")
//        if(_storedActivityList.value != null) {
//            return
//        }

        val storageActivityList = StoredActivityHelper.loadStoredActivityList(myContext)
        _storedActivityList.value = storageActivityList

//        Log.i("ActivityViewModel", "加载活动1：${_storedActivityList.value?.size}")
        // 建立活动名到应用信息的映射
        _storageActivityMap = builtMapFromList(storageActivityList)
//        Log.i("ActivityViewModel", "加载活动2：${_activityNameToStorageActivityMap.size}")

        // 建立用于修改的映射
        builtModifiedMap()
    }


    // 通过列表建立活动名到应用信息的映射
    private fun builtMapFromList(storageActivityList: MutableList<StoredActivity>): LinkedHashMap<String, StoredActivity> {
        return storageActivityList.associateByTo(LinkedHashMap()) { it.activityName }
    }
    // 映射的复制
    private fun builtMapFromMap(map: LinkedHashMap<String, StoredActivity>): LinkedHashMap<String, StoredActivity> {
        return map.toMutableMap() as LinkedHashMap<String, StoredActivity>
    }


    // 建立用于修改的映射
    private fun builtModifiedMap() {
        builtModifiedSizeMap()
        builtModifiedOrderMap()
    }
    // 建立用于修改列表大小的映射
    private fun builtModifiedSizeMap() {
        _modifiedSizeStorageActivityMap = builtMapFromMap(_storageActivityMap)
    }
    // 建立用于修改顺序的映射
    private fun builtModifiedOrderMap() {
        _modifiedOrderStorageActivityMap = builtMapFromMap(_storageActivityMap)
    }


    // 修改字典顺序
    fun modifyStoredActivityListOrder() {
        _modifiedOrderStorageActivityMap = builtMapFromList(_storedActivityList.value!!)
        widgetListOrderWasModified()
    }
    // 恢复原始活动列表顺序
    fun restoreOriginStoredActivityList() {
        _storedActivityList.value = _storageActivityMap.values.toMutableList()
        builtModifiedOrderMap()
    }


    // 保存组件列表到存储中
    fun saveWidgetList() {
//        Log.i("saveStorageActivities", "保存活动列表到存储中")

        if(getFragmentName() == FragmentNames.WIDGET_LIST_FRAGMENT) {
            updateStorageActivityListOrder()
            StoredActivityHelper.saveStoredActivityList(myContext, _storedActivityList.value!!)
        } else {
            updateWidget()
            updateStorageActivityListSize()
            StoredActivityHelper.saveStoredActivityList(
                myContext, _storageActivityMap.values.toMutableList()
            )
        }

    }


    // 更新小组件
    private fun updateWidget() {
        if(widgetWasModified.value != true) {
            return
        }
        val activityName = modifiedWidget.value?.activityName ?: return
        _storageActivityMap[activityName]?.update(modifiedWidget.value!!)
        originWidget.value?.update(modifiedWidget.value!!)
        widgetWasModified()
    }


    // 更新要存储的活动列表大小
    private fun updateStorageActivityListSize() {
//        Log.i(
//            "准备更新要存储的活动列表",
//            "${_storedActivityList.value?.size},${_activityNameToStorageActivityMap.size},${_modifiedActivityNameToStorageActivityMap.size}"
//        )
        if(widgetListSizeWasModified.value != true) {
            return
        }
        // 遍历转化的链表，避免边遍历边修改的问题
        for((activityName, _) in _storageActivityMap.toList()) {
            if(activityName !in _modifiedSizeStorageActivityMap) {
                // 该项被删除了，移除原字典的键值对和列表中的元素
                removeActivity(activityName)
//                Log.i("删除：$activityName", "${activityName in _activityNameToStorageActivityMap}")
            } else {
                // 该项不变，只移除新字典的键值对
                _modifiedSizeStorageActivityMap.remove(activityName)
            }
        }
        // 剩下的都是新增的
        for(storageActivity in _modifiedSizeStorageActivityMap.values) {
            addActivity(storageActivity)
        }

        _modifiedOrderStorageActivityMap = builtMapFromList(_storedActivityList.value!!)
        _modifiedSizeStorageActivityMap = builtMapFromMap(_storageActivityMap)
        widgetListSizeWasModified()
        widgetListOrderWasModified()
    }


    // 更新要存储的活动列表顺序
    private fun updateStorageActivityListOrder() {
        if(widgetListOrderWasModified.value != true) {
            return
        }
        _storageActivityMap = builtMapFromList(_storedActivityList.value!!)

        builtModifiedSizeMap()
        widgetListOrderWasModified()
    }


    // 给 _storedActivityList 排序
    fun sortStoredActivityList() {
//        Log.e("排序", "开始排序")
        _storedActivityList.value?.let { activities ->
//            Log.e("排序", "进入排序")
            _storedActivityList.value =
                activities.sortedWith(compareBy(collator) { activity -> activity.nickName })
                    .toMutableList()
            _modifiedOrderStorageActivityMap = builtMapFromList(_storedActivityList.value!!)
            modifyStoredActivityListOrder()
            widgetListWasSorted.value = true
        }
    }


    // 获取可启动的活动列表(协程)
    fun getInstalledActivitiesCoroutine() {
        viewModelScope.launch {
            getInstalledActivities()
            // 设置修改字典，用于比较是否产生修改
            builtModifiedMap()
        }
    }


    // 获取可启动的活动列表
    private fun getInstalledActivities() {
        if(_installedActivityList.value != null) {
            return
        }

        // 推荐工具
        val recommendedActivities = getRecommendedActivities()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfoList: List<ResolveInfo> =
            myPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
//        val resolveInfoList: List<ResolveInfo> = getAllExportedActivities()
//        Log.e("ToolboxViewModel", "获取活动列表：${resolveInfoList.size}")


        var installedActivities = resolveInfoList.mapNotNull { resolveInfo ->
            val applicationInfo = resolveInfo.activityInfo.applicationInfo
            // 应用名
//            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            // 活动名
            val appName = resolveInfo.activityInfo.loadLabel(myPackageManager).toString()
            val packageName = applicationInfo.packageName
            val activityName = resolveInfo.activityInfo.name
//            if(packageName == myPackageName) {
//                // 不能启动自己，防止出错
////                Log.e(myPackageName, "获取包名：$packageName")
//                null
//            } else {
            InstalledActivity(
                appName = appName,
                packageName = packageName,
                activityName = activityName,
            )
//            }

        }.sortedWith(compareBy(collator) { it.appName })

        installedActivities = (recommendedActivities + installedActivities).distinct()

        installedAppListSize = installedActivities.size
        // 如果有子标题，则大小减 2
        if(installedActivities[0].packageName.isEmpty()) {
            installedAppListSize -= 2
        }

        // 强加一个空白项，然后在 RecyclerView 底部加一个 padding
        // 暂时解决 RecyclerView 被搜索框挤下去的问题
        _installedActivityList.value = installedActivities //+ getEmptyInstalledActivity()

        // 获取活动名到应用信息的映射
        for(installedActivity in installedActivities) {
            val activityName = installedActivity.activityName
            if(!_installActivityMap.containsKey(activityName)) {
                _installActivityMap[activityName] = installedActivity
            }
        }
    }
    // 获取空白项
//    fun getEmptyInstalledActivity() = InstalledActivity(
//        appName = "",
//        packageName = "",
//        activityName = "",
//    )


    // 获取推荐活动列表
    private fun getRecommendedActivities(): List<InstalledActivity> {
//        val splitChar = context.getString(R.string.split_char)
        var recommendedActivities = InstalledActivityHelper.loadInstalledActivityList(
            context = myContext,
            deviceType = DeviceTypes.ALL,
        )
//        Log.e("recommendedActivities", "$recommendedActivities")
//        Log.e("ToolboxViewModel", "isWatch: ${isWatch()}")

        recommendedActivities += if(isWatch()) {
            // 手表活动
            InstalledActivityHelper.loadInstalledActivityList(
                context = myContext,
                deviceType = DeviceTypes.WATCH,
            )
        } else {
            // 手机活动
            InstalledActivityHelper.loadInstalledActivityList(
                context = myContext,
                deviceType = DeviceTypes.PHONE,
            )
        }

        // 过滤掉不存在的活动
        recommendedActivities = recommendedActivities.filter { activity ->
            try {
                myPackageManager.getApplicationInfo(activity.packageName, 0)
                true
            } catch(e: PackageManager.NameNotFoundException) {
//                Log.e("不存在的活动", "$activity")
                false
            }
        }.sortedWith(compareBy(collator) { it.appName }).toMutableList()

//        lastRecommendedActivityName = recommendedActivities.lastOrNull()?.appName ?: ""
//        recommendedActivitySize = recommendedActivities.size

        if(recommendedActivities.isNotEmpty()) {
            recommendedActivities = (mutableListOf(
                InstalledActivity(
                    appName = myContext.getString(R.string.recommended_activity),
                    packageName = "",
                    activityName = "",
                ),
            ) + recommendedActivities + mutableListOf(
                InstalledActivity(
                    appName = myContext.getString(R.string.other_activity),
                    packageName = "",
                    activityName = "",
                ),
            )).toMutableList()
        }

        return recommendedActivities
    }


    // 获取图标
    fun getIcon(installedActivity: InstalledActivity): Drawable? {
        return iconCacheHelper.getIcon(
            iconName = installedActivity.iconName,
            packageName = installedActivity.packageName,
            activityName = installedActivity.activityName,
        )
    }
    // 获取图标
    fun getIcon(storedActivity: StoredActivity): Drawable? {
        return iconCacheHelper.getIcon(
            iconName = storedActivity.iconName,
            packageName = storedActivity.packageName,
            activityName = storedActivity.activityName,
        )
    }


//    // 判断活动是否被存储
//    fun isStoredActivity(activityName: String): Boolean {
////        Log.i("ActivityViewModel", "${activityName},${_activityNameToStorageActivityMap.size}")
//        return activityName in _storageActivityMap
//    }


    // 判断活动是否被在修改大小的字典中
    fun inModifiedSizeMap(activityName: String): Boolean {
//        Log.i("ActivityViewModel", "${activityName},${_activityNameToStorageActivityMap.size}")
        return activityName in _modifiedSizeStorageActivityMap
    }


    // 删除或恢复组件
    fun deleteOrRestoreWidget(isDelete: Boolean, activityName: String) {
        if(isDelete) {
            removeFromModifiedMap(activityName)
        } else {
            addToModifiedMap(activityName)
        }
        widgetListSizeWasModified()
    }


    // 切换活动列表开关状态
    fun toggleSwitch(isChecked: Boolean, installedActivity: InstalledActivity) {
        if(isChecked) {
            addToModifiedMap(installedActivity)
        } else {
            removeFromModifiedMap(installedActivity)
        }
        // 如果修改后的关键字集合与原来的一样，直接重建映射
        if(_storageActivityMap.keys.toSet() == _modifiedSizeStorageActivityMap.keys.toSet()) {
//            Log.e("ViewModel", "重建映射")
            _modifiedSizeStorageActivityMap = builtMapFromMap(_storageActivityMap)
        }
        widgetListSizeWasModified()
        //        widgetListOrderWasModified()
    }


    // 将活动添加到修改的字典
    private fun addToModifiedMap(installedActivity: InstalledActivity) {
//        Log.e("ViewModel", "添加：${installedActivity.appName}")
        // 如果之前就有，重建映射；否则新建一个实例
        if(_storageActivityMap.containsKey(installedActivity.activityName)) {
            val storageActivity = _storageActivityMap[installedActivity.activityName]
            _modifiedSizeStorageActivityMap[installedActivity.activityName] = storageActivity!!
//            Log.e("原始集合", "${_storageActivityMap.keys.toSortedSet()}")
//            Log.e("修改集合", "${_modifiedSizeStorageActivityMap.keys.toSortedSet()}")


        } else {
//            Log.e("ViewModel", "新建活动：${installedActivity.appName}")
            val storageActivity = installedActivityToStorageActivity(
                installedActivity = installedActivity,
            )
            _modifiedSizeStorageActivityMap[installedActivity.activityName] = storageActivity
        }
    }
    // 组件编辑页的恢复按钮
    private fun addToModifiedMap(activityName: String) {
        // Log.e(
        //     "添加到修改字典",
        //     "${_storageActivityMap.containsKey(activityName)},${modifiedWidget.value?.appName}"
        // )
        if(_storageActivityMap.containsKey(activityName)) {
            _modifiedSizeStorageActivityMap = builtMapFromMap(_storageActivityMap)
        } else {
            _modifiedSizeStorageActivityMap[activityName] = modifiedWidget.value!!
        }
//        } else {
////            Log.i("ViewModel(addToModifiedMap)", "没有找到活动：$activityName")
//            // 保存删除操作后再恢复
//
//        }
    }


    // 将活动从修改的字典中移除
    private fun removeFromModifiedMap(installedActivity: InstalledActivity) {
//        Log.e("ViewModel", "移除：${installedActivity.appName}")
        _modifiedSizeStorageActivityMap.remove(installedActivity.activityName)
    }
    // 组件编辑页的删除按钮
    private fun removeFromModifiedMap(activityName: String) {
        // Log.e("从修改字典中移除", activityName)
        _modifiedSizeStorageActivityMap.remove(activityName)
    }


    private fun addActivity(storedActivity: StoredActivity) {
//        if(storedActivity.activityName in _activityNameToStorageActivityMap) {
//            return
//        }
        _storedActivityList.value?.add(storedActivity)
        _storageActivityMap[storedActivity.activityName] = storedActivity
    }


    private fun removeActivity(activityName: String) {
        val appInfo = _storageActivityMap[activityName] ?: return
        // Log.i("删除${activityName}", "1")
        _storedActivityList.value?.remove(appInfo)
        _storageActivityMap.remove(appInfo.activityName)
    }


    // 将安装类型的活动转换为存储类型
    private fun installedActivityToStorageActivity(
        installedActivity: InstalledActivity,
        widgetSize: Int = maxWidgetSize,
    ): StoredActivity {
        val storageActivity = StoredActivity(
            appName = installedActivity.appName,
            packageName = installedActivity.packageName,
            activityName = installedActivity.activityName,
            width = widgetSize,
            iconName = installedActivity.iconName,
        )
        return storageActivity
    }


//    // 获取该 package 下所有导出的 Activity
//    private fun getAllExportedActivities(
//        packageName: String
//    ): List<ActivityInfo> {
//        return packageManager.getPackageInfo(
//            packageName, PackageManager.GET_ACTIVITIES
//        ).activities?.filter { it.exported } ?: emptyList() // 过滤出被导出的Activity
//    }

}
