/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.ImageSaveHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel


class AboutProjectAdapter(
    private val context: Context,
    val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<AboutProjectAdapter.SupportAuthorViewHolder>() {

    private val aboutProjectViewList = viewModel.aboutProjectViewList
    private val aboutProjectViewTypes = ToolboxViewModel.Companion.ViewTypes.AboutProject

    inner class SupportAuthorViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {

        lateinit var imageViewQRGitee: ImageView
        lateinit var imageViewQRGitHub: ImageView
        lateinit var textViewURL: TextView

        init {
            when(viewType) {
                aboutProjectViewTypes.PROJECT_PATH_GITEE -> {
                    imageViewQRGitee = itemView.findViewById(R.id.imageView_qr_gitee)
                    textViewURL = itemView.findViewById(R.id.textView_url)
                }

                aboutProjectViewTypes.PROJECT_PATH_GITHUB -> {
                    imageViewQRGitHub = itemView.findViewById(R.id.imageView_qr_github)
                    textViewURL = itemView.findViewById(R.id.textView_url)
                }
            }
        }
    }


    override fun getItemCount(): Int = aboutProjectViewList.size


    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return aboutProjectViewList[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportAuthorViewHolder {
        val layoutId = when(viewType) {
            aboutProjectViewTypes.NOTICE -> R.layout.item_about_project_notice
            aboutProjectViewTypes.PROJECT_PATH_NAME -> R.layout.item_about_project_project_path_name
            aboutProjectViewTypes.PROJECT_PATH_GITEE -> R.layout.item_about_project_project_path_gitee
            aboutProjectViewTypes.PROJECT_PATH_GITHUB -> R.layout.item_about_project_project_path_github
            else -> -1
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return SupportAuthorViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: SupportAuthorViewHolder, position: Int) {

        val imageSize = viewModel.imageSize

        when(holder.itemViewType) {
            aboutProjectViewTypes.PROJECT_PATH_GITEE -> {
                holder.imageViewQRGitee.layoutParams.width = imageSize
                holder.imageViewQRGitee.layoutParams.height = imageSize
                ImageSaveHelper.setPopupMenu(
                    holder.imageViewQRGitee,
                    context.getString(R.string.qr_gitee_file_name),
                    viewModel,
                    context,
                )
                setTextViewUrl(
                    holder.textViewURL,
                    context.getString(R.string.projectPath_gitee),
                )
            }

            aboutProjectViewTypes.PROJECT_PATH_GITHUB -> {
                holder.imageViewQRGitHub.layoutParams.width = imageSize
                holder.imageViewQRGitHub.layoutParams.height = imageSize
                ImageSaveHelper.setPopupMenu(
                    holder.imageViewQRGitHub,
                    context.getString(R.string.qr_github_file_name),
                    viewModel,
                    context,
                )
                setTextViewUrl(
                    holder.textViewURL,
                    context.getString(R.string.projectPath_github),
                )
            }

        }

    }


    // 打开网址
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } catch(e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.openUrlError, url),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // 设置网址
    private fun setTextViewUrl(textViewURL: TextView, url: String) {
        // 单击打开网址
        textViewURL.setOnClickListener {
            VibrationHelper.vibrateOnClick(context, viewModel)
            openUrl(url)
        }
        // 长按复制到剪贴板
        textViewURL.setOnLongClickListener {
            VibrationHelper.vibrateOnClick(context, viewModel)
            ClipboardHelper.copyToClipboard(
                context = context,
                text = textViewURL.text.toString(),
                label = context.getString(R.string.projectPath_name),
            )
            true
        }
    }

}
