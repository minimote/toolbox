/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Egg
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.DataCleanHelper
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.ImageSaveHelper.setSavePopupMenuListener
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class MyListAdapter(
    private val myActivity: MainActivity,
    val viewModel: MyViewModel,
) : RecyclerView.Adapter<MyListAdapter.MyViewHolder>() {

    private val viewList = ViewList.myList
    private val viewTypes = ViewTypes.My

    inner class MyViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {

        lateinit var imageViewAppIcon: ImageView
        lateinit var textViewAppName: TextView
        lateinit var textViewPackageName: TextView
        lateinit var textViewAppVersion: TextView
        lateinit var textViewAppAuthor: TextView

        lateinit var textViewClearCache: TextView
        lateinit var textViewCacheSize: TextView

        lateinit var textViewClearData: TextView
        lateinit var textViewDataSize: TextView

        lateinit var textViewName: TextView

        init {
            when(viewType) {
                // 应用信息
                viewTypes.APP_INFO -> {
                    imageViewAppIcon = itemView.findViewById(R.id.imageView_appIcon)
                    textViewAppName = itemView.findViewById(R.id.textView_appName)
                    textViewPackageName = itemView.findViewById(R.id.textView_packageName)
                    textViewAppVersion = itemView.findViewById(R.id.textView_app_version)
                    textViewAppAuthor = itemView.findViewById(R.id.textView_app_author)
                }

                // 清除缓存
                viewTypes.CLEAR_CACHE -> {
                    textViewClearCache = itemView.findViewById(R.id.textView_name)
                    textViewCacheSize = itemView.findViewById(R.id.textView_dataSize)
                }

                // 清除数据
                viewTypes.CLEAR_DATA -> {
                    textViewClearData = itemView.findViewById(R.id.textView_name)
                    textViewDataSize = itemView.findViewById(R.id.textView_dataSize)
                }

                viewTypes.CHECK_UPDATE -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                in viewTypes.normalSet -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }
            }
        }
    }


    override fun getItemCount(): Int = viewList.size


    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return viewList[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutId = when(viewType) {
            viewTypes.APP_INFO -> R.layout.item_my_app_info
            viewTypes.CLEAR_CACHE -> R.layout.item_my_clear_data
            viewTypes.CLEAR_DATA -> R.layout.item_my_clear_data
            viewTypes.CHECK_UPDATE -> R.layout.item_my_check_update
            in viewTypes.normalSet -> R.layout.item_my_setting
            else -> -1
        }
        val view = LayoutInflater.from(myActivity).inflate(layoutId, parent, false)
        return MyViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        when(holder.itemViewType) {
            viewTypes.APP_INFO -> {
                setupAppInfo(holder)
            }

            viewTypes.CLEAR_CACHE -> {
                setupClearCache(holder)
            }

            viewTypes.CLEAR_DATA -> {
                setupClearData(holder)
            }

            viewTypes.SUPPORT_AUTHOR -> {
                setFragmentItem(
                    holder = holder,
                    text = myActivity.getString(R.string.support_author),
                    fragmentName = FragmentName.SUPPORT_AUTHOR_FRAGMENT,
                )
            }

            viewTypes.ABOUT_PROJECT -> {
                setFragmentItem(
                    holder = holder,
                    text = myActivity.getString(R.string.about_project),
                    fragmentName = FragmentName.ABOUT_PROJECT_FRAGMENT,
                )
            }

            viewTypes.CHECK_UPDATE -> {
                setupCheckUpdate(holder)
            }

            viewTypes.SETTING -> {
                setFragmentItem(
                    holder = holder,
                    text = myActivity.getString(R.string.setting),
                    fragmentName = FragmentName.SETTING_FRAGMENT,
                )
            }

            viewTypes.INSTRUCTION -> {
                setWebView(
                    holder = holder,
                    textViewText = myActivity.getString(R.string.instruction),
                    url = myActivity.getString(R.string.instruction_url),
                )
            }

            viewTypes.UPDATE_LOG -> {
                setWebView(
                    holder = holder,
                    textViewText = myActivity.getString(R.string.update_log),
                    url = myActivity.getString(R.string.update_log_url),
                )
            }

            viewTypes.PROBLEM_FEEDBACK -> {
                setWebView(
                    holder = holder,
                    textViewText = myActivity.getString(R.string.problem_feedback),
                    url = myActivity.getString(R.string.problem_feedback_url),
                )
            }

            viewTypes.SCHEME_LIST -> {
                setFragmentItem(
                    holder = holder,
                    text = myActivity.getString(R.string.scheme_list),
                    fragmentName = FragmentName.SCHEME_LIST_FRAGMENT,
                )
            }

            viewTypes.APP_DETAIL -> {
                setupAppDetail(holder = holder)
            }

            viewTypes.BACKUP_AND_RESTORE -> {
                setFragmentItem(
                    holder = holder,
                    text = myActivity.getString(R.string.backup_and_recovery),
                    fragmentName = FragmentName.BACKUP_AND_RECOVERY_FRAGMENT,
                )
            }

            viewTypes.LONG_PRESS_MENU -> {
                setFragmentItem(
                    holder = holder,
                    text = myActivity.getString(R.string.long_press_menu),
                    fragmentName = FragmentName.SCHEME_LIST_FRAGMENT,
                )
            }
        }

    }


    // 设置应用信息
    private fun setupAppInfo(holder: MyViewHolder) {
        // 长按弹出保存图片的选项
        holder.imageViewAppIcon.setSavePopupMenuListener(
            fileName = viewModel.myAppName,
            viewModel = viewModel,
            myActivity = myActivity,
        )

        holder.textViewAppName.text = viewModel.myAppName
        // 禁用振动反馈
        holder.textViewAppName.isHapticFeedbackEnabled = false
        holder.textViewAppName.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = myActivity,
                text = viewModel.myAppName,
                toastString = myActivity.getString(R.string.toast_app_name),
            )
            true
        }

        holder.textViewPackageName.text = viewModel.myPackageName
        // 禁用振动反馈
        holder.textViewPackageName.isHapticFeedbackEnabled = false
        holder.textViewPackageName.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = myActivity,
                text = viewModel.myPackageName,
                toastString = myActivity.getString(R.string.toast_package_name),
            )
            true
        }

        holder.textViewAppVersion.text =
            myActivity.getString(R.string.app_version, viewModel.myVersionName)
        // 禁用振动反馈
        holder.textViewAppVersion.isHapticFeedbackEnabled = false
        holder.textViewAppVersion.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = myActivity,
                text = viewModel.myVersionName,
                toastString = myActivity.getString(R.string.toast_app_version),
            )
            true
        }

        // 添加连续点击计数器和上次点击时间
        var clickCount = 0
        var lastClickTime = 0L
        holder.textViewAppVersion.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if(currentTime - lastClickTime < Egg.CLICK_GAP) {
                clickCount++
                if(clickCount >= Egg.CLICK_COUNT) {
                    // 连续点击8次的处理逻辑
                    VibrationHelper.vibrateOnClick(viewModel)
                    clickCount = 0
                    // 显示彩蛋 Fragment
                    FragmentHelper.switchFragment(
                        fragmentName = FragmentName.EASTER_EGG_FRAGMENT,
                        activity = myActivity,
                        viewModel = viewModel,
                    )
                }
            } else {
                clickCount = 1
            }
            lastClickTime = currentTime
        }

        holder.textViewAppAuthor.text =
            myActivity.getString(R.string.app_author, viewModel.myAuthorName)
        // 禁用振动反馈
        holder.textViewAppAuthor.isHapticFeedbackEnabled = false
        holder.textViewAppAuthor.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            ClipboardHelper.copyToClipboard(
                context = myActivity,
                text = viewModel.myAuthorName,
                toastString = myActivity.getString(R.string.toast_author_name),
            )
            true
        }
    }


    // 设置清除缓存
    private fun setupClearCache(holder: MyViewHolder) {
        holder.textViewClearCache.text = myActivity.getString(R.string.clear_cache)
        updateCacheSize(holder)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            DialogHelper.setAndShowDefaultDialog(
                context = myActivity,
                viewModel = viewModel,
                messageText = myActivity.getString(R.string.clear_cache_confirmation),
                positiveAction = {
                    DataCleanHelper.clearCache(viewModel)
                    updateCacheSize(holder)
                    Toast.makeText(
                        myActivity,
                        myActivity.getString(R.string.clear_cache_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                positiveButtonTextColor = myActivity.getColor(R.color.red),
            )
        }
    }
    // 更新缓存大小
    private fun updateCacheSize(holder: MyViewHolder) {
        holder.textViewCacheSize.text = DataCleanHelper.getCacheSize(viewModel)
    }


    // 设置清除数据
    private fun setupClearData(holder: MyViewHolder) {
        holder.textViewClearData.text = myActivity.getString(R.string.clear_data)
        updateDataSize(holder)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            VibrationHelper.vibrateOnDangerousOperation(viewModel)
            DialogHelper.setAndShowDefaultDialog(
                context = myActivity,
                viewModel = viewModel,
                messageText = myActivity.getString(R.string.clear_data_confirmation),
                positiveAction = {
                    DataCleanHelper.clearData(viewModel)
                    updateDataSize(holder)
                    Toast.makeText(
                        myActivity,
                        myActivity.getString(R.string.clear_data_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                positiveButtonTextColor = myActivity.getColor(R.color.red),
            )
        }
    }
    // 更新数据大小
    private fun updateDataSize(holder: MyViewHolder) {
        holder.textViewDataSize.text = DataCleanHelper.getDataSize(viewModel)
    }


    // 设置进入新页面的条目
    private fun setFragmentItem(
        holder: MyViewHolder,
        text: String,
        fragmentName: String,
    ) {
        holder.textViewName.text = text
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            FragmentHelper.switchFragment(
                fragmentName = fragmentName,
                activity = myActivity,
                viewModel = viewModel,
            )
        }
    }


    // 检查更新
    private fun setupCheckUpdate(holder: MyViewHolder) {
//        val clickableContainer = holder.itemView.findViewById<ConstraintLayout>(R.id.clickable_container)

        holder.textViewName.text = myActivity.getString(R.string.check_update)
        //        clickableContainer.setOnClickListener {
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)

            CheckUpdateHelper.checkNetworkAccessModeAndCheckUpdate(
                context = myActivity,
                viewModel = viewModel,
                silence = false,
            )
        }
    }


    private fun setWebView(
        holder: MyViewHolder,
        textViewText: String,
        url: String,
    ) {
        holder.textViewName.text = textViewText
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            viewModel.webViewUrl = url
            FragmentHelper.switchFragment(
                fragmentName = FragmentName.WEB_VIEW_FRAGMENT,
                activity = myActivity,
                viewModel = viewModel,
            )
        }
    }


    // 应用详情
    private fun setupAppDetail(holder: MyViewHolder) {
        holder.textViewName.text = myActivity.getString(R.string.app_detail)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", myActivity.packageName, null)
                intent.data = uri
                LaunchHelper.launch(
                    myActivity = myActivity,
                    viewModel = viewModel,
                    intent = intent,
                )
            } catch(e: Exception) {
                Toast.makeText(
                    myActivity,
                    myActivity.getString(
                        R.string.app_error,
                        e.message,
                    ),
                    Toast.LENGTH_SHORT,
                ).show()
            }

        }
    }


}
