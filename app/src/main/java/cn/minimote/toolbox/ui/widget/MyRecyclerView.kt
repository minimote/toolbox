/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

//package cn.minimote.toolbox.ui.widget
//
//import android.animation.Animator
//import android.animation.AnimatorSet
//import android.animation.ObjectAnimator
//import android.animation.ValueAnimator
//import android.annotation.SuppressLint
//import android.content.Context
//import android.util.AttributeSet
//import android.util.Property
//import android.view.MotionEvent
//import android.view.View
//import android.view.animation.DecelerateInterpolator
//import android.view.animation.Interpolator
//import androidx.recyclerview.widget.RecyclerView
//import kotlin.math.abs
//
//
//// 支持越界回弹的 RecyclerView
//open class MyRecyclerView(context: Context, attrs: AttributeSet?, defStyle: Int) :
//    RecyclerView(context, attrs, defStyle), View.OnTouchListener {
//    // 保存起始触摸行为参数（用于判断滑动方向）
//    private val mStartAttr = OverScrollStartAttributes()
//    private var mVelocity = 0f // 当前滑动速度
//    private val mRecyclerView: RecyclerView = this // 持有当前 RecyclerView 的引用
//
//    // 状态模式管理：闲置、滚动中、回弹动画状态
//    private var mCurrentState: IDecoratorState
//    private var mIdleState: IdleState
//    private var mOverScrollingState: OverScrollingState
//    private var mBounceBackState: BounceBackState
//
//    companion object {
//        // 下拉与上拉的拖拽比例（数值越大视图移动越慢）
//        private const val DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD = 1f // 下拉时的拖拽比
//        private const val DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK = 1f // 上拉时的拖拽比
//
//        // 默认减速系数
//        private const val DEFAULT_DECELERATE_FACTOR = 1f
//
//        // 回弹动画持续时间范围（毫秒）
//        private const val MIN_BOUNCE_BACK_DURATION_MS = 200
//        private const val MAX_BOUNCE_BACK_DURATION_MS = 400
//    }
//
//    constructor(context: Context) : this(context, null)
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
//
//    /**
//     * 初始化参数及状态实例
//     */
//    init {
//        mBounceBackState = BounceBackState()
//        mOverScrollingState = OverScrollingState()
//        mCurrentState = IdleState().apply { mIdleState = this }
//        attach()
//    }
//
//
//    /**
//     * 触摸事件处理
//     */
//    override fun onTouch(v: View, event: MotionEvent): Boolean {
//
//        return when(event.action) {
//            MotionEvent.ACTION_MOVE -> mCurrentState.handleMoveTouchEvent(event)
//            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> mCurrentState.handleUpTouchEvent(
//                event
//            )
//
//            else -> false
//        }
//    }
//
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        attach() // 重新绑定触摸事件
//        // 可选：重置到空闲状态
//        mCurrentState = mIdleState
//    }
//
//    /**
//     * 当 View 被从窗口分离时调用，清理资源
//     */
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        detach()
//    }
//
//    /**
//     * 启用自定义 Touch 监听器并禁用系统默认的 Overscroll 效果
//     */
//    @SuppressLint("ClickableViewAccessibility")
//    fun attach() {
//        mRecyclerView.setOnTouchListener(this)
//        mRecyclerView.overScrollMode = OVER_SCROLL_NEVER
//    }
//
//    /**
//     * 取消绑定 Touch 监听器并恢复系统默认的 Overscroll 效果
//     */
//    @SuppressLint("ClickableViewAccessibility")
//    fun detach() {
//        mRecyclerView.setOnTouchListener(null)
//        mRecyclerView.overScrollMode = OVER_SCROLL_ALWAYS
//        // 移除状态引用
//        mCurrentState = mIdleState
//        clearAnimation() // 清除所有动画
//        clearFocus() // 清除焦点
//    }
//
//
//    /**
//     * 状态切换函数
//     */
//    private fun issueStateTransition(state: IDecoratorState) {
//        val oldState = mCurrentState
//        mCurrentState = state
//        // 处理状态切换时的动画过渡
//        mCurrentState.handleTransitionAnim(oldState)
//    }
//
//    /**
//     * 存储 Motion 事件涉及的属性（如偏移量、方向等）
//     */
//    protected class MotionAttributes {
//        var mAbsOffset = 0f // 绝对偏移量
//        var mDeltaOffset = 0f // 移动增量
//        var mDir = false // 滑动方向（true 下拉，false 上拉）
//    }
//
//    /**
//     * 记录初始 Touch 行为信息
//     */
//    protected class OverScrollStartAttributes {
//        var mPointerId = 0 // 手势 ID
//        var mAbsOffset = 0f // 初始偏移量
//        var mDir = false // 初始方向
//    }
//
//    /**
//     * 动画相关属性包装类
//     */
//    protected class AnimationAttributes {
//        var mProperty: Property<View, Float>? = null // 动画属性（如 translationY）
//        var mAbsOffset = 0f // 当前绝对偏移值
//        var mMaxOffset = 0f // 最大偏移阈值
//    }
//
//    /**
//     * 初始化动画属性
//     */
//    private fun initAnimationAttributes(view: View, attributes: AnimationAttributes) {
//        attributes.mProperty = TRANSLATION_Y
//        attributes.mAbsOffset = view.translationY
//        attributes.mMaxOffset = view.height.toFloat()
//    }
//
//    /**
//     * 初始化 Motion 属性并检测是否为有效竖直滑动
//     */
//    private fun initMotionAttributes(
//        view: View,
//        attributes: MotionAttributes,
//        event: MotionEvent
//    ): Boolean {
//        if(event.historySize == 0) {
//            return false
//        }
//
//        val dy = event.getY(0) - event.getHistoricalY(0, 0)
//        val dx = event.getX(0) - event.getHistoricalX(0, 0)
//
//        if(abs(dy.toDouble()) < abs(dx.toDouble())) {
//            return false // 非垂直滑动，忽略
//        }
//
//        attributes.mAbsOffset = view.translationY
//        attributes.mDeltaOffset = dy
//        attributes.mDir = attributes.mDeltaOffset > 0
//
//        return true
//    }
//
//    /**
//     * 是否处于 RecyclerView 顶部
//     */
//    private fun isInAbsoluteStart(view: View): Boolean {
//        return !view.canScrollVertically(-1)
//    }
//
//    /**
//     * 是否处于 RecyclerView 底部
//     */
//    private fun isInAbsoluteEnd(view: View): Boolean {
//        return !view.canScrollVertically(1)
//    }
//
//    /**
//     * 设置 View 的 Y 轴平移偏移量
//     */
//    private fun translateView(view: View, offset: Float) {
//        view.translationY = offset
//    }
//
//    /**
//     * 设置 View 平移并调整 MotionEvent 坐标（用于防止事件跳跃）
//     */
//    private fun translateViewAndEvent(view: View, offset: Float, event: MotionEvent) {
//        view.translationY = offset
//        event.offsetLocation(0f, offset - event.getY(0))
//    }
//
//    /**
//     * 状态接口：定义三种状态的行为规范
//     */
//    interface IDecoratorState {
//        fun handleMoveTouchEvent(event: MotionEvent): Boolean
//        fun handleUpTouchEvent(event: MotionEvent): Boolean
//        fun handleTransitionAnim(fromState: IDecoratorState)
//    }
//
//    /**
//     * 空闲状态：未发生任何拖拽动作的状态
//     */
//    inner class IdleState : IDecoratorState {
//        private val mMoveAttr = MotionAttributes()
//
//        override fun handleMoveTouchEvent(event: MotionEvent): Boolean {
//            if(!initMotionAttributes(mRecyclerView, mMoveAttr, event)) return false
//
//            if(!(isInAbsoluteStart(mRecyclerView) && mMoveAttr.mDir || isInAbsoluteEnd(mRecyclerView) && !mMoveAttr.mDir)) {
//                return false
//            }
//
//            mStartAttr.mPointerId = event.getPointerId(0)
//            mStartAttr.mAbsOffset = mMoveAttr.mAbsOffset
//            mStartAttr.mDir = mMoveAttr.mDir
//
//            issueStateTransition(mOverScrollingState)
//            return mOverScrollingState.handleMoveTouchEvent(event)
//        }
//
//        override fun handleUpTouchEvent(event: MotionEvent): Boolean = false
//
//        override fun handleTransitionAnim(fromState: IDecoratorState) {}
//    }
//
//    /**
//     * 滚动状态：正在拖拽中
//     */
//    inner class OverScrollingState : IDecoratorState {
//        private val mTouchDragRatioFwd: Float = DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD
//        private val mTouchDragRatioBck: Float = DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK
//        private val mMoveAttr: MotionAttributes = MotionAttributes()
//
//        override fun handleMoveTouchEvent(event: MotionEvent): Boolean {
//            val startAttr = mStartAttr
//            if(startAttr.mPointerId != event.getPointerId(0)) {
//                issueStateTransition(mBounceBackState)
//                return true
//            }
//
//            val view = mRecyclerView
//            if(!initMotionAttributes(view, mMoveAttr, event)) return true
//
//            val deltaOffset =
//                mMoveAttr.mDeltaOffset / if(mMoveAttr.mDir == startAttr.mDir) mTouchDragRatioFwd else mTouchDragRatioBck
//            val newOffset = mMoveAttr.mAbsOffset + deltaOffset
//
//            if((startAttr.mDir && !mMoveAttr.mDir && (newOffset <= startAttr.mAbsOffset)) ||
//                (!startAttr.mDir && mMoveAttr.mDir && (newOffset >= startAttr.mAbsOffset))
//            ) {
//                translateViewAndEvent(view, startAttr.mAbsOffset, event)
//                issueStateTransition(mIdleState)
//                return true
//            }
//
//            view.parent?.requestDisallowInterceptTouchEvent(true)
//            val dt = event.eventTime - event.getHistoricalEventTime(0)
//            if(dt > 0) mVelocity = deltaOffset / dt
//            translateView(view, newOffset)
//            return true
//        }
//
//        override fun handleUpTouchEvent(event: MotionEvent): Boolean {
//            issueStateTransition(mBounceBackState)
//            return false
//        }
//
//        override fun handleTransitionAnim(fromState: IDecoratorState) {}
//    }
//
//    /**
//     * 回弹状态：使用 ValueAnimator 实现弹性动画
//     */
//    inner class BounceBackState : IDecoratorState, Animator.AnimatorListener,
//        ValueAnimator.AnimatorUpdateListener {
//
//        private val mBounceBackInterpolator: Interpolator = DecelerateInterpolator()
//        private val mDecelerateFactor: Float = DEFAULT_DECELERATE_FACTOR
//        private val mDoubleDecelerateFactor: Float = 2f * DEFAULT_DECELERATE_FACTOR
//        private val mAnimAttributes: AnimationAttributes = AnimationAttributes()
//        private val view = mRecyclerView
//
//
//        override fun handleTransitionAnim(fromState: IDecoratorState) {
//            val bounceBackAnim = createAnimator()
//            bounceBackAnim.addListener(this)
//            bounceBackAnim.start()
//        }
//
//        override fun onAnimationEnd(animation: Animator) {
//            issueStateTransition(mIdleState)
//        }
//
//        private fun createAnimator(): Animator {
//            initAnimationAttributes(view, mAnimAttributes)
//            if(mVelocity == 0f || (mVelocity < 0 && mStartAttr.mDir) || (mVelocity > 0 && !mStartAttr.mDir)) {
//                return createBounceBackAnimator(mAnimAttributes.mAbsOffset)
//            }
//
//            var slowdownDuration = (0 - mVelocity) / mDecelerateFactor
//            slowdownDuration = if(slowdownDuration < 0) 0f else slowdownDuration
//
//            val slowdownDistance = -mVelocity * mVelocity / mDoubleDecelerateFactor
//            val slowdownEndOffset = mAnimAttributes.mAbsOffset + slowdownDistance
//
//            val slowdownAnim =
//                createSlowdownAnimator(view, slowdownDuration.toInt(), slowdownEndOffset)
//            val bounceBackAnim = createBounceBackAnimator(slowdownEndOffset)
//            val wholeAnim = AnimatorSet()
//            wholeAnim.playSequentially(slowdownAnim, bounceBackAnim)
//            return wholeAnim
//        }
//
//        private fun createSlowdownAnimator(
//            view: View,
//            slowdownDuration: Int,
//            slowdownEndOffset: Float
//        ): ObjectAnimator {
//            val slowdownAnim =
//                ObjectAnimator.ofFloat(view, mAnimAttributes.mProperty, slowdownEndOffset)
//            slowdownAnim.duration = slowdownDuration.toLong()
//            slowdownAnim.interpolator = mBounceBackInterpolator
//            slowdownAnim.addUpdateListener(this)
//            return slowdownAnim
//        }
//
//        private fun createBounceBackAnimator(startOffset: Float): ObjectAnimator {
//            val bounceBackDuration =
//                (abs(startOffset) / mAnimAttributes.mMaxOffset) * MAX_BOUNCE_BACK_DURATION_MS
//            val bounceBackAnim =
//                ObjectAnimator.ofFloat(view, mAnimAttributes.mProperty, mStartAttr.mAbsOffset)
//            bounceBackAnim.duration =
//                bounceBackDuration.toInt().coerceAtLeast(MIN_BOUNCE_BACK_DURATION_MS).toLong()
//            bounceBackAnim.interpolator = mBounceBackInterpolator
//            bounceBackAnim.addUpdateListener(this)
//            return bounceBackAnim
//        }
//
//        override fun handleMoveTouchEvent(event: MotionEvent): Boolean = true
//        override fun handleUpTouchEvent(event: MotionEvent): Boolean = true
//        override fun onAnimationUpdate(animation: ValueAnimator) {}
//        override fun onAnimationStart(animation: Animator) {}
//        override fun onAnimationCancel(animation: Animator) {}
//        override fun onAnimationRepeat(animation: Animator) {}
//    }
//
//}