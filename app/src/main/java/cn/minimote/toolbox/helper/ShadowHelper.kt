/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.helper

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.FlexboxLayoutManager


object ShadowHelper {

    fun setShadow(
        recyclerView: RecyclerView,
        viewShadowTop: View,
        viewShadowBottom: View,
        viewModel: MyViewModel,
        spaceHeight: Int,
    ) {

        val layoutManager = recyclerView.layoutManager
        when(layoutManager) {
            is FlexboxLayoutManager, is GridLayoutManager, is LinearLayoutManager -> {
            }

            else -> {
                return
            }
        }

        val isWatch = viewModel.isWatch

        if(isWatch) {
            viewShadowTop.visibility = View.INVISIBLE
            viewShadowBottom.visibility = View.INVISIBLE
        }


        updateShadow(
            viewShadowTop = viewShadowTop,
            viewShadowBottom = viewShadowBottom,
            recyclerView = recyclerView,
//            isWatch = isWatch,
            spaceHeight = spaceHeight,
        )


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

//                LogHelper.e("拖动: dx = $dx, dy = $dy", "dx = $dx, dy = $dy")
//                // 手表不用更新，避免多次调用
//                if(isWatch) {
//                    return
//                }
                updateShadow(
                    viewShadowTop = viewShadowTop,
                    viewShadowBottom = viewShadowBottom,
                    recyclerView = recyclerView,
//                    isWatch = isWatch,
                    spaceHeight = spaceHeight,
                )

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when(newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        // 滚动停止
                        LogHelper.e("滚动停止", "滚动停止")
                        val inTopOrBottomFlag = updateShadow(
                            viewShadowTop = viewShadowTop,
                            viewShadowBottom = viewShadowBottom,
                            recyclerView = recyclerView,
//                            isWatch = isWatch,
                            spaceHeight = spaceHeight,
                        )
                        if(inTopOrBottomFlag) {
                            LogHelper.e("《在顶部或底部》", "《在顶部或底部》")
                            VibrationHelper.vibrateOnScrollToEdge(viewModel = viewModel)
                        }
                    }

                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // 用户开始拖动
                        LogHelper.e("开始拖动", "开始拖动")
                        updateShadow(
                            viewShadowTop = viewShadowTop,
                            viewShadowBottom = viewShadowBottom,
                            recyclerView = recyclerView,
//                            isWatch = isWatch,
                            spaceHeight = spaceHeight,
                        )
                    }

                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        // 惯性滚动
                        LogHelper.e("惯性滚动", "惯性滚动")
                    }
                }
            }

        })
    }


    fun updateShadow(
        viewShadowTop: View,
        viewShadowBottom: View,
        recyclerView: RecyclerView,
//        isWatch: Boolean,
        spaceHeight: Int,
    ): Boolean {

        val layoutManager = recyclerView.layoutManager

        val inTopFlag = inTop(layoutManager)
        val inBottomFlag = inBottom(
            layoutManager = layoutManager,
            spaceHeight = spaceHeight,
        )

//        if(isWatch) {
//            return inTopFlag || inBottomFlag
//        }

        LogHelper.e("更新阴影，在顶部${inTopFlag},在底部${inBottomFlag}", "")

        viewShadowTop.visibility = if(inTopFlag) {
            View.INVISIBLE
        } else {
//            if(inBottom(layoutManager)) {
//                View.INVISIBLE
//            }
            View.VISIBLE
        }
        viewShadowBottom.visibility = if(inBottomFlag) {
//            if(inTop(layoutManager)) {
//                LogHelper.e("显示底部阴影", "")
//                View.INVISIBLE
//            } else {
//                LogHelper.e("隐藏底部阴影1", "")
            View.INVISIBLE
//            }
        } else {
//            LogHelper.e("隐藏底部阴影2", "")
            View.VISIBLE
        }
//        一直显示是正常的，但列表项不满时就无法显示
//        viewShadowTop.visibility = View.VISIBLE
//        viewShadowBottom.visibility = View.VISIBLE
        return inTopFlag || inBottomFlag
    }


    private fun inTop(
        layoutManager: RecyclerView.LayoutManager?,
    ): Boolean {
        return when(layoutManager) {
            is FlexboxLayoutManager -> {
                val firstCompletelyVisiblePosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                if(firstCompletelyVisiblePosition == RecyclerView.NO_POSITION) {
                    val firstView = layoutManager.findViewByPosition(0)
                    firstView?.top == 0
                } else {
                    firstCompletelyVisiblePosition == 0
                }
            }

            is GridLayoutManager -> {
                val firstCompletelyVisiblePosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                if(firstCompletelyVisiblePosition == RecyclerView.NO_POSITION) {
                    val firstView = layoutManager.findViewByPosition(0)
                    firstView?.top == 0
                } else {
                    firstCompletelyVisiblePosition == 0
                }
            }

            is LinearLayoutManager -> {
                val firstCompletelyVisiblePosition =
                    layoutManager.findFirstCompletelyVisibleItemPosition()
                LogHelper.e(
                    "第一个完全显示的位置：$firstCompletelyVisiblePosition",
                    "firstView?.top == 0:${layoutManager.findViewByPosition(0)?.top}"
                )
                if(firstCompletelyVisiblePosition == RecyclerView.NO_POSITION) {
                    val firstView = layoutManager.findViewByPosition(0)
                    firstView?.top == 0
                } else {
                    firstCompletelyVisiblePosition == 0
                }
            }

            else -> {
                true
            }
        }
    }


    private fun inBottom(
        layoutManager: RecyclerView.LayoutManager?,
        spaceHeight: Int,
    ): Boolean {
        return when(layoutManager) {
            is FlexboxLayoutManager -> {
                val lastCompletelyVisiblePosition =
                    layoutManager.findLastCompletelyVisibleItemPosition()

                if(lastCompletelyVisiblePosition == RecyclerView.NO_POSITION) {
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    val lastView = layoutManager.findViewByPosition(lastVisiblePosition)
                    lastView?.bottom == layoutManager.height - spaceHeight
                } else {
                    lastCompletelyVisiblePosition == layoutManager.itemCount - 1
                }
            }

            is GridLayoutManager -> {
                val lastCompletelyVisiblePosition =
                    layoutManager.findLastCompletelyVisibleItemPosition()

                if(lastCompletelyVisiblePosition == RecyclerView.NO_POSITION) {
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    val lastView = layoutManager.findViewByPosition(lastVisiblePosition)
                    lastView?.bottom == layoutManager.height - spaceHeight
                } else {
                    lastCompletelyVisiblePosition == layoutManager.itemCount - 1
                }
            }

            is LinearLayoutManager -> {
                val lastCompletelyVisiblePosition =
                    layoutManager.findLastCompletelyVisibleItemPosition()

//                LogHelper.e(
//                    "最后一个完全显示的位置：$lastCompletelyVisiblePosition",
//                    "lastView?.bottom == layoutManager.height:${
//                        layoutManager.findViewByPosition(layoutManager.findLastVisibleItemPosition())?.bottom == layoutManager.height
//                    }"
//                )

                if(lastCompletelyVisiblePosition == RecyclerView.NO_POSITION) {
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                    val lastView = layoutManager.findViewByPosition(lastVisiblePosition)
//                    LogHelper.e(
//                        "各个高度值",
//                        "lastView?.bottom:${lastView?.bottom}, layoutManager.height:${layoutManager.height - spaceHeight}, layoutManager.paddingBottom:${layoutManager.paddingBottom}"
//                    )
                    lastView?.bottom == layoutManager.height - spaceHeight
                } else {
                    lastCompletelyVisiblePosition == layoutManager.itemCount - 1
                }
            }

            else -> {
                true
            }
        }
    }
    
}