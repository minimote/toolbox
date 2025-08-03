/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.Tools
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.helper.ImageSaveHelper
import cn.minimote.toolbox.helper.LaunchHelper
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel


class SupportAuthorAdapter(
    private val myActivity: MainActivity,
    val viewModel: MyViewModel,
) : RecyclerView.Adapter<SupportAuthorAdapter.SupportAuthorViewHolder>() {

    private val viewList = ViewList.supportAuthorViewList
    private val viewTypes = ViewTypes.SupportAuthor

    inner class SupportAuthorViewHolder(
        itemView: View,
        viewType: Int,
    ) : RecyclerView.ViewHolder(itemView) {

        lateinit var imageViewQRAlipay: ImageView

        lateinit var buttonGotoAlipay: Button

        lateinit var imageViewQRWechat: ImageView

        lateinit var buttonSaveWechatQR: Button
        lateinit var buttonOpenWechatScan: Button

        init {
            when(viewType) {
                viewTypes.QR_ALIPAY -> {
                    imageViewQRAlipay = itemView.findViewById(R.id.imageView_qr_alipay)
                }

                viewTypes.OPERATE_ALIPAY -> {
                    buttonGotoAlipay = itemView.findViewById(R.id.button_goto_alipay)
                }

                viewTypes.QR_WECHAT -> {
                    imageViewQRWechat = itemView.findViewById(R.id.imageView_qr_wechat)
                }

                viewTypes.OPERATE_WECHAT -> {
                    buttonSaveWechatQR = itemView.findViewById(R.id.button_save_qr)
                    buttonOpenWechatScan = itemView.findViewById(R.id.button_open_wechat_scan)
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
            viewTypes.WELCOME -> R.layout.item_support_author_welcome
            viewTypes.NOTICE -> R.layout.item_support_author_notice
            viewTypes.QR_ALIPAY -> R.layout.item_support_author_qr_alipay
            viewTypes.OPERATE_ALIPAY -> R.layout.item_support_author_operate_alipay
            viewTypes.QR_WECHAT -> R.layout.item_support_author_qr_wechat
            viewTypes.OPERATE_WECHAT -> R.layout.item_support_author_operate_wechat
            else -> -1
        }
        val view = LayoutInflater.from(myActivity).inflate(layoutId, parent, false)
        return SupportAuthorViewHolder(view, viewType)
    }


    override fun onBindViewHolder(holder: SupportAuthorViewHolder, position: Int) {

        val imageSize = viewModel.imageSize

        when(holder.itemViewType) {
            viewTypes.QR_ALIPAY -> {
                holder.imageViewQRAlipay.layoutParams.width = imageSize
                holder.imageViewQRAlipay.layoutParams.height = imageSize
                ImageSaveHelper.setPopupMenu(
                    imageView = holder.imageViewQRAlipay,
                    fileName = myActivity.getString(R.string.qr_alipay_file_name),
                    viewModel = viewModel,
                    context = myActivity,
                )
            }

            viewTypes.OPERATE_ALIPAY -> {
                setupAlipay(holder)
            }

            viewTypes.QR_WECHAT -> {
                holder.imageViewQRWechat.layoutParams.width = imageSize
                holder.imageViewQRWechat.layoutParams.height = imageSize
                ImageSaveHelper.setPopupMenu(
                    imageView = holder.imageViewQRWechat,
                    fileName = myActivity.getString(R.string.qr_wechat_file_name),
                    viewModel = viewModel,
                    context = myActivity,
                )
            }

            viewTypes.OPERATE_WECHAT -> {
                setupWechat(holder)
            }

        }

    }


    // 设置支付宝跳转
    private fun setupAlipay(holder: SupportAuthorViewHolder) {
        // 手表隐藏跳转按钮
        if(viewModel.isWatch) {
            holder.buttonGotoAlipay.visibility = View.GONE
        } else {
            holder.buttonGotoAlipay.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                gotoAlipay()
            }
        }
    }


    private fun gotoAlipay() {
        LaunchHelper.launch(
            myActivity = myActivity,
            viewModel = viewModel,
            tool = Tools.AppTool.Alipay.supportAuthor,
        )
    }


    // 设置微信
    private fun setupWechat(holder: SupportAuthorViewHolder) {
        // 手表隐藏跳转按钮
        if(viewModel.isWatch) {
            holder.buttonSaveWechatQR.visibility = View.GONE
            holder.buttonOpenWechatScan.visibility = View.GONE
        } else {
            holder.buttonSaveWechatQR.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                saveWechatQR(myActivity)
            }

            holder.buttonOpenWechatScan.setOnClickListener {
                VibrationHelper.vibrateOnClick(viewModel)
                openWechatScan()
            }
        }
    }


    // 保存微信二维码
    private fun saveWechatQR(context: Context) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.qr_wechat)
        ImageSaveHelper.saveImage(
            drawable = drawable,
            context = context,
            fileName = context.getString(R.string.qr_wechat_file_name),
            viewModel = viewModel,
        )
    }


    // 打开微信扫一扫
    private fun openWechatScan() {
        LaunchHelper.launch(
            myActivity = myActivity,
            viewModel = viewModel,
            tool = Tools.AppTool.WeChat.scan,
        )
    }

}
