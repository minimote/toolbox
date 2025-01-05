/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.view_model

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.minimote.toolbox.data_class.InstalledActivity
import cn.minimote.toolbox.data_class.StoredActivity
import cn.minimote.toolbox.others.ActivityStorage
import cn.minimote.toolbox.others.IconCacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel
@Inject constructor(
    application: Application,
    private val iconCacheManager: IconCacheManager,
) : AndroidViewModel(application) {

    // 主视图小组件的列数(6 是 2 和 3 的倍数)
    val spanCount = 6

    private val _storedActivityList = MutableLiveData<MutableList<StoredActivity>>()
    val storedActivityList: LiveData<MutableList<StoredActivity>> = _storedActivityList

    private val _installedActivityList = MutableLiveData<List<InstalledActivity>>()
    val installedActivityList: LiveData<List<InstalledActivity>> = _installedActivityList

    // 用 LinkedHashMap 保持插入顺序
    private val _activityNameToStorageActivityMap = LinkedHashMap<String, StoredActivity>()
    private var _modifiedActivityNameToStorageActivityMap = LinkedHashMap<String, StoredActivity>()

    private val _activityNameToInstallActivityMap = mutableMapOf<String, InstalledActivity>()

    var fragmentName = MutableLiveData("")

    var isModified = MutableLiveData(false)
    var isEditMode = MutableLiveData(false)

//    var scrollPosition: Int = SavedStateHandle.get("scrollPosition") ?: 0


    // 加载存储的活动信息
    fun loadStorageActivities() {
        Log.i("ActivityViewModel", "加载活动0")
        if(_storedActivityList.value != null) {
            return
        }
        val context = getApplication<Application>().applicationContext
        val storageActivityList = ActivityStorage.loadActivityList(context)
        _storedActivityList.value = storageActivityList

        Log.i("ActivityViewModel", "加载活动1：${_storedActivityList.value?.size}")
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
        val context = getApplication<Application>().applicationContext
        _storedActivityList.value?.let { ActivityStorage.saveActivityList(context, it) }
    }


    // 获取可启动的活动列表(协程)
    fun getInstalledActivitiesCoroutine() {
        viewModelScope.launch {
            getInstalledActivities()
            // 获取副本，用于比较是否产生修改
            _modifiedActivityNameToStorageActivityMap =
                _activityNameToStorageActivityMap.toMutableMap() as LinkedHashMap<String, StoredActivity>
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
        val collator = Collator.getInstance(Locale.CHINA)

        val installedActivities = resolveInfoList.map { resolveInfo ->
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

        _installedActivityList.value = installedActivities

        // 获取活动名到应用信息的映射
        for(installedActivity in installedActivities) {
            val activityName = installedActivity.activityName
            if(!_activityNameToInstallActivityMap.containsKey(activityName)) {
                _activityNameToInstallActivityMap[activityName] = installedActivity
            }
        }
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


    // 切换开关状态
    fun toggleSwitch(isChecked: Boolean, installedActivity: InstalledActivity) {
        if(isChecked) {
            addToModifiedMap(installedActivity)
        } else {
            removeFromModifiedMap(installedActivity)
        }
        isModified.value = hasActivityListChanged()
//        Log.i(
//            "ActivityViewModel",
//            "${isModified.value}, ${hasActivityListChanged()}, ${_modifiedActivityNameToStorageActivityMap.size},${_activityNameToStorageActivityMap.size}"
//        )
//        Log.i(
//            "ActivityViewModel",
//            "${fragmentStack}"
//        )
    }


    // 将活动添加到修改的字典
    private fun addToModifiedMap(installedActivity: InstalledActivity) {
        Log.i("ActivityViewModel", "添加到修改字典：${installedActivity.activityName}")
        // 如果之前就有，直接映射；否则新建一个实例
        val storageActivity = _activityNameToStorageActivityMap[installedActivity.activityName]
            ?: installedActivityToStorageActivity(
                installedActivity
            )
        _modifiedActivityNameToStorageActivityMap[installedActivity.activityName] = storageActivity
    }


    // 将安装类型的活动转换为存储类型
    private fun installedActivityToStorageActivity(installedActivity: InstalledActivity): StoredActivity {
        val storageActivity = StoredActivity(
            appName = installedActivity.appName,
            packageName = installedActivity.packageName,
            activityName = installedActivity.activityName,
        )
        return storageActivity
    }


    // 将活动从修改的字典中移除
    private fun removeFromModifiedMap(installedActivity: InstalledActivity) {
        Log.i("ActivityViewModel", "从修改字典中移除：${installedActivity.activityName}")
        _modifiedActivityNameToStorageActivityMap.remove(installedActivity.activityName)
    }


    // 活动列表是否发生改变
    private fun hasActivityListChanged(): Boolean {
        return _activityNameToStorageActivityMap != _modifiedActivityNameToStorageActivityMap
    }


    // 更新要存储的活动列表
    fun updateStorageActivityList() {
        if(!hasActivityListChanged()) {
            return
        }
        // 遍历转化的链表，避免边遍历边修改的问题
        for((activityName, storageActivity) in _activityNameToStorageActivityMap.toList()) {
            if(activityName !in _modifiedActivityNameToStorageActivityMap) {
                // 该项被删除了，移除原字典的键值对和列表中的元素
                removeActivity(activityName)
            } else {
                // 该项不变，只移除新字典的键值对
                _modifiedActivityNameToStorageActivityMap.remove(activityName)
            }
        }
        // 剩下的都是新增的
        for(storageActivity in _modifiedActivityNameToStorageActivityMap.values) {
            addActivity(storageActivity)
        }
        _modifiedActivityNameToStorageActivityMap.clear()
    }


    // 保存滚动位置
//    fun saveScrollPosition(position: Int) {
//        scrollPosition = position
//        savedStateHandle.set("scrollPosition", position)
//    }


    fun addActivity(storedActivity: StoredActivity) {
        if(storedActivity.activityName in _activityNameToStorageActivityMap) {
            return
        }
        _storedActivityList.value?.add(storedActivity)
        _activityNameToStorageActivityMap[storedActivity.activityName] = storedActivity
//        // 触发数据更新事件
//        _storageActivityList.postValue(_storageActivityList.value)
    }


    fun removeActivity(activityName: String) {
        val appInfo = _activityNameToStorageActivityMap[activityName] ?: return
        _storedActivityList.value?.remove(appInfo)
        _activityNameToStorageActivityMap.remove(appInfo.packageName)
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
