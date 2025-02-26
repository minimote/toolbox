/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.fragment.MyListFragment
import cn.minimote.toolbox.objects.ClipboardHelper
import cn.minimote.toolbox.objects.DataCleanHelper
import cn.minimote.toolbox.objects.FragmentHelper
import cn.minimote.toolbox.objects.ImageSaveHelper
import cn.minimote.toolbox.objects.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel


class MyListAdapter(
    private val context: Context,
    val viewModel: ToolboxViewModel,
    private val fragment: MyListFragment,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<MyListAdapter.MyViewHolder>() {

    private val myList = viewModel.myList
    private val myViewTypes = ToolboxViewModel.Constants.MyViewTypes
    private val fragmentNames = ToolboxViewModel.Constants.FragmentNames

    inner class MyViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {

        lateinit var imageViewAppIcon: ImageView
        lateinit var textViewAppName: TextView
        lateinit var textViewPackageName: TextView
        lateinit var textViewAppVersion: TextView

        lateinit var textViewClearCache: TextView
        lateinit var textViewCacheSize: TextView

        lateinit var textViewClearData: TextView
        lateinit var textViewDataSize: TextView

        init {
            when(viewType) {
                // 应用信息
                myViewTypes.APP_INFO -> {
                    imageViewAppIcon = itemView.findViewById(R.id.imageView_appIcon)
                    textViewAppName = itemView.findViewById(R.id.textView_appName)
                    textViewPackageName = itemView.findViewById(R.id.textView_packageName)
                    textViewAppVersion = itemView.findViewById(R.id.textView_app_version)
                }

                // 清除缓存
                myViewTypes.CLEAR_CACHE -> {
                    textViewClearCache = itemView.findViewById(R.id.textView_clearData)
                    textViewCacheSize = itemView.findViewById(R.id.textView_dataSize)
                }

                // 清除数据
                myViewTypes.CLEAR_DATA -> {
                    textViewClearData = itemView.findViewById(R.id.textView_clearData)
                    textViewDataSize = itemView.findViewById(R.id.textView_dataSize)
                }
            }
        }
    }


    override fun getItemCount(): Int = myList.size


    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return myList[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutId = when(viewType) {
            myViewTypes.APP_INFO -> R.layout.item_my_app_info
            myViewTypes.CLEAR_CACHE -> R.layout.item_my_clear_data
            myViewTypes.CLEAR_DATA -> R.layout.item_my_clear_data
            myViewTypes.SUPPORT_AUTHOR -> R.layout.item_my_support_author
            myViewTypes.ABOUT_PROJECT -> R.layout.item_my_about_project
            else -> -1
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return MyViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        when(holder.itemViewType) {
            myViewTypes.APP_INFO -> {
                setupAppInfo(holder)
            }

            myViewTypes.CLEAR_CACHE -> {
                setupClearCache(holder)
            }

            myViewTypes.CLEAR_DATA -> {
                setupClearData(holder)
            }

            myViewTypes.SUPPORT_AUTHOR -> {
                setupSupportAuthor(holder)
            }

            myViewTypes.ABOUT_PROJECT -> {
                setupAboutProject(holder)
            }
        }

    }


    // 设置应用信息
    private fun setupAppInfo(holder: MyViewHolder) {
        ImageSaveHelper.setPopupMenu(
            holder.imageViewAppIcon,
            viewModel.appName,
            viewModel,
            context,
        )
        holder.textViewAppName.text = viewModel.appName
        holder.textViewAppName.setOnLongClickListener {
            VibrationHelper.vibrateOnClick(context)
            ClipboardHelper.copyToClipboard(
                context = context,
                text = holder.textViewAppName.text as String,
            )
            true
        }
        holder.textViewPackageName.text = viewModel.packageName
        holder.textViewPackageName.setOnLongClickListener {
            VibrationHelper.vibrateOnClick(context)
            ClipboardHelper.copyToClipboard(
                context = context,
                text = holder.textViewPackageName.text as String,
            )
            true
        }
        holder.textViewAppVersion.text =
            context.getString(R.string.app_version, viewModel.versionName)
    }


    // 设置清除缓存
    private fun setupClearCache(holder: MyViewHolder) {
        holder.textViewClearCache.text = context.getString(R.string.clear_cache)
        updateCacheSize(holder)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            showClearCacheConfirmationDialog(holder)
        }
    }
    // 显示清除缓存的确认对话框
    private fun showClearCacheConfirmationDialog(holder: MyViewHolder) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.clear_cache_confirmation))
        builder.setPositiveButton(context.getString(R.string.confirm)) { dialog, _ ->
            VibrationHelper.vibrateOnClick(context)
            DataCleanHelper.clearCache(context)
            updateCacheSize(holder)
            Toast.makeText(
                context,
                context.getString(R.string.clear_cache_success),
                Toast.LENGTH_SHORT,
            ).show()
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            VibrationHelper.vibrateOnClick(context)
            dialog.dismiss()
        }
        builder.show()
    }
    // 更新缓存大小
    private fun updateCacheSize(holder: MyViewHolder) {
        holder.textViewCacheSize.text = DataCleanHelper.getCacheSize(context)
    }


    // 设置清除数据
    private fun setupClearData(holder: MyViewHolder) {
        holder.textViewClearData.text = context.getString(R.string.clear_data)
        updateDataSize(holder)
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            showClearDataConfirmationDialog(holder)
        }
    }
    // 显示清除数据的确认对话框
    private fun showClearDataConfirmationDialog(holder: MyViewHolder) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.clear_data_confirmation))
        builder.setPositiveButton(context.getString(R.string.confirm)) { dialog, _ ->
            VibrationHelper.vibrateOnClick(context)
            DataCleanHelper.clearData(viewModel)
            updateDataSize(holder)
            Toast.makeText(
                context,
                context.getString(R.string.clear_data_success),
                Toast.LENGTH_SHORT,
            ).show()
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            VibrationHelper.vibrateOnClick(context)
            dialog.dismiss()
        }
        builder.show()
    }
    // 更新数据大小
    private fun updateDataSize(holder: MyViewHolder) {
        holder.textViewDataSize.text = DataCleanHelper.getDataSize(context)
    }


    // 支持作者
    private fun setupSupportAuthor(holder: MyViewHolder) {
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            FragmentHelper.switchFragment(
                fragmentName = fragmentNames.SUPPORT_AUTHOR_FRAGMENT,
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
            )
        }
    }


    // 关于项目
    private fun setupAboutProject(holder: MyViewHolder) {
        holder.itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            FragmentHelper.switchFragment(
                fragmentName = fragmentNames.ABOUT_PROJECT_FRAGMENT,
                fragmentManager = fragmentManager,
                viewModel = viewModel,
                viewPager = fragment.viewPager,
                constraintLayoutOrigin = fragment.constraintLayoutOrigin,
            )
        }
    }

}
