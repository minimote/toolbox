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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.FragmentName
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.fragment.MyListFragment
import cn.minimote.toolbox.helper.CheckUpdateHelper
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.DataCleanHelper
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.FragmentHelper
import cn.minimote.toolbox.helper.ImageSaveHelper
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class MyListAdapter(
    private val myActivity: MainActivity,
    val viewModel: MyViewModel,
    private val fragment: MyListFragment,
    private val fragmentManager: FragmentManager,
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

                viewTypes.SETTING -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.SUPPORT_AUTHOR -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.ABOUT_PROJECT -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.INSTRUCTION -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.UPDATE_LOG -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.PROBLEM_FEEDBACK -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.SCHEME_LIST -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.APP_DETAIL -> {
                    textViewName = itemView.findViewById(R.id.textView_name)
                }

                viewTypes.CHECK_UPDATE -> {
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
            viewTypes.SUPPORT_AUTHOR -> R.layout.item_my_setting
            viewTypes.ABOUT_PROJECT -> R.layout.item_my_setting
            viewTypes.CHECK_UPDATE -> R.layout.item_my_check_update
            viewTypes.SETTING -> R.layout.item_my_setting
            viewTypes.INSTRUCTION -> R.layout.item_my_setting
            viewTypes.UPDATE_LOG -> R.layout.item_my_setting
            viewTypes.PROBLEM_FEEDBACK -> R.layout.item_my_setting
            viewTypes.SCHEME_LIST -> R.layout.item_my_setting
            viewTypes.APP_DETAIL -> R.layout.item_my_setting
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
                setupSupportAuthor(holder)
            }

            viewTypes.ABOUT_PROJECT -> {
                setupAboutProject(holder)
            }

            viewTypes.CHECK_UPDATE -> {
                setupCheckUpdate(holder)
            }

            viewTypes.SETTING -> {
                setupSetting(holder)
            }

            viewTypes.INSTRUCTION -> {
                setupWebView(
                    holder = holder,
                    textViewText = myActivity.getString(R.string.instruction),
                    url = myActivity.getString(R.string.instruction_url),
                )
            }

            viewTypes.UPDATE_LOG -> {
                setupWebView(
                    holder = holder,
                    textViewText = myActivity.getString(R.string.update_log),
                    url = myActivity.getString(R.string.update_log_url),
                )
            }

            viewTypes.PROBLEM_FEEDBACK -> {
                setupWebView(
                    holder = holder,
                    textViewText = myActivity.getString(R.string.problem_feedback),
                    url = myActivity.getString(R.string.problem_feedback_url),
                )
            }

            viewTypes.SCHEME_LIST -> {
                setupSchemeList(holder = holder)
            }

            viewTypes.APP_DETAIL -> {
                setupAppDetail(holder = holder)
            }
        }

    }


    // 设置应用信息
    private fun setupAppInfo(holder: MyViewHolder) {
        // 长按弹出保存图片的选项
        ImageSaveHelper.setPopupMenu(
            imageView = holder.imageViewAppIcon,
            fileName = viewModel.myAppName,
            viewModel = viewModel,
            context = myActivity,
            viewPager = fragment.viewPager,
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
            DialogHelper.showConfirmDialog(
                context = myActivity,
                viewModel = viewModel,
                titleText = myActivity.getString(R.string.clear_cache_confirmation),
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
            DialogHelper.showConfirmDialog(
                context = myActivity,
                viewModel = viewModel,
                titleText = myActivity.getString(R.string.clear_data_confirmation),
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


    // 支持作者
    private fun setupSupportAuthor(holder: MyViewHolder) {
        holder.textViewName.text = myActivity.getString(R.string.supportAuthor)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            FragmentHelper.switchFragment(
                fragmentName = FragmentName.SUPPORT_AUTHOR_FRAGMENT,
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
            )
        }
    }


    // 关于项目
    private fun setupAboutProject(holder: MyViewHolder) {
        holder.textViewName.text = myActivity.getString(R.string.about_project)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            FragmentHelper.switchFragment(
                fragmentName = FragmentName.ABOUT_PROJECT_FRAGMENT,
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
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


    // 设置
    private fun setupSetting(holder: MyViewHolder) {
        holder.textViewName.text = myActivity.getString(R.string.setting)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            FragmentHelper.switchFragment(
                fragmentName = FragmentName.SETTING_FRAGMENT,
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
            )
        }
    }


    // 使用说明
    private fun setupWebView(
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
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                context = myActivity,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
            )
        }
    }

    private fun setupSchemeList(holder: MyViewHolder) {
        holder.textViewName.text = myActivity.getString(R.string.scheme_list)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            FragmentHelper.switchFragment(
                fragmentName = FragmentName.SCHEME_LIST_FRAGMENT,
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                context = myActivity,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
            )
        }
    }


    // 应用详情
    private fun setupAppDetail(holder: MyViewHolder) {
        holder.textViewName.text = myActivity.getString(R.string.app_detail)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", myActivity.packageName, null)
            intent.data = uri
            LaunchHelper.launch(
                myActivity = myActivity,
                viewModel = viewModel,
                intent = intent,
            )

        }
    }
}
