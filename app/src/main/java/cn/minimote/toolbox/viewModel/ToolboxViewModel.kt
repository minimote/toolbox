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
import android.os.Environment
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.constant.FragmentNames
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.dataClass.InstalledActivity
import cn.minimote.toolbox.dataClass.StoredActivity
import cn.minimote.toolbox.dataClass.ToolActivity
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper
import cn.minimote.toolbox.helper.IconCacheHelper
import cn.minimote.toolbox.helper.StoredActivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min


@HiltViewModel
class ToolboxViewModel
@Inject constructor(
    private val application: Application,
//    private val iconCacheManager: IconCacheManager,
) : AndroidViewModel(application) {

    // 获取上下文
    val myContext: Context get() = application.applicationContext


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


    // 判断设备是否为手表
    val isWatch: Boolean
        get() {
            val uiMode = myContext.resources.configuration.uiMode
            return uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH
        }


    // 保存路径
    val savePath: File = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        myContext.getString(R.string.app_name_en),
    ).apply {
        if(!exists()) {
            mkdirs()
        }
    }
    // 数据路径
    val dataPath: File = myContext.filesDir.apply {
        if(!exists()) {
            mkdirs()
        }
    }
    // 缓存路径
    val cachePath: File = myContext.cacheDir.apply {
        if(!exists()) {
            mkdirs()
        }
    }


    val iconCacheHelper: IconCacheHelper = IconCacheHelper(this)

    // 主视图小组件的列数
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
//    var recommendedActivitySize = 0

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


    // 上次更新检查时间
    val lastUpdateCheckTime: Long
        get() = ConfigHelper.getConfigValue(
            key = ConfigKeys.LAST_CHECK_UPDATE_TIME,
            viewModel = this,
        )?.toString()?.toLongOrNull() ?: 0L

    // 检查更新间隔
    val updateCheckGap: Long
        get() = CheckUpdateHelper.getUpdateCheckGapLong(
            ConfigHelper.getConfigValue(
                key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
                viewModel = this,
            ).toString()
        )

    // 下次更新检查时间
    val nextUpdateCheckTime: Long
        get() = lastUpdateCheckTime + updateCheckGap


    // 配置文件
    var defaultConfig: Map<String, Any> = Config.defaultConfig
    var userConfig: MutableMap<String, Any> = mutableMapOf()
    var userConfigBackup: MutableMap<String, Any> = mutableMapOf()

    // 设置已经被修改
    val settingWasModified: MutableLiveData<Boolean> = MutableLiveData()


    //    private val fragmentNames = FragmentNames
//    val configKeys = ConfigKeys


    // 默认就是组件列表
    private var _fragmentName = MutableLiveData(FragmentNames.WIDGET_LIST_FRAGMENT)
    val fragmentName: LiveData<String> = _fragmentName

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
        imageSize = (UI.FULL_SCREEN_IMAGE_RATE * screenShortSide).toInt()
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
    fun widgetListSizeWasModified() {
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
        val flag = modifiedWidget.value?.nickName != originWidget.value?.name
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

        val storageActivityList = StoredActivityHelper.loadStoredActivityList(this)
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
        return storageActivityList.associateByTo(LinkedHashMap()) { it.id }
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

        updateStorageActivityListSize()
        if(getFragmentName() == FragmentNames.WIDGET_LIST_FRAGMENT) {
            updateStorageActivityListOrder()
//            StoredActivityHelper.saveStoredActivityList(
//                this, _storedActivityList.value!!
//            )
            StoredActivityHelper.saveStoredActivityList(
                this, _storageActivityMap.values.toMutableList()
            )
        } else {
            updateWidget()
            StoredActivityHelper.saveStoredActivityList(
                this, _storageActivityMap.values.toMutableList()
            )
        }

    }


    // 更新小组件
    private fun updateWidget() {
        if(widgetWasModified.value != true) {
            return
        }
        val id = modifiedWidget.value?.id ?: return
        _storageActivityMap[id]?.update(modifiedWidget.value!!)
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
        for((id, _) in _storageActivityMap.toList()) {
            if(id !in _modifiedSizeStorageActivityMap) {
                // 该项被删除了，移除原字典的键值对和列表中的元素
                removeActivity(id)
//                Log.i("删除：$id", "${id in _activityNameToStorageActivityMap}")
            } else {
                // 该项不变，只移除新字典的键值对
                _modifiedSizeStorageActivityMap.remove(id)
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
//        val recommendedActivities = getRecommendedActivities()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfoList: List<ResolveInfo> =
            myPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
//        val resolveInfoList: List<ResolveInfo> = getAllExportedActivities()
//        Log.e("ToolboxViewModel", "获取活动列表：${resolveInfoList.size}")


        var installedActivities = resolveInfoList.map { resolveInfo ->
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
                name = appName,
                packageName = packageName,
                activityName = activityName,
            )
//            }

        }.sortedWith(compareBy(collator) { it.name })

//        installedActivities = (recommendedActivities + installedActivities).distinct()

        installedAppListSize = installedActivities.size
//        // 如果有子标题，则大小减 2
//        if(installedActivities[0].packageName.isEmpty()) {
//            installedAppListSize -= 2
//        }

        // 强加一个空白项，然后在 RecyclerView 底部加一个 padding
        // 暂时解决 RecyclerView 被搜索框挤下去的问题
        _installedActivityList.value = installedActivities //+ getEmptyInstalledActivity()

        // 获取活动名到应用信息的映射
        for(installedActivity in installedActivities) {
            val id = installedActivity.id
            if(!_installActivityMap.containsKey(id)) {
                _installActivityMap[id] = installedActivity
            }
        }
    }
    // 获取空白项
//    fun getEmptyInstalledActivity() = InstalledActivity(
//        appName = "",
//        packageName = "",
//        activityName = "",
//    )


//    // 获取推荐活动列表
//    private fun getRecommendedActivities(): List<InstalledActivity> {
////        val splitChar = context.getString(R.string.split_char)
//        var recommendedActivities = InstalledActivityHelper.loadInstalledActivityList(
//            context = myContext,
//            deviceType = DeviceTypes.ALL,
//        )
////        Log.e("recommendedActivities", "$recommendedActivities")
////        Log.e("ToolboxViewModel", "isWatch: ${isWatch()}")
//
//        recommendedActivities += if(isWatch()) {
//            // 手表活动
//            InstalledActivityHelper.loadInstalledActivityList(
//                context = myContext,
//                deviceType = DeviceTypes.WATCH,
//            )
//        } else {
//            // 手机活动
//            InstalledActivityHelper.loadInstalledActivityList(
//                context = myContext,
//                deviceType = DeviceTypes.PHONE,
//            )
//        }
//
//        // 过滤掉不存在的活动
//        recommendedActivities = recommendedActivities.filter { activity ->
//            try {
//                myPackageManager.getApplicationInfo(activity.packageName, 0)
//                true
//            } catch(_: PackageManager.NameNotFoundException) {
////                Log.e("不存在的活动", "$activity")
//                false
//            }
//        }.sortedWith(compareBy(collator) { it.appName }).toMutableList()
//
////        lastRecommendedActivityName = recommendedActivities.lastOrNull()?.appName ?: ""
////        recommendedActivitySize = recommendedActivities.size
//
//        if(recommendedActivities.isNotEmpty()) {
//            recommendedActivities = (mutableListOf(
//                InstalledActivity(
//                    appName = myContext.getString(R.string.recommended_activity),
//                    packageName = "",
//                    activityName = "",
//                ),
//            ) + recommendedActivities + mutableListOf(
//                InstalledActivity(
//                    appName = myContext.getString(R.string.other_activity),
//                    packageName = "",
//                    activityName = "",
//                ),
//            )).toMutableList()
//        }
//
//        return recommendedActivities
//    }


//    // 判断活动是否被存储
//    fun isStoredActivity(activityName: String): Boolean {
////        Log.i("ActivityViewModel", "${activityName},${_activityNameToStorageActivityMap.size}")
//        return activityName in _storageActivityMap
//    }


    // 判断活动是否被在修改大小的字典中
    fun inModifiedSizeMap(id: String): Boolean {
        return id in _modifiedSizeStorageActivityMap
    }


    // 删除或恢复组件
    fun deleteOrRestoreWidget(isDelete: Boolean, id: String) {
        if(isDelete) {
            removeFromModifiedMap(id)
        } else {
            addToModifiedMap(id)
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
        val id = installedActivity.id
        // 如果之前就有，重建映射；否则新建一个实例
        if(_storageActivityMap.containsKey(id)) {
            val storageActivity = _storageActivityMap[id]
            _modifiedSizeStorageActivityMap[id] = storageActivity!!
//            Log.e("原始集合", "${_storageActivityMap.keys.toSortedSet()}")
//            Log.e("修改集合", "${_modifiedSizeStorageActivityMap.keys.toSortedSet()}")


        } else {
//            Log.e("ViewModel", "新建活动：${installedActivity.appName}")
            val storedActivity = installedActivity.toStoredActivity(width = maxWidgetSize)
            _modifiedSizeStorageActivityMap[id] = storedActivity
        }
    }
    // 将工具活动添加到修改的字典
    fun addToModifiedMap(toolActivity: ToolActivity) {
        val storedActivity = toolActivity.toStoredActivity(width = maxWidgetSize)
        _modifiedSizeStorageActivityMap[storedActivity.id] = storedActivity
    }

    // 组件编辑页的恢复按钮
    private fun addToModifiedMap(id: String) {
        // Log.e(
        //     "添加到修改字典",
        //     "${_storageActivityMap.containsKey(activityName)},${modifiedWidget.value?.appName}"
        // )
        if(_storageActivityMap.containsKey(id)) {
            _modifiedSizeStorageActivityMap = builtMapFromMap(_storageActivityMap)
        } else {
            _modifiedSizeStorageActivityMap[id] = modifiedWidget.value!!
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
        _modifiedSizeStorageActivityMap.remove(installedActivity.id)
    }
    // 将工具从修改的字典中移除
    fun removeFromModifiedMap(toolActivity: ToolActivity) {
//        Log.e("ViewModel", "移除：${installedActivity.appName}")
        _modifiedSizeStorageActivityMap.remove(toolActivity.id)
    }
    // 组件编辑页的删除按钮
    private fun removeFromModifiedMap(id: String) {
        _modifiedSizeStorageActivityMap.remove(id)
    }


    private fun addActivity(storedActivity: StoredActivity) {
        _storedActivityList.value?.add(storedActivity)
        _storageActivityMap[storedActivity.id] = storedActivity
    }


    private fun removeActivity(id: String) {
        val appInfo = _storageActivityMap[id] ?: return
        _storedActivityList.value?.remove(appInfo)
        _storageActivityMap.remove(appInfo.id)
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
