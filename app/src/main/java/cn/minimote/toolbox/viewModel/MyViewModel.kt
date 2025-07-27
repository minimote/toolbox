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
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import cn.minimote.toolbox.R
import cn.minimote.toolbox.constant.Collator
import cn.minimote.toolbox.constant.Config
import cn.minimote.toolbox.constant.Config.ConfigKeys
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.ToolConstants
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.dataClass.InstalledApp
import cn.minimote.toolbox.dataClass.StoredTool
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ConfigHelper.getConfigValue
import cn.minimote.toolbox.helper.IconCacheHelper
import cn.minimote.toolbox.helper.SchemeHelper
import cn.minimote.toolbox.helper.StoredToolHelper
import cn.minimote.toolbox.helper.ToolListSortHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.math.min


@HiltViewModel
class MyViewModel
@Inject constructor(
    private val application: Application,
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
    val savePath: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            myContext.getString(R.string.app_name_en),
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    // 数据路径
    val dataPath: File
        get() = File(
            myContext.filesDir,
            myContext.getString(R.string.app_name_en),
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }
    // 缓存路径
    val cachePath: File
        get() = File(
            myContext.cacheDir,
            myContext.getString(R.string.app_name_en),
        ).apply {
            if(!exists()) {
                mkdirs()
            }
        }


    val iconCacheHelper: IconCacheHelper = IconCacheHelper(this)


    //    // 最小最大组件大小
//    val minWidgetSize = MIN_WIDGET_SIZE
    val maxWidgetSize = ToolConstants.MAX_WIDGET_SIZE


    // 屏幕尺寸
    val screenWidth: Int get() = myContext.resources.displayMetrics.widthPixels
    val screenHeight: Int get() = myContext.resources.displayMetrics.heightPixels
    // 屏幕短边
    private var screenShortSide = min(screenWidth, screenHeight)
    val imageSize = (UI.FULL_SCREEN_IMAGE_RATE * screenShortSide).toInt()


    // 多选时选中的组件 id
    private val _selectedIds = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedIds: LiveData<MutableSet<String>> = _selectedIds


    // 搜索词(用于高亮显示结果)
    val searchQuery = MutableLiveData("")


    // WebView 网址
    var webViewUrl = ""


    // 上次更新检查时间
    val lastUpdateCheckTime: Long
        get() = getConfigValue(
            key = ConfigKeys.LAST_CHECK_UPDATE_TIME,
        )?.toString()?.toLongOrNull() ?: 0L

    // 检查更新间隔
    val updateCheckGapLong: Long
        get() = CheckUpdateHelper.getUpdateCheckGapLong(
            getConfigValue(
                key = ConfigKeys.CHECK_UPDATE_FREQUENCY,
            ).toString()
        )

    // 下次更新检查时间
    val nextUpdateCheckTime: Long
        get() = lastUpdateCheckTime + updateCheckGapLong


    // 配置文件
    var defaultConfig: Map<String, Any> = Config.defaultConfig
    var userConfig: MutableMap<String, Any> = mutableMapOf()
    var userConfigBackup: MutableMap<String, Any> = mutableMapOf()
    // 配置更改
    val configChanged: MutableLiveData<Boolean> = MutableLiveData()


    // fragment 名称
    private var _fragmentName = MutableLiveData(FragmentName.WIDGET_LIST_FRAGMENT)
    val fragmentName: LiveData<String> = _fragmentName


    // 本地软件列表大小
    val installedAppListSize: Int get() = _installedAppList.value?.size ?: 0


    // 存储的工具列表
    private val _storedToolList = MutableLiveData<MutableList<StoredTool>>()
    val storedToolList: LiveData<MutableList<StoredTool>> = _storedToolList
    // 存储的工具字典 (用 LinkedHashMap 保持插入顺序)
    private var _storedToolMap = LinkedHashMap<String, StoredTool>()
    val storedToolMap: LinkedHashMap<String, StoredTool> = _storedToolMap


    // 本地软件列表
    private val _installedAppList = MutableLiveData<List<InstalledApp>>()
    val installedAppList: LiveData<List<InstalledApp>> = _installedAppList
    // 本地软件字典
    private val _installAppMap = mutableMapOf<String, InstalledApp>()


    // 用来记录组件的增减
    private var _toolListSizeChangeMap = LinkedHashMap<String, StoredTool>()
    // 用来记录组件的顺序变化
    private var _toolListOrderChangeMap = LinkedHashMap<String, StoredTool>()


    // 修改单个组件
    val originTool = MutableLiveData<StoredTool>()
    val editedTool = MutableLiveData<StoredTool>()


    // 首页拖动场景使用
    var toolListOrderChanged: MutableLiveData<Boolean> = MutableLiveData()
    // 编辑列表和活动列表共用
    var toolListSizeChanged: MutableLiveData<Boolean> = MutableLiveData()


    // 编辑列表使用
    var toolNicknameChanged: MutableLiveData<Boolean> = MutableLiveData()
    var toolDisplayModeChanged: MutableLiveData<Boolean> = MutableLiveData()
    var toolWidthChanged: MutableLiveData<Boolean> = MutableLiveData()
    var toolAlignmentChanged: MutableLiveData<Boolean> = MutableLiveData()
    // 自动更新修改状态
    val toolChanged = MediatorLiveData<Boolean>().apply {
        // 初始化值
        value = false

        val observer = Observer<Boolean> { _ ->
            val changed = listOf(
                toolNicknameChanged.value,
                toolDisplayModeChanged.value,
                toolWidthChanged.value,
                toolAlignmentChanged.value,
            ).any { it == true }

            this.value = changed
        }

        addSource(toolNicknameChanged, observer)
        addSource(toolDisplayModeChanged, observer)
        addSource(toolWidthChanged, observer)
        addSource(toolAlignmentChanged, observer)
    }


    // 多选模式、排序模式、搜索模式
    var multiselectMode: MutableLiveData<Boolean> = MutableLiveData(false)
    var sortMode: MutableLiveData<Boolean> = MutableLiveData(false)
    var searchMode: MutableLiveData<Boolean> = MutableLiveData(false)

    var freeSort = false
    var sortModeString = ""

    private val _detailList = MutableLiveData<List<Int>>()


//    // 将 dp 转换为 px
//    fun dpToPx(dp: Float): Int {
//        val density = myContext.resources.displayMetrics.density
//        return (dp * density).toInt()
//    }


    fun updateFragmentName(fragmentName: String?) {
        _fragmentName.value = fragmentName
    }


    fun getFragmentName(): String {
        return _fragmentName.value ?: FragmentName.NO_FRAGMENT
    }


    fun updateDetailList(dataList: List<Int>) {
        _detailList.value = dataList
    }

    fun getDetailList(): List<Int> {
        return _detailList.value?.filter {
            showInDetailList(it)
        } ?: emptyList()
    }

    private fun showInDetailList(viewType: Int): Boolean {
        return when(viewType) {
            ViewTypes.WidgetDetail.SCHEME -> {
                SchemeHelper.getSchemeFromId(originTool.value!!.id).isNotBlank()
            }

            ViewTypes.WidgetDetail.DESCRIPTION -> {
                originTool.value?.description != null
            }

            ViewTypes.WidgetDetail.WARNING_MESSAGE -> {
                originTool.value?.warningMessage != null
            }

            ViewTypes.WidgetDetail.ACTIVITY_NAME -> {
                originTool.value?.activityName != null
            }

            ViewTypes.WidgetDetail.INTENT_EXTRAS -> {
                originTool.value?.intentExtras != null
            }

            ViewTypes.WidgetDetail.INTENT_URI -> {
                originTool.value?.intentUri != null
            }

            else -> {
                true
            }
        }
    }


    // 更新小组件列表顺序发生改变的 LiveData
    private fun updateToolListOrderChanged() {
        val flag = _storedToolMap.entries.zip(
            _toolListOrderChangeMap.entries
        ).any { (a, b) -> a != b }
//        LogHelper.e("updateToolListOrderChanged", "flag: $flag, toolListOrderChanged: ${toolListOrderChanged.value}")
        if(toolListOrderChanged.value != flag) {
            toolListOrderChanged.value = flag
        }
    }


    // 更新小组件列表大小发生改变的 LiveData(包括键值对是否改变)
    fun updateToolListSizeChanged() {
        val flag =
            _storedToolMap.size != _toolListSizeChangeMap.size
                    || _storedToolMap.entries.zip(
                _toolListSizeChangeMap.entries
            ).any { (a, b) -> a != b }
        if(toolListSizeChanged.value != flag) {
            toolListSizeChanged.value = flag
        }
    }


    // 重置单个相关的 LiveData
    fun resetToolChanged() {
        toolNicknameChanged.value = false
        toolDisplayModeChanged.value = false
        toolWidthChanged.value = false
        toolAlignmentChanged.value = false
    }


    // 小组件昵称是否发生改变
    fun updateToolNicknameChanged(): Boolean {
        val flag = editedTool.value?.nickname != originTool.value?.nickname
        if(toolNicknameChanged.value != flag) {
            toolNicknameChanged.value = flag
        }
        return flag
    }


    // 小组件显示方式是否发生改变
    fun updateToolDisplayModeChanged(): Boolean {
        val flag = editedTool.value?.displayMode != originTool.value?.displayMode
        if(toolDisplayModeChanged.value != flag) {
            toolDisplayModeChanged.value = flag
        }
        return flag
    }


    // 小组件宽度是否发生改变
    fun updateToolWidthChanged(): Boolean {
        val flag = editedTool.value?.width != originTool.value?.width
        if(toolWidthChanged.value != flag) {
            toolWidthChanged.value = flag
        }
        return flag
    }


    // 小组件对齐方式是否发生改变
    fun updateToolAlignmentChanged(): Boolean {
        val flag = editedTool.value?.alignment != originTool.value?.alignment
        if(toolAlignmentChanged.value != flag) {
            toolAlignmentChanged.value = flag
        }
        return flag
    }


    // 当前组件是否发生改变
    fun updateToolChanged() {
        // toolChanged 会自动更新
        updateToolNicknameChanged()
        updateToolDisplayModeChanged()
        updateToolWidthChanged()
    }


    // 更新小组件
    private fun updateTool() {
        val id = editedTool.value?.id ?: return
        _storedToolMap[id]?.update(editedTool.value!!)
        originTool.value?.update(editedTool.value!!)
        updateToolChanged()
    }


    // 加载存储的活动信息
    fun loadStorageActivities() {

        val storedToolList = StoredToolHelper.loadStoredToolList(this)
        _storedToolList.value = storedToolList

        // 建立字典
        builtOriginMapAndChangeMap()
    }


    // 通过列表建立字典
    private fun builtMapFromList(storageActivityList: MutableList<StoredTool>): LinkedHashMap<String, StoredTool> {
        return storageActivityList.associateByTo(LinkedHashMap()) { it.id }
    }
    // 字典的复制
    private fun builtMapFromMap(map: LinkedHashMap<String, StoredTool>): LinkedHashMap<String, StoredTool> {
        return map.toMutableMap() as LinkedHashMap<String, StoredTool>
    }


    // 建立字典
    private fun builtOriginMapAndChangeMap() {
        builtOriginMap()
        builtChangeMap()
    }
    // 建立原始字典
    private fun builtOriginMap() {
        _storedToolMap = builtMapFromList(_storedToolList.value!!)
    }
    // 建立用于修改的字典
    private fun builtChangeMap() {
        builtChangeSizeMap()
        builtChangeOrderMap()
    }
    // 建立用于修改列表大小的映射
    private fun builtChangeSizeMap() {
        _toolListSizeChangeMap = builtMapFromList(_storedToolList.value!!)
    }
    // 建立用于修改顺序的映射
    private fun builtChangeOrderMap() {
        _toolListOrderChangeMap = builtMapFromList(_storedToolList.value!!)
    }


    // 恢复原始活动列表顺序
    fun restoreStoredToolList() {
        _storedToolList.value = _storedToolMap.values.toMutableList()
        builtChangeMap()
    }


    // 保存组件列表到存储中
    fun saveWidgetList() {

        when(getFragmentName()) {
            FragmentName.WIDGET_LIST_FRAGMENT -> {
                // 排序模式修改的是 List
                if(sortMode.value == true) {
                    builtOriginMapAndChangeMap()
                    updateToolListOrderChanged()
                } else if(multiselectMode.value == true) {
                    // 多选模式修改的是 SizeChangeMap
                    updateStoredToolListSize()
                } else {
                    // 从全部页面添加到主页或从主页删除的保存
                    updateStoredToolListSize()
                }
            }

            FragmentName.EDIT_LIST_FRAGMENT -> {
                updateTool()
                updateStoredToolListSize()
            }

            FragmentName.INSTALLED_APP_LIST_FRAGMENT -> {
                updateStoredToolListSize()
            }
        }
        StoredToolHelper.saveStoredActivityList(
            this, _storedToolList.value!!
        )
//        updateStoredToolListSize()
//        if(getFragmentName() == FragmentName.WIDGET_LIST_FRAGMENT) {
//            updateStoredToolListOrder()
//        } else {
//            updateTool()
//        }
//        builtOriginMap()

    }


    // 更新要存储的活动列表大小
    private fun updateStoredToolListSize() {

        // 遍历转化的链表，避免边遍历边修改的问题
        for((id, _) in _storedToolMap.toList()) {
            if(id !in _toolListSizeChangeMap) {
                // 该项被删除了，移除列表中的元素和原字典的键值对
                removeToolFromListAndMap(id)
            } else {
                // 该项没动，只移除修改字典的键值对
                _toolListSizeChangeMap.remove(id)
            }
        }
        // 剩下的都是新增的
        for(tool in _toolListSizeChangeMap.values) {
            addToolToListAndMap(tool)
        }

        // 修改大小的时候顺序也可能改变
        builtChangeMap()
        updateToolListSizeChanged()
        updateToolListOrderChanged()

    }


    private fun addToolToListAndMap(storedTool: StoredTool) {
        _storedToolList.value?.add(storedTool)
        _storedToolMap[storedTool.id] = storedTool
    }


    private fun removeToolFromListAndMap(id: String) {
        val appInfo = _storedToolMap[id] ?: return
        _storedToolList.value?.remove(appInfo)
        _storedToolMap.remove(appInfo.id)
    }


    // 更新要存储的活动列表顺序
    fun updateStoredToolListOrder() {

        // 改变顺序的时候大小不会改变
        builtChangeOrderMap()
        updateToolListOrderChanged()

    }


    // 排序
    fun sortStoredToolList(sortType: Int) {
//        Log.e("排序", "开始排序")
        _storedToolList.value?.let { activities ->
//            Log.e("排序", "进入排序")
            _storedToolList.value = ToolListSortHelper.sort(activities, sortType)

            updateStoredToolListOrder()
        }
    }


    // 获取可启动的活动列表(协程)
    fun getInstalledAppCoroutine() {
        viewModelScope.launch {
            getInstalledApp()
        }
    }


    // 获取可启动的活动列表
    private fun getInstalledApp() {

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfoList: List<ResolveInfo> =
            myPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        // 过滤掉本应用
        val installedAppList = resolveInfoList.filter { resolveInfo ->
            val packageName = resolveInfo.activityInfo.applicationInfo.packageName
            packageName != myPackageName
        }.map { resolveInfo ->
            val applicationInfo = resolveInfo.activityInfo.applicationInfo

            // 应用名、包名、活动名
            val appName = resolveInfo.activityInfo.loadLabel(myPackageManager).toString()
            val packageName = applicationInfo.packageName
            val activityName = resolveInfo.activityInfo.name
            InstalledApp(
                name = appName,
                packageName = packageName,
                activityName = activityName,
            )
        }.sortedWith(compareBy(Collator.chineseCollator) { it.name })

        _installedAppList.value = installedAppList

        // 建立 id 到应用信息的映射
        for(installedActivity in installedAppList) {
            val id = installedActivity.id
            _installAppMap[id] = installedActivity
        }
    }


    // 判断活动是否被在修改大小的字典中
    fun inSizeChangeMap(id: String): Boolean {
        return id in _toolListSizeChangeMap
    }


    // 如果修改后的关键字集合与原来的一样，复制映射
    fun syncSizeChangeMapIfSameKeys() {
        if(_storedToolMap.keys.toSet() == _toolListSizeChangeMap.keys.toSet()) {
            _toolListSizeChangeMap = builtMapFromMap(_storedToolMap)
        }
        updateToolListSizeChanged()
    }


    // 将安装的软件添加到修改的字典
    fun addToSizeChangeMap(installedApp: InstalledApp) {
        val id = installedApp.id
        // 如果之前就有，直接映射；否则新建一个实例
        val storageTool = _storedToolMap[id] ?: installedApp.toStoredTool(width = maxWidgetSize)
        _toolListSizeChangeMap[id] = storageTool
        updateToolListSizeChanged()
    }
    // 将工具添加到修改的字典
    fun addToSizeChangeMap(tool: Tool) {
        val storedActivity = tool.toStoredTool(width = maxWidgetSize)
        _toolListSizeChangeMap[storedActivity.id] = storedActivity
        updateToolListSizeChanged()
    }


    // 将工具从修改的字典中移除
    fun removeFromSizeChangeMap(id: String) {
        _toolListSizeChangeMap.remove(id)
        updateToolListSizeChanged()
    }


    // 多选时调用的方法
    // 选中一个条目
    fun selectedItem(id: String) {
        val currentSet = _selectedIds.value ?: mutableSetOf()
        currentSet.add(id)
        _selectedIds.value = currentSet
//        LogHelper.e("多选", "选中 $id, 共选 ${currentSet.size} 项")
    }


    // 删除一个条目
    fun deselectItem(id: String) {
        val currentSet = _selectedIds.value ?: mutableSetOf()
        currentSet.remove(id)
        _selectedIds.value = currentSet
    }


    // 清空集合
    fun clearSelectedIds() {
        _selectedIds.value = mutableSetOf()
    }


    // 全选
    fun selectAllItems() {
        val currentSet = _selectedIds.value ?: mutableSetOf()
        currentSet.addAll(_storedToolMap.keys)
        _selectedIds.value = currentSet
    }


    // 反选
    fun invertSelection() {
        val currentSet = _selectedIds.value ?: mutableSetOf()
        val allKeys = _storedToolMap.keys

        // 创建一个新的集合，包含所有不在当前选中集合中的项
        val newSelection = if(currentSet.isEmpty()) {
            allKeys
        } else {
            allKeys.subtract(currentSet).toMutableSet()
        }

        _selectedIds.value = newSelection
    }


    // 判断是否已选中某个条目
    fun isSelected(id: String): Boolean {
        return _selectedIds.value?.contains(id) ?: false
    }


    // 获取选中集合中的大小
    fun getSelectedSize(): Int {
        return _selectedIds.value?.size ?: 0
    }


    // 删除选中集合中的所有项并保存
    fun deleteSelectedItemAndSave() {
        val currentSet = _selectedIds.value ?: mutableSetOf()
        for(id in currentSet) {
            removeFromSizeChangeMap(id)
        }
        saveWidgetList()
        clearSelectedIds()
//        LogHelper.e("${_storedActivityList.value?.size}", "${storedActivityList.value?.size}")
        // 触发无组件提示
        toolListSizeChanged.value = true
        toolListSizeChanged.value = false
    }

}
