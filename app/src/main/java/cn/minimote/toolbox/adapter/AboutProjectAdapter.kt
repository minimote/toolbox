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
import cn.minimote.toolbox.constant.ViewLists
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.helper.ClipboardHelper
import cn.minimote.toolbox.helper.ImageSaveHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.ToolboxViewModel


class AboutProjectAdapter(
    private val context: Context,
    val viewModel: ToolboxViewModel,
) : RecyclerView.Adapter<AboutProjectAdapter.SupportAuthorViewHolder>() {

    private val viewList = ViewLists.aboutProjectViewList
    private val viewTypes = ViewTypes.AboutProject

    inner class SupportAuthorViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {

        lateinit var imageViewQRCode: ImageView
        lateinit var textViewTitle: TextView
        lateinit var textViewContent: TextView

        init {
            when(viewType) {
                viewTypes.PROJECT_PATH_GITEE -> {
                    imageViewQRCode = itemView.findViewById(R.id.imageView_qr_code)
                    textViewTitle = itemView.findViewById(R.id.textView_title)
                    textViewContent = itemView.findViewById(R.id.textView_content)
                }

                viewTypes.PROJECT_PATH_GITHUB -> {
                    imageViewQRCode = itemView.findViewById(R.id.imageView_qr_code)
                    textViewTitle = itemView.findViewById(R.id.textView_title)
                    textViewContent = itemView.findViewById(R.id.textView_content)
                }

                viewTypes.LICENSE_TITLE -> {
                    textViewTitle = itemView.findViewById(R.id.textView_title)
                }

                viewTypes.LICENSE -> {
                    textViewContent = itemView.findViewById(R.id.textView_content)
                }
            }
        }
    }


    override fun getItemCount(): Int = viewList.size


    override fun getItemViewType(position: Int): Int {
        // 根据位置返回不同的视图类型
        return viewList[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportAuthorViewHolder {
        val layoutId = when(viewType) {
            viewTypes.NOTICE -> R.layout.item_about_project_notice
            viewTypes.PROJECT_PATH_NAME -> R.layout.item_about_project_title
            viewTypes.PROJECT_PATH_GITEE -> R.layout.item_about_project_project_path
            viewTypes.PROJECT_PATH_GITHUB -> R.layout.item_about_project_project_path
            viewTypes.LICENSE_TITLE -> R.layout.item_about_project_title
            viewTypes.LICENSE -> R.layout.item_about_project_notice
            else -> -1
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return SupportAuthorViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: SupportAuthorViewHolder, position: Int) {

        val imageSize = viewModel.imageSize

        when(holder.itemViewType) {
            viewTypes.PROJECT_PATH_GITEE -> {
                holder.textViewTitle.text = context.getString(R.string.gitee)

                holder.textViewContent.text = context.getString(R.string.projectPath_gitee)
                setTextViewUrl(
                    holder.textViewContent,
                    holder.textViewContent.text.toString(),
                )

                holder.imageViewQRCode.setImageResource(R.drawable.qr_gitee)
                holder.imageViewQRCode.layoutParams.width = imageSize
                holder.imageViewQRCode.layoutParams.height = imageSize
                ImageSaveHelper.setPopupMenu(
                    holder.imageViewQRCode,
                    context.getString(R.string.qr_gitee_file_name),
                    viewModel,
                    context,
                )
            }

            viewTypes.PROJECT_PATH_GITHUB -> {
                holder.textViewTitle.text = context.getString(R.string.github)

                holder.textViewContent.text = context.getString(R.string.projectPath_github)
                setTextViewUrl(
                    holder.textViewContent,
                    holder.textViewContent.text.toString(),
                )

                holder.imageViewQRCode.setImageResource(R.drawable.qr_github)
                holder.imageViewQRCode.layoutParams.width = imageSize
                holder.imageViewQRCode.layoutParams.height = imageSize
                ImageSaveHelper.setPopupMenu(
                    holder.imageViewQRCode,
                    context.getString(R.string.qr_github_file_name),
                    viewModel,
                    context,
                )
            }

            viewTypes.LICENSE_TITLE -> {
                holder.textViewTitle.text = context.getString(R.string.license_title)
            }

            viewTypes.LICENSE -> {
                holder.textViewContent.text =
                    context.assets.open("license.txt").use { inputStream ->
                        inputStream.readBytes().decodeToString()
                    }

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
