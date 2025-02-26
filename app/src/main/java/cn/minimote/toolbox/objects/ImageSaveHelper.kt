/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.objects

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import cn.minimote.toolbox.R
import cn.minimote.toolbox.viewModel.ToolboxViewModel
import java.io.File
import java.io.FileOutputStream


object ImageSaveHelper {

    fun setPopupMenu(
        imageView: ImageView,
        fileName: String,
        viewModel: ToolboxViewModel,
        context: Context,
        quality: Int = 100,
        imagePath: File = viewModel.savePath,
    ) {

        var x = viewModel.screenWidth / 2
        var y = viewModel.screenHeight / 2
        imageView.setOnLongClickListener {
            VibrationHelper.vibrateOnClick(context)
            showPopupMenu(
                imageView,
                fileName,
                viewModel,
                context,
                quality,
                imagePath,
                x,
                y,
            )
            true
        }

        imageView.setOnTouchListener { view, event ->
            if(event.action == MotionEvent.ACTION_DOWN) {
                x = event.rawX.toInt()
                y = event.rawY.toInt()
//                view.performLongClick()
            }
            false
        }
//

//        val gestureDetector =
//            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
//                override fun onLongPress(e: MotionEvent) {
//                    VibrationHelper.vibrateOnClick(context)
//                    showPopupMenu(
//                        imageView,
//                        fileName,
//                        viewModel,
//                        context,
//                        quality,
//                        imagePath,
//                        e.rawX.toInt(),
//                        e.rawY.toInt()
//                    )
//                }
//
////                override fun onSingleTapUp(e: MotionEvent): Boolean {
////                    // 处理单击事件
////                    return true
////                }
//            })

//        imageView.setOnTouchListener { view, event ->
//            gestureDetector.onTouchEvent(event)
//        }

    }


    private fun showPopupMenu(
        imageView: ImageView,
        fileName: String,
        viewModel: ToolboxViewModel,
        context: Context,
        quality: Int = 100,
        imagePath: File = viewModel.savePath,
        x: Int,
        y: Int,
    ) {
        // 创建 PopupWindow
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_menu_layout_save_image, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // 设置菜单项点击事件
        val saveButton = popupView.findViewById<Button>(R.id.save_image)
        saveButton.setOnClickListener {
            VibrationHelper.vibrateOnClick(context)
            saveImage(
                imageView,
                fileName,
                viewModel,
                context,
                quality,
                imagePath,
            )
            popupWindow.dismiss()
        }

        // 显示 PopupWindow
        popupWindow.showAtLocation(imageView, Gravity.NO_GRAVITY, x, y)

    }


    fun saveImage(
        imageView: ImageView,
        fileName: String,
        viewModel: ToolboxViewModel,
        context: Context,
        quality: Int = 100,
        imagePath: File = viewModel.savePath,
    ) {
        // 从 imageView 中获取图片
        val drawable = imageView.drawable

        // 检查文件夹是否存在，如果不存在则创建
        if(!imagePath.exists()) {
            if(!imagePath.mkdirs()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.unable_to_create_folder),
                    Toast.LENGTH_SHORT,
                ).show()
                return
            }
        }

        // 确保文件名以 .png 结尾
        val correctedFileName = if(fileName.endsWith(".png", ignoreCase = true)) {
            fileName + ""
        } else {
            "$fileName.png"
        }

//        Log.e("下载路径", "$imagePath")
        val file = File(imagePath, correctedFileName)

        // 将 bitmap 保存为文件
        try {
            FileOutputStream(file).use { out ->
                drawable?.toBitmap()?.compress(Bitmap.CompressFormat.PNG, quality, out)

                Toast.makeText(
                    context,
                    context.getString(R.string.has_been_saved_to, imagePath),
                    Toast.LENGTH_LONG,
                ).show()
            }

            // 通知系统扫描新文件
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null,
            ) { _, _ ->
//                Log.i("ExternalStorage", "Scanned $path:")
//                Log.i("ExternalStorage", "-> uri=$uri")
            }
        } catch(e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                context.getString(R.string.image_save_failed),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

}