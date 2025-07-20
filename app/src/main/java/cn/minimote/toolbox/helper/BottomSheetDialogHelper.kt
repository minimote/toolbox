/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.minimote.toolbox.R
import cn.minimote.toolbox.adapter.BottomSheetAdapter
import cn.minimote.toolbox.constant.UI
import cn.minimote.toolbox.dataClass.Tool
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog


object BottomSheetDialogHelper {

    private fun getBottomSheetDialog(
        context: Context,
        viewModel: MyViewModel,
        tool: Tool? = null,
        menuList: List<Int>,
        fragmentManager: FragmentManager? = null,
        viewPager: ViewPager2? = null,
        constraintLayoutOrigin: ConstraintLayout? = null,
        onMenuItemClick: (Int) -> Unit = {}, // 回调函数
    ): BottomSheetDialog {
        val bottomSheetDialog = BottomSheetDialog(
            context,
            R.style.TranslucentBottomSheet,
        )

        val view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_menu, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_bottomSheet)
        recyclerView.adapter = BottomSheetAdapter(
            context = context,
            viewModel = viewModel,
            tool = tool,
            bottomSheetDialog = bottomSheetDialog,
            menuList = menuList,
            fragmentManager = fragmentManager,
            viewPager = viewPager,
            constraintLayoutOrigin = constraintLayoutOrigin,
            onMenuItemClick = onMenuItemClick,
        )
        recyclerView.layoutManager = LinearLayoutManager(context)

        bottomSheetDialog.setContentView(view)
        // 允许点击外部关闭
        bottomSheetDialog.setCanceledOnTouchOutside(true)

        // 半透明遮罩
        bottomSheetDialog.window?.apply {
            setBackgroundDrawable(R.color.black.toDrawable()) // 黑色背景
            setDimAmount(UI.ALPHA_7) // 透明度
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // 启用遮罩层
        }


        viewPager?.apply {
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
        context: Context,
        viewModel: MyViewModel,
        tool: Tool? = null,
        menuList: List<Int>,
        fragmentManager: FragmentManager? = null,
        viewPager: ViewPager2? = null,
        constraintLayoutOrigin: ConstraintLayout? = null,
        onMenuItemClick: (Int) -> Unit = {}, // 回调函数
    ): BottomSheetDialog {
        val bottomSheetDialog = getBottomSheetDialog(
            context = context,
            viewModel = viewModel,
            tool = tool,
            menuList = menuList,
            viewPager = viewPager,
            fragmentManager = fragmentManager,
            constraintLayoutOrigin = constraintLayoutOrigin,
            onMenuItemClick = onMenuItemClick,
        )
        bottomSheetDialog.show()
        return bottomSheetDialog
    }


}