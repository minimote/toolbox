/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.widget.ImageView
import android.widget.Toast
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.MenuList
import cn.minimote.toolbox.constant.MenuType
import cn.minimote.toolbox.helper.IconHelper.toBitmap
import cn.minimote.toolbox.viewModel.MyViewModel
import java.io.File
import java.io.FileOutputStream


object ImageSaveHelper {


    fun ImageView.setSavePopupMenuListener(
        fileName: String,
        viewModel: MyViewModel,
        myActivity: MainActivity,
        drawable: Drawable = this.drawable,
        quality: Int = 100,
        imagePath: File = viewModel.savePath,
    ) {

        // 禁用振动反馈
        this.isHapticFeedbackEnabled = false
        this.setOnLongClickListener {
            VibrationHelper.vibrateOnLongPress(viewModel)
            setMenu(
                drawable = drawable,
                fileName = fileName,
                viewModel = viewModel,
                activity = myActivity,
                quality = quality,
                imagePath = imagePath,
            )
            true
        }
    }


    // 显示弹窗
    private fun setMenu(
        drawable: Drawable,
        fileName: String,
        viewModel: MyViewModel,
        activity: MainActivity,
        menuList: List<Int> = MenuList.saveImage,
        quality: Int = 100,
        imagePath: File = viewModel.savePath,
    ) {
        BottomSheetDialogHelper.setAndShowBottomSheetDialog(
            viewModel = viewModel,
            activity = activity,
            menuList = menuList,
            onMenuItemClick = { menuItemId ->
                when(menuItemId) {
                    MenuType.SAVE_IMAGE, MenuType.SAVE_ICON -> {
                        saveImage(
                            drawable = drawable,
                            fileName = fileName,
                            viewModel = viewModel,
                            context = activity,
                            quality = quality,
                            imagePath = imagePath,
                        )
                    }
                }
            }
        )
    }


//    private fun showPopupMenu(
//        imageView: ImageView,
//        fileName: String,
//        viewModel: MyViewModel,
//        context: Context,
//        quality: Int = 100,
//        imagePath: File = viewModel.savePath,
//        x: Int,
//        y: Int,
//    ) {
//        // 创建 PopupWindow
//        val popupView =
//            LayoutInflater.from(context).inflate(R.layout.popup_menu_layout_save_image, null)
//        val popupWindow = PopupWindow(
//            popupView,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            true
//        )
//
//        // 设置菜单项点击事件
//        val textViewSave = popupView.findViewById<TextView>(R.id.save_image)
//        textViewSave.setOnClickListener {
//            VibrationHelper.vibrateOnClick(viewModel)
//            saveImage(
//                imageView = imageView,
//                fileName = fileName,
//                viewModel = viewModel,
//                context = context,
//                quality = quality,
//                imagePath = imagePath,
//            )
//            popupWindow.dismiss()
//        }
//
//        // 显示 PopupWindow
//        popupWindow.showAtLocation(imageView, Gravity.NO_GRAVITY, x, y)
//
//    }


    fun saveImage(
        drawable: Drawable,
        fileName: String,
        viewModel: MyViewModel,
        context: Context,
        quality: Int = 100,
        imagePath: File = viewModel.savePath,
    ) {

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
                drawable.toBitmap().compress(
                    Bitmap.CompressFormat.PNG, quality, out
                )

                Toast.makeText(
                    context,
                    context.getString(
                        R.string.has_been_saved_to, file,
                    ),
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
                context.getString(
                    R.string.image_save_failed,
                    e.message,
                ),
                Toast.LENGTH_LONG,
            ).show()
        }
    }


}
