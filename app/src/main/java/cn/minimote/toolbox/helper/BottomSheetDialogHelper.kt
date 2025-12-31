/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.adapter.BottomSheetAdapter
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


object BottomSheetDialogHelper {

    private fun getBottomSheetDialog(
        viewModel: MyViewModel,
        tool: Tool? = null,
        menuList: List<Int>,
        myActivity: MainActivity,
        onMenuItemClick: (Int) -> Unit = {}, // 回调函数
    ): BottomSheetDialog {
        val viewPager = myActivity.viewPager
        val bottomSheetDialog = BottomSheetDialog(
            myActivity,
            R.style.TranslucentBottomSheet,
        )

        val view = LayoutInflater.from(myActivity).inflate(R.layout.layout_bottom_sheet_menu, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_bottomSheet)
        recyclerView.adapter = BottomSheetAdapter(
            context = myActivity,
            viewModel = viewModel,
            myActivity = myActivity,
            tool = tool,
            bottomSheetDialog = bottomSheetDialog,
            menuList = menuList,
            onMenuItemClick = onMenuItemClick,
        )
        recyclerView.layoutManager = LinearLayoutManager(myActivity)

        bottomSheetDialog.setContentView(view)
        // 允许点击外部关闭
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        // 半透明遮罩
        bottomSheetDialog.window?.apply {
            setBackgroundDrawable(R.color.black.toDrawable()) // 黑色背景
            setDimAmount(UI.Alpha.ALPHA_7) // 透明度
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // 启用遮罩层
        }


        viewPager.apply {
            // 打开的时候禁用 ViewPaper 的滑动
            bottomSheetDialog.setOnShowListener {
                viewPager.isUserInputEnabled = false
            }

            // 滑动或点击外部取消的时候恢复 ViewPager 的滑动
            bottomSheetDialog.setOnCancelListener {
                viewPager.isUserInputEnabled = true
            }
        }

        return bottomSheetDialog
    }


    fun setAndShowBottomSheetDialog(
        viewModel: MyViewModel,
        activity: MainActivity,
        tool: Tool? = null,
        menuList: List<Int>,
        onMenuItemClick: (Int) -> Unit = {}, // 回调函数
    ): BottomSheetDialog {
        val bottomSheetDialog = getBottomSheetDialog(
            viewModel = viewModel,
            tool = tool,
            menuList = menuList,
            myActivity = activity,
            onMenuItemClick = onMenuItemClick,
        )
        bottomSheetDialog.show()
        return bottomSheetDialog
    }


}