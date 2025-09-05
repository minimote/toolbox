/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.ui.widget

//class MaxHeightScrollView : ScrollView {
//    private var maxHeight: Int = 0
//
//    constructor(context: Context) : super(context) {
//        init(context, null)
//    }
//
//    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//        init(context, attrs)
//    }
//
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        init(context, attrs)
//    }
//
//    private fun init(context: Context, attrs: AttributeSet?) {
//        if (attrs != null) {
//            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
//            maxHeight = typedArray.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, 0)
//            typedArray.recycle()
//        }
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        var heightMeasureSpec = heightMeasureSpec
//        if (maxHeight > 0) {
//            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//            if (heightSize > maxHeight) {
//                val atMost = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
//                super.onMeasure(widthMeasureSpec, atMost)
//                return
//            }
//        }
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//    }
//
//    fun setMaxHeight(maxHeight: Int) {
//        this.maxHeight = maxHeight
//        requestLayout()
//        invalidate()
//    }
//}
