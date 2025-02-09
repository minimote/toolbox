/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.view_model

import android.app.Application
import android.content.Intent
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
import cn.minimote.toolbox.objects.ActivityStorageHelper
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
    private val iconCacheManager: IconCacheManager,
) : AndroidViewModel(application) {

    // 主视图小组件的列数(1-6的最小公倍数)
//    val spanCount = 60
    val spanCount = 12
    // 最大组件大小
    val maxWidgetSize = spanCount
    var screenWidth = 0

    private val _storedActivityList = MutableLiveData<MutableList<StoredActivity>>()
    val storedActivityList: LiveData<MutableList<StoredActivity>> = _storedActivityList
    // 原始活动列表(主要用于保存顺序)
    private val _originStoredActivityList = MutableLiveData<MutableList<StoredActivity>>()

    private val _installedActivityList = MutableLiveData<List<InstalledActivity>>()
    val installedActivityList: LiveData<List<InstalledActivity>> = _installedActivityList

    // 用 LinkedHashMap 保持插入顺序
    private val _activityNameToStorageActivityMap = LinkedHashMap<String, StoredActivity>()
    private var _modifiedActivityNameToStorageActivityMap = LinkedHashMap<String, StoredActivity>()

    private val _activityNameToInstallActivityMap = mutableMapOf<String, InstalledActivity>()

    // 修改组件
    var originWidget = MutableLiveData<StoredActivity>()
    var modifiedWidget = MutableLiveData<StoredActivity>()

    // 中文排序器
    private val collator: Collator = Collator.getInstance(Locale.CHINA)


    companion object Constants {
        // 编辑视图类型
        object EditViewTypes {
            const val EDIT_VIEW_TYPE_ACTIVITY_NAME = 0
            const val EDIT_VIEW_TYPE_NICKNAME = 1
            const val EDIT_VIEW_TYPE_SHOW_NAME = 2
            const val EDIT_VIEW_TYPE_SIZE = 3
            const val EDIT_VIEW_TYPE_DELETE = 4
        }

        // Fragment 名称
        object FragmentNames {
            const val WIDGET_LIST_FRAGMENT = "WidgetListFragment"
            const val EDIT_LIST_FRAGMENT = "EditListFragment"
            const val ACTIVITY_LIST_FRAGMENT = "ActivityListFragment"
        }
    }

    val editViewTypes = EditViewTypes
    val fragmentNames = FragmentNames

    val editList = listOf(
        editViewTypes.EDIT_VIEW_TYPE_ACTIVITY_NAME, // 活动名称
        editViewTypes.EDIT_VIEW_TYPE_NICKNAME, // 显示的名称
        editViewTypes.EDIT_VIEW_TYPE_SHOW_NAME, // 是否显示名称
        editViewTypes.EDIT_VIEW_TYPE_SIZE, // 组件大小
        editViewTypes.EDIT_VIEW_TYPE_DELETE, // 删除组件
    )

    private var _fragmentName = MutableLiveData("No Fragment")
    val fragmentName: LiveData<String> = _fragmentName

    var widgetListWasModified = MutableLiveData(false)
    var widgetNameWasModified = MutableLiveData(false)
    var widgetSizeWasModified = MutableLiveData(false)
    var editMode = MutableLiveData(false)


    @Suppress("SpellCheckingInspection")
    private fun getRecommendedActivities(
        packageManager: PackageManager,
    ): List<InstalledActivity> {
        val context = getApplication<Application>().applicationContext
        val splitChar = context.getString(R.string.split_char)
        var recommendedActivities = mutableListOf(
            InstalledActivity(
                appName = "开发者选项",
                packageName = "com.android.settings",
                activityName = "com.android.settings.Settings\$DevelopmentSettingsDashboardActivity",
                iconName = "developer_option",
            ),
            InstalledActivity(
                appName = "无障碍选项",
                packageName = "com.android.settings",
                activityName = "com.android.settings.Settings\$AccessibilitySettingsActivity",
                iconName = "accessibility_option",
            ),
        )

//        Log.e("ToolboxViewModel", "isWatch: ${isWatch()}")
        if(isWatch()) {
            // 手表活动
            recommendedActivities += listOf(
                InstalledActivity(
                    appName = "最近任务",
                    packageName = "com.heytap.wearable.launcher",
                    activityName = "com.android.quickstep.RecentsActivity",
                    iconName = "recent_task",
                ),
                InstalledActivity(
                    appName = "支付宝-付款码",
                    packageName = "com.eg.android.AlipayGphone",
                    activityName = "alipays://showpage=codepay",
                ),
            )
        } else {
            // 手机活动
            recommendedActivities += listOf(
                InstalledActivity(
                    appName = "支付宝-收款码",
                    packageName = "com.eg.android.AlipayGphone",
                    activityName = "alipays://platformapi/startapp?appId=20000123",
                ),
                InstalledActivity(
                    appName = "支付宝-付款码",
                    packageName = "com.eg.android.AlipayGphone",
                    activityName = "alipays://platformapi/startapp?appId=20000056",
                ),
                InstalledActivity(
                    appName = "支付宝-扫一扫",
                    packageName = "com.eg.android.AlipayGphone",
                    activityName = "alipays://platformapi/startapp?appId=10000007",
                ),
                InstalledActivity(
                    appName = "支付宝-乘车码",
                    packageName = "com.eg.android.AlipayGphone",
                    activityName = "alipays://platformapi/startapp?appId=200011235",
                ),
                InstalledActivity(
                    appName = "微信-付款码",
                    packageName = "com.tencent.mm",
                    activityName = listOf(
                        "com.tencent.mm.action.BIZSHORTCUT",
                        "LauncherUI.Shortcut.LaunchType",
                        "launch_type_offline_wallet",
                    ).joinToString(splitChar)
                ),
                InstalledActivity(
                    appName = "微信-扫一扫",
                    packageName = "com.tencent.mm",
                    activityName = "LauncherUI.From.Scaner.Shortcut",
                ),
                InstalledActivity(
                    appName = "微信-名片码",
                    packageName = "com.tencent.mm",
                    activityName = listOf(
                        "com.tencent.mm.action.BIZSHORTCUT",
                        "LauncherUI.Shortcut.LaunchType",
                        "launch_type_my_qrcode",
                    ).joinToString(splitChar)
                ),
//                InstalledActivity(
//                    appName = "云闪付-收款码",
//                    packageName = "com.unionpay",
//                    activityName = "upwallet://native/codecollect",
//                ),
                InstalledActivity(
                    appName = "云闪付-付款码",
                    packageName = "com.unionpay",
                    activityName = "upwallet://native/codepay",
                ),
                InstalledActivity(
                    appName = "云闪付-扫一扫",
                    packageName = "com.unionpay",
                    activityName = "upwallet://native/scanCode",
                ),
//                InstalledActivity(
//                    appName = "云闪付-乘车码",
//                    packageName = "com.unionpay",
//                    activityName = "upwallet://rn/rnhtmlridingcode",
//                ),
            )
        }

        // 过滤掉不存在的活动
        recommendedActivities = recommendedActivities.filter { activity ->
            try {
                packageManager.getApplicationInfo(activity.packageName, 0)
                true
            } catch(e: PackageManager.NameNotFoundException) {
                false
            }
        }.toMutableList()

        return recommendedActivities.distinct().sortedWith(compareBy(collator) { it.appName })
    }


    // 判断设备是否为手表
    private fun isWatch(): Boolean {
        val context = getApplication<Application>().applicationContext
        val uiMode = context.resources.configuration.uiMode
        return uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH
    }


    fun updateFragmentName(fragmentName: String?) {
        _fragmentName.value = fragmentName
    }


    fun getFragmentName(): String {
        return _fragmentName.value ?: "No Fragment"
    }


    // 小组件是否发生改变
    fun widgetWasModified() {
        widgetListWasModified.value = modifiedWidget.value != originWidget.value
    }


    // 小组件名称是否发生改变
    fun widgetNameWasModified() {
        widgetNameWasModified.value = modifiedWidget.value?.nickName != originWidget.value?.appName
    }


    // 小组件大小是否发生改变
    fun widgetSizeWasModified() {
        widgetSizeWasModified.value =
            modifiedWidget.value?.widgetSize != originWidget.value?.widgetSize
    }


    // 更新要存储的活动列表
    fun updateWidget() {
        if(widgetListWasModified.value != true) {
            return
        }
        val activityName = modifiedWidget.value?.activityName ?: return
        _storedActivityList
        _activityNameToStorageActivityMap[activityName]?.update(modifiedWidget.value!!)
        widgetSizeWasModified.value = false
    }


    // 加载存储的活动信息
    fun loadStorageActivities() {
//        Log.i("ActivityViewModel", "加载活动0")
        if(_storedActivityList.value != null) {
            return
        }
        val context = getApplication<Application>().applicationContext
        val storageActivityList = ActivityStorageHelper.loadActivityList(context)
        _storedActivityList.value = storageActivityList
        // 更新原始活动列表
        updateOriginStoredActivityList()

//        Log.i("ActivityViewModel", "加载活动1：${_storedActivityList.value?.size}")
        for(storageActivity in storageActivityList) {
            // 建立活动名到应用信息的映射
            val activityName = storageActivity.activityName
            if(!_activityNameToStorageActivityMap.containsKey(activityName)) {
                _activityNameToStorageActivityMap[activityName] = storageActivity
            }
        }
//        Log.i("ActivityViewModel", "加载活动2：${_activityNameToStorageActivityMap.size}")
    }


    // 保存活动列表到存储中
    fun saveStorageActivities() {
//        Log.i("saveStorageActivities", "保存活动列表到存储中")
        val context = getApplication<Application>().applicationContext

        _storedActivityList.value?.let {
            ActivityStorageHelper.saveActivityList(context, it)
            // 更新原始活动列表
            updateOriginStoredActivityList()
        }
        widgetListWasModified.value = false
    }


    // 给 _storedActivityList 排序
    fun sortStoredActivityList() {
        _storedActivityList.value?.let { activities ->
            _storedActivityList.value =
                activities.sortedWith(compareBy(collator) { activity -> activity.nickName })
                    .toMutableList()
            storedActivityListWasModified()
        }
    }


    // 更新原始活动列表
    private fun updateOriginStoredActivityList() {
        _originStoredActivityList.value = _storedActivityList.value?.toMutableList()
    }


    // 恢复原始活动列表
    fun restoreOriginStoredActivityList() {
        _storedActivityList.value = _originStoredActivityList.value?.toMutableList()
    }


    // storedActivityList 是否发生改变
    fun storedActivityListWasModified() {
        widgetListWasModified.value = _storedActivityList.value != _originStoredActivityList.value
    }


    // 设置修改字典
    fun setupModifiedActivityNameToStorageActivityMap() {
        _modifiedActivityNameToStorageActivityMap =
            _activityNameToStorageActivityMap.toMutableMap() as LinkedHashMap<String, StoredActivity>
    }


    // 获取可启动的活动列表(协程)
    fun getInstalledActivitiesCoroutine() {
        viewModelScope.launch {
            getInstalledActivities()
            // 设置修改字典，用于比较是否产生修改
            setupModifiedActivityNameToStorageActivityMap()
//            Log.i(
//                "ActivityViewModel",
//                "获取副本：${_activityNameToStorageActivityMap.size},${_modifiedActivityNameToStorageActivityMap.size}"
//            )
//            Log.i(
//                "ActivityViewModel", "${_storedActivityList.value?.size}"
//            )
        }
    }


    // 获取可启动的活动列表
    private fun getInstalledActivities() {
        if(_installedActivityList.value != null) {
            return
        }
        val context = getApplication<Application>().applicationContext
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val packageManager: PackageManager = context.packageManager
        val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        // 推荐工具
        val recommendedActivities = getRecommendedActivities(packageManager)

//        val alipay_packageName = "com.eg.android.AlipayGphone"
//        Log.i(
//            "支付宝活动",
//            "${getAllActivitiesByPackageName(alipay_packageName)}"
//        )

        var installedActivities = resolveInfoList.map { resolveInfo ->
            val applicationInfo = resolveInfo.activityInfo.applicationInfo
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()
            val packageName = applicationInfo.packageName
            val activityName = resolveInfo.activityInfo.name

            InstalledActivity(
                appName = appName,
                packageName = packageName,
                activityName = activityName,
            )
        }.distinct().sortedWith(compareBy(collator) { it.appName })

        installedActivities = recommendedActivities + installedActivities
        _installedActivityList.value = installedActivities

        // 获取活动名到应用信息的映射
        for(installedActivity in installedActivities) {
            val activityName = installedActivity.activityName
            if(!_activityNameToInstallActivityMap.containsKey(activityName)) {
                _activityNameToInstallActivityMap[activityName] = installedActivity
            }
        }
    }


    private fun getAllActivitiesByPackageName(packageName: String): List<String> {
        val activities = mutableListOf<String>()
        val context = getApplication<Application>().applicationContext
        val packageManager: PackageManager = context.packageManager

        try {
            val packageInfo = packageManager.getPackageInfo(
                packageName, PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
            )
            val activitiesInfo = packageInfo.activities ?: return activities

            for(activityInfo in activitiesInfo) {
                activities.add(activityInfo.name)
            }
        } catch(e: PackageManager.NameNotFoundException) {
            // Log.e("PackageManager", "Package not found: $packageName", e)
        }

        return activities
    }


    // 获取图标
    fun getIcon(packageName: String): Drawable {
        return iconCacheManager.getIcon(packageName)
    }


    // 判断活动是否被存储
    fun isStoredActivity(activityName: String): Boolean {
//        Log.i("ActivityViewModel", "${activityName},${_activityNameToStorageActivityMap.size}")
        return activityName in _activityNameToStorageActivityMap
    }


    // 切换活动列表开关状态
    fun toggleSwitch(isChecked: Boolean, installedActivity: InstalledActivity) {
        if(isChecked) {
            addToModifiedMap(installedActivity)
        } else {
            removeFromModifiedMap(installedActivity)
        }
        widgetListWasModified.value = hasActivityListChanged()
//        Log.i(
//            "ActivityViewModel",
//            "${isModified.value}, ${hasActivityListChanged()}, ${_modifiedActivityNameToStorageActivityMap.size},${_activityNameToStorageActivityMap.size}"
//        )
//        Log.i(
//            "ActivityViewModel",
//            "${fragmentStack}"
//        )
    }


    // 删除或恢复组件
    fun deleteOrRestoreWidget(isDelete: Boolean, activityName: String) {
        if(isDelete) {
            removeFromModifiedMap(activityName)
        } else {
            addToModifiedMap(activityName)
        }
        widgetListWasModified.value = hasActivityListChanged()
    }


    // 将活动添加到修改的字典
    private fun addToModifiedMap(installedActivity: InstalledActivity) {
//        Log.i("ActivityViewModel", "添加到修改字典：${installedActivity.activityName}")
        // 如果之前就有，直接映射；否则新建一个实例
        val storageActivity = _activityNameToStorageActivityMap[installedActivity.activityName]
            ?: installedActivityToStorageActivity(
                installedActivity = installedActivity,
            )
        _modifiedActivityNameToStorageActivityMap[installedActivity.activityName] = storageActivity
    }
    // 函数重载
    private fun addToModifiedMap(activityName: String) {
        // Log.i("ActivityViewModel", "添加到修改字典：${activityName}")
        val storageActivity =
            _activityNameToStorageActivityMap[activityName] ?: modifiedWidget.value
//        if(storageActivity != null) {
        _modifiedActivityNameToStorageActivityMap[activityName] = storageActivity!!
//        } else {
////            Log.i("ViewModel(addToModifiedMap)", "没有找到活动：$activityName")
//            // 保存删除操作后再恢复
//
//        }
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


    // 将活动从修改的字典中移除
    private fun removeFromModifiedMap(installedActivity: InstalledActivity) {
        // Log.i("ActivityViewModel", "从修改字典中移除：${installedActivity.activityName}")
        _modifiedActivityNameToStorageActivityMap.remove(installedActivity.activityName)
    }
    // 函数重载
    private fun removeFromModifiedMap(activityName: String) {
        // Log.i("ActivityViewModel", "从修改字典中移除：$activityName")
        _modifiedActivityNameToStorageActivityMap.remove(activityName)
    }


    // 活动列表是否发生改变
    private fun hasActivityListChanged(): Boolean {
        return _activityNameToStorageActivityMap != _modifiedActivityNameToStorageActivityMap
    }


    // 更新要存储的活动列表
    fun updateStorageActivityList() {
//        Log.i(
//            "准备更新要存储的活动列表",
//            "${_storedActivityList.value?.size},${_activityNameToStorageActivityMap.size},${_modifiedActivityNameToStorageActivityMap.size}"
//        )
        if(!hasActivityListChanged()) {
            return
        }
        // 遍历转化的链表，避免边遍历边修改的问题
        for((activityName, _) in _activityNameToStorageActivityMap.toList()) {
            if(activityName !in _modifiedActivityNameToStorageActivityMap) {
                // 该项被删除了，移除原字典的键值对和列表中的元素
                removeActivity(activityName)
//                Log.i("删除：$activityName", "${activityName in _activityNameToStorageActivityMap}")
            } else {
                // 该项不变，只移除新字典的键值对
                _modifiedActivityNameToStorageActivityMap.remove(activityName)
            }
        }
        // 剩下的都是新增的
        for(storageActivity in _modifiedActivityNameToStorageActivityMap.values) {
            addActivity(storageActivity)
        }
        setupModifiedActivityNameToStorageActivityMap()
//        Log.i(
//            "完成更新要存储的活动列表",
//            "${_storedActivityList.value?.size},${_activityNameToStorageActivityMap.size},${_modifiedActivityNameToStorageActivityMap.size}"
//        )
    }


    private fun addActivity(storedActivity: StoredActivity) {
//        if(storedActivity.activityName in _activityNameToStorageActivityMap) {
//            return
//        }
        _storedActivityList.value?.add(storedActivity)
        _activityNameToStorageActivityMap[storedActivity.activityName] = storedActivity
//        // 触发数据更新事件
//        _storageActivityList.postValue(_storageActivityList.value)
    }


    private fun removeActivity(activityName: String) {
        val appInfo = _activityNameToStorageActivityMap[activityName] ?: return
        // Log.i("删除${activityName}", "1")
        _storedActivityList.value?.remove(appInfo)
        _activityNameToStorageActivityMap.remove(appInfo.activityName)
//        // 触发数据更新事件
//        _storageActivityList.postValue(_storageActivityList.value)
    }


    fun moveToFront(activityName: String) {
        val appInfo = _activityNameToStorageActivityMap[activityName] ?: return
        _storedActivityList.value?.remove(appInfo)
        _storedActivityList.value?.add(0, appInfo)
//        // 触发数据更新事件
//        _storageActivityList.postValue(_storageActivityList.value)
    }


    fun moveToBack(activityName: String) {
        val appInfo = _activityNameToStorageActivityMap[activityName] ?: return
        _storedActivityList.value?.remove(appInfo)
        _storedActivityList.value?.add(appInfo)
//        // 触发数据更新事件
//        _storageActivityList.postValue(_storageActivityList.value)
    }
}
