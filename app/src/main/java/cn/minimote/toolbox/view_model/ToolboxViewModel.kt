/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.view_model

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.minimote.toolbox.R
import cn.minimote.toolbox.data_class.InstalledActivity
import cn.minimote.toolbox.data_class.StoredActivity
import cn.minimote.toolbox.objects.ConfigHelper
import cn.minimote.toolbox.objects.ConfigHelper.ConfigName
import cn.minimote.toolbox.objects.InstalledActivityHelper
import cn.minimote.toolbox.objects.StoredActivityHelper
import cn.minimote.toolbox.others.IconCacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class ToolboxViewModel
@Inject constructor(
    application: Application,
//    private val iconCacheManager: IconCacheManager,
) : AndroidViewModel(application) {

    private val iconCacheManager: IconCacheManager = IconCacheManager(context)

    // 主视图小组件的列数(1-6的最小公倍数)
//    val spanCount = 60
    val spanCount = 12
    // 最小最大组件大小
    val minWidgetSize = 1
    val maxWidgetSize = spanCount
    // 屏幕尺寸
    var screenWidth = 0
    var screenHeight = 0

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

    // 配置文件
    var defaultConfig = ConfigHelper.loadConfig(
        context = context,
        configName = ConfigName.DEFAULT_CONFIG,
    )
    var userConfig = ConfigHelper.loadConfig(
        context = context,
        configName = ConfigName.USER_CONFIG,
    )

    // 修改组件
    var originWidget = MutableLiveData<StoredActivity>()
    var modifiedWidget = MutableLiveData<StoredActivity>()

    // 中文排序器
    private val collator: Collator = Collator.getInstance(Locale.CHINA)

    companion object Constants {
        // 编辑视图类型
        object EditViewTypes {
            const val EDIT_VIEW_TYPE_PACKAGE_NAME = 0
            const val EDIT_VIEW_TYPE_ACTIVITY_NAME = 1
            const val EDIT_VIEW_TYPE_NICKNAME = 2
            const val EDIT_VIEW_TYPE_SHOW_NAME = 3
            const val EDIT_VIEW_TYPE_SIZE = 4
            const val EDIT_VIEW_TYPE_DELETE = 5
        }

        // Fragment 名称
        object FragmentNames {
            const val WIDGET_LIST_FRAGMENT = "WidgetListFragment"
            const val EDIT_LIST_FRAGMENT = "EditListFragment"
            const val ACTIVITY_LIST_FRAGMENT = "ActivityListFragment"
        }

        // 设备类型
        object DeviceType {
            const val ALL = "all"
            const val PHONE = "phone"
            const val WATCH = "watch"
        }

        // 配置的键
        object ConfigKeys {
            const val VIBRATION_MODE = "vibration_mode"
        }

        // 配置的值
        object ConfigValues {
            const val VIBRATION_MODE_ON = "on"
            const val VIBRATION_MODE_OFF = "off"
            const val VIBRATION_MODE_AUTO = "auto"
        }
    }

    val editViewTypes = EditViewTypes
    val fragmentNames = FragmentNames
    val configKeys = ConfigKeys

    val editList = listOf(
        editViewTypes.EDIT_VIEW_TYPE_PACKAGE_NAME, // 包名
        editViewTypes.EDIT_VIEW_TYPE_ACTIVITY_NAME, // 活动名
        editViewTypes.EDIT_VIEW_TYPE_NICKNAME, // 显示的名称
        editViewTypes.EDIT_VIEW_TYPE_SHOW_NAME, // 是否显示名称
        editViewTypes.EDIT_VIEW_TYPE_SIZE, // 组件大小
        editViewTypes.EDIT_VIEW_TYPE_DELETE, // 删除组件
    )

    private var _fragmentName = MutableLiveData("No Fragment")
    val fragmentName: LiveData<String> = _fragmentName

    // 首页拖动场景使用
    var widgetListOrderWasModified = MutableLiveData(false)
    // 编辑列表和活动列表共用
    var widgetListSizeWasModified = MutableLiveData(false)
    // 编辑列表使用
    var widgetWasModified = MutableLiveData(false)
    var widgetNameWasModified = MutableLiveData(false)
    private var widgetShowNameWasModified = MutableLiveData(false)
    var widgetSizeWasModified = MutableLiveData(false)


    var editMode = MutableLiveData(false)
    var searchMode = MutableLiveData(false)


    // 获取上下文
    val context: Context
        get() = getApplication<Application>().applicationContext


    // 获取 PackageManager
    private val packageManager: PackageManager
        get() = context.packageManager


    // 判断设备是否为手表
    private fun isWatch(): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH
    }


    fun updateFragmentName(fragmentName: String?) {
        _fragmentName.value = fragmentName
    }


    fun getFragmentName(): String {
        return _fragmentName.value ?: "No Fragment"
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
//         Log.e("大小是否发生改变",
//            "${_storageActivityMap.size != _modifiedSizeStorageActivityMap.size},${
//                _storageActivityMap.entries.zip(
//                    _modifiedSizeStorageActivityMap.entries
//                ).any { (a, b) -> a != b }
//            }")
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
        val flag = modifiedWidget.value?.widgetSize != originWidget.value?.widgetSize
        if(widgetSizeWasModified.value != flag) {
            widgetSizeWasModified.value = flag
        }
    }


    // 获取配置的值
    fun getConfigValue(key: String): Any? {
        return ConfigHelper.getConfigValue(key, this)
    }


    // 更新配置的值
    fun updateConfigValue(key: String, value: Any) {
        ConfigHelper.updateConfigValue(key, value, this)
    }


    // 保存配置文件
    fun saveUserConfig() {
        ConfigHelper.saveUserConfig(context, userConfig)
    }


    // 加载存储的活动信息
    fun loadStorageActivities() {
//        Log.i("ActivityViewModel", "加载活动0")
        if(_storedActivityList.value != null) {
            return
        }

        val storageActivityList = StoredActivityHelper.loadStoredActivityList(context)
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
            StoredActivityHelper.saveStoredActivityList(context, _storedActivityList.value!!)
        } else {
            updateWidget()
            updateStorageActivityListSize()
            StoredActivityHelper.saveStoredActivityList(
                context, _storageActivityMap.values.toMutableList()
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
        _storedActivityList.value?.let { activities ->
            _storedActivityList.value =
                activities.sortedWith(compareBy(collator) { activity -> activity.nickName })
                    .toMutableList()
            _modifiedOrderStorageActivityMap = builtMapFromList(_storedActivityList.value!!)
            widgetListOrderWasModified()
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
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
//        val resolveInfoList: List<ResolveInfo> = getAllExportedActivities()
//        Log.e("ToolboxViewModel", "获取活动列表：${resolveInfoList.size}")


        var installedActivities = resolveInfoList.map { resolveInfo ->
            val applicationInfo = resolveInfo.activityInfo.applicationInfo
            // 应用名
//            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            // 活动名
            val appName = resolveInfo.activityInfo.loadLabel(packageManager).toString()
            val packageName = applicationInfo.packageName
            val activityName = resolveInfo.activityInfo.name

            InstalledActivity(
                appName = appName,
                packageName = packageName,
                activityName = activityName,
            )
        }.sortedWith(compareBy(collator) { it.appName })

        installedActivities = (recommendedActivities + installedActivities).distinct()

        installedAppListSize = installedActivities.size
        // 如果有子标题，则大小减 2
        if(installedActivities[0].packageName.isEmpty()) {
            installedAppListSize -= 2
        }

        _installedActivityList.value = installedActivities

        // 获取活动名到应用信息的映射
        for(installedActivity in installedActivities) {
            val activityName = installedActivity.activityName
            if(!_installActivityMap.containsKey(activityName)) {
                _installActivityMap[activityName] = installedActivity
            }
        }
    }


    // 获取推荐活动列表
    private fun getRecommendedActivities(): List<InstalledActivity> {
//        val splitChar = context.getString(R.string.split_char)
        var recommendedActivities = InstalledActivityHelper.loadInstalledActivityList(
            context = context,
            deviceType = DeviceType.ALL,
        )
//        Log.e("recommendedActivities", "$recommendedActivities")
//        Log.e("ToolboxViewModel", "isWatch: ${isWatch()}")

        recommendedActivities += if(isWatch()) {
            // 手表活动
            InstalledActivityHelper.loadInstalledActivityList(
                context = context,
                deviceType = DeviceType.WATCH,
            )
        } else {
            // 手机活动
            InstalledActivityHelper.loadInstalledActivityList(
                context = context,
                deviceType = DeviceType.PHONE,
            )
        }

        // 过滤掉不存在的活动
        recommendedActivities = recommendedActivities.filter { activity ->
            try {
                packageManager.getApplicationInfo(activity.packageName, 0)
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
                    appName = context.getString(R.string.recommended_activity),
                    packageName = "",
                    activityName = "",
                ),
            ) + recommendedActivities + mutableListOf(
                InstalledActivity(
                    appName = context.getString(R.string.other_activity),
                    packageName = "",
                    activityName = "",
                ),
            )).toMutableList()
        }

        return recommendedActivities
    }


    // 获取该 package 下所有导出的 Activity
    private fun getAllExportedActivities(
        packageName: String
    ): List<ActivityInfo> {
        return packageManager.getPackageInfo(
            packageName, PackageManager.GET_ACTIVITIES
        ).activities?.filter { it.exported } ?: emptyList() // 过滤出被导出的Activity
    }


    // 获取全部可导出的活动列表
    private fun getAllExportedActivities(): List<ResolveInfo> {
//        val packages = packageManager.getInstalledPackages(PackageManager.GET_INTENT_FILTERS)
        val packages = packageManager.getInstalledPackages(PackageManager.MATCH_ALL)
//        Log.e("getAllExportedActivities", "${packages.size}")
        val exportedActivities = mutableListOf<ResolveInfo>()

        for(packageInfo in packages) {
//            Log.e("getAllExportedActivities", packageInfo.packageName)
//            val activities = packageInfo.activities ?: continue
            val activities = getAllExportedActivities(packageInfo.packageName)
//            if(activities.isNotEmpty()) {
//                Log.e("getAllExportedActivities", "${packageInfo.packageName}:${activities.size}")
//            }
            for(activity in activities) {
//                Log.e("getAllExportedActivities", activity.name)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setClassName(packageInfo.packageName, activity.name)
                val resolveInfo = packageManager.resolveActivity(intent, 0)
                if(resolveInfo != null) {
                    exportedActivities.add(resolveInfo)
                }
            }
        }

        return exportedActivities
    }


    // 获取图标
    fun getIcon(installedActivity: InstalledActivity): Drawable? {
        return iconCacheManager.getIcon(
            iconName = installedActivity.iconName,
            packageName = installedActivity.packageName,
            activityName = installedActivity.activityName,
        )
    }
    // 获取图标
    fun getIcon(storedActivity: StoredActivity): Drawable? {
        return iconCacheManager.getIcon(
            iconName = storedActivity.iconName,
            packageName = storedActivity.packageName,
            activityName = storedActivity.activityName,
        )
    }


    // 判断活动是否被存储
    fun isStoredActivity(activityName: String): Boolean {
//        Log.i("ActivityViewModel", "${activityName},${_activityNameToStorageActivityMap.size}")
        return activityName in _storageActivityMap
    }


    // 切换活动列表开关状态
    fun toggleSwitch(isChecked: Boolean, installedActivity: InstalledActivity) {
        if(isChecked) {
            addToModifiedMap(installedActivity)
        } else {
            removeFromModifiedMap(installedActivity)
        }
        widgetListSizeWasModified()
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


    // 将活动添加到修改的字典
    private fun addToModifiedMap(installedActivity: InstalledActivity) {
//        Log.i("ActivityViewModel", "添加到修改字典：${installedActivity.activityName}")
        // 如果之前就有，直接映射；否则新建一个实例
        val storageActivity = _storageActivityMap[installedActivity.activityName]
            ?: installedActivityToStorageActivity(
                installedActivity = installedActivity,
            )
        _modifiedSizeStorageActivityMap[installedActivity.activityName] = storageActivity
    }
    // 将活动添加到修改的字典
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
        // Log.i("ActivityViewModel", "从修改字典中移除：${installedActivity.activityName}")
        _modifiedSizeStorageActivityMap.remove(installedActivity.activityName)
    }
    // 函数重载
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
            widgetSize = widgetSize,
            iconName = installedActivity.iconName,
        )
        return storageActivity
    }
}
