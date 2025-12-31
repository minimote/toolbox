/*
 * Copyright (c) 2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.helper.DimensionHelper.getLayoutSize
import cn.minimote.toolbox.helper.IconHelper.toCircularDrawable
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.material.switchmaterial.SwitchMaterial


class EasterEggAdapter(
    private val myActivity: MainActivity,
    val viewModel: MyViewModel,
) : RecyclerView.Adapter<EasterEggAdapter.ViewHolder>() {


    inner class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
//
    }


    override fun getItemCount(): Int = dataList.size


    override fun getItemViewType(position: Int): Int {
        return when(dataList[position]) {
            title -> {
                ViewTypes.TITLE
            }

            switchOnlySwitch -> {
                ViewTypes.SWITCH_ONLY_SWITCH
            }

            switchOnlySwitchVibration -> {
                ViewTypes.SWITCH_ONLY_SWITCH_VIBRATION
            }

            switchMine -> {
                ViewTypes.SWITCH_MINE
            }

            enterOnlyIcon -> {
                ViewTypes.ENTER_ONLY_ICON
            }

            enterMine -> {
                ViewTypes.ENTER_MINE
            }

            gapLine -> {
                ViewTypes.GAP_LINE
            }

            else -> {
                ViewTypes.TEXT
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = when(viewType) {
            ViewTypes.TITLE -> R.layout.item_easteregg_title
            ViewTypes.TEXT -> R.layout.item_easteregg_text
            in switchSet -> R.layout.item_installed_app
            in enterSet -> R.layout.item_my_setting
            ViewTypes.GAP_LINE -> R.layout.item_tool_separator
            else -> -1
        }
        val view = LayoutInflater.from(myActivity).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when(holder.itemViewType) {
            ViewTypes.TITLE, ViewTypes.TEXT -> {
                setTextView(holder, dataList[position])
            }

            in switchSet -> {
                setSwitch(holder, dataList[position])
            }

            in enterSet -> {
                setEnter(holder, dataList[position])
            }

            ViewTypes.GAP_LINE -> {
                val view = holder.itemView
                val viewLine = view.findViewById<View>(R.id.view_line)

                val params = viewLine.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = getLayoutSize(
                    myActivity, R.dimen.layout_size_1_5_small
                )
                params.bottomMargin = params.topMargin
                viewLine.layoutParams = params
            }
        }

    }


    private fun setTextView(holder: ViewHolder, text: String) {
        holder.itemView.findViewById<TextView>(R.id.textView_content).text = text
    }


    private fun setSwitch(holder: ViewHolder, text: String) {
        val view = holder.itemView
        val type = holder.itemViewType
        val title = view.findViewById<TextView>(R.id.textView_app_name)
        val switch = view.findViewById<SwitchMaterial>(R.id.switch_whether_show_in_home)
        val container = view.findViewById<ConstraintLayout>(R.id.constraintLayout_container)
        val imageViewAppIcon = view.findViewById<ImageView>(R.id.imageView_app_icon)

        title.text = text
        imageViewAppIcon.setImageDrawable(
            ContextCompat.getDrawable(
                myActivity,
                R.mipmap.ic_launcher,
            )?.toCircularDrawable(myActivity)
        )

        when(type) {
            ViewTypes.SWITCH_ONLY_SWITCH -> {
                container.background = null
//                container.background = ContextCompat.getDrawable(
//                    myActivity, R.drawable.background_round_stroke,
//                )

                switch.setOnClickListener {
                    // SwitchMaterial 会自动切换状态
                }
            }

            ViewTypes.SWITCH_ONLY_SWITCH_VIBRATION -> {
                container.background = null
//                container.background = ContextCompat.getDrawable(
//                    myActivity, R.drawable.background_round_stroke,
//                )

                view.setOnClickListener {
                    switch.isChecked = !switch.isChecked
                }
                switch.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                }
            }

            ViewTypes.SWITCH_MINE -> {
//                switchContainer.background = ContextCompat.getDrawable(
//                    myActivity, R.drawable.background_ripple_round_stroke,
//                )
                view.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    switch.isChecked = !switch.isChecked
                }
            }
        }
    }


    private fun setEnter(holder: ViewHolder, text: String) {
        val view = holder.itemView
        val type = holder.itemViewType
        val title = view.findViewById<TextView>(R.id.textView_name)
        val container = view.findViewById<ConstraintLayout>(R.id.constraintLayout_container)

        fun pretendEnter() {
            Toast.makeText(
                myActivity,
                "假装进入新页面",
                Toast.LENGTH_SHORT
            ).show()
        }


        title.text = text
        when(type) {
            ViewTypes.ENTER_ONLY_ICON -> {
                container.background = null
//                container.background = ContextCompat.getDrawable(
//                    myActivity, R.drawable.background_round_stroke,
//                )
                val icon = view.findViewById<View>(R.id.imageView_enter)

                val layoutParams = icon.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = getLayoutSize(
                    myActivity, R.dimen.layout_size_1_5_small
                )

                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET

//                layoutParams.width /= 4
//                layoutParams.width *= 3
//                layoutParams.height /= 4
//                layoutParams.height *= 3
                layoutParams.width = getLayoutSize(
                    myActivity, R.dimen.layout_size_1_5_small
                )
                layoutParams.height = layoutParams.width

                icon.layoutParams = layoutParams

                icon.setOnClickListener {
                    pretendEnter()
                }
            }

            ViewTypes.ENTER_MINE -> {
//                container.background = ContextCompat.getDrawable(
//                    myActivity, R.drawable.background_ripple_round_stroke,
//                )

                view.setOnClickListener {
                    VibrationHelper.vibrateOnClick(viewModel)
                    pretendEnter()
                }
            }
        }

    }


    private val blank = myActivity.getString(R.string.blank)
    private val indent = blank.repeat(2)
    private val newLine = "\n"
    private val gapLine = "分隔线"

    private val title = "写给自己，也写给你"

    private val switchOnlySwitch = "只能点开关切换"
    private val switchOnlySwitchVibration = "只有开关有震动"
    private val switchMine = "我的开关"

    private val enterOnlyIcon = "只能点图标进入"
    private val enterMine = "我的菜单布局"


    object ViewTypes {
        const val TITLE = 0
        const val TEXT = 1

        const val SWITCH_ONLY_SWITCH = 2
        const val SWITCH_ONLY_SWITCH_VIBRATION = 3
        const val SWITCH_MINE = 4

        const val ENTER_ONLY_ICON = 5
        const val ENTER_MINE = 6

        const val GAP_LINE = 7
    }


    private val switchSet = setOf(
        ViewTypes.SWITCH_ONLY_SWITCH,
        ViewTypes.SWITCH_ONLY_SWITCH_VIBRATION,
        ViewTypes.SWITCH_MINE,
    )


    private val enterSet = setOf(
        ViewTypes.ENTER_ONLY_ICON,
        ViewTypes.ENTER_MINE,
    )


    private val dataList = listOf(
        title,
        indent + "你好啊，我是软件的作者微尘，恭喜你发现了我留下的彩蛋。我有一些话想说，但又不想太直白地直接说出来，所以我把它们放在了这里，等待有缘人的发现。受限于语文表达水平，可能会有些语句不通顺或者词不达意，希望你理解。",

        indent + "一开始想做这个软件，是因为我的手表只有一个实体按钮可以设置快捷键，可以双击打开一个自定义软件或功能，但我想快速打开的软件比较多，比如音乐、付款码、最近任务等，所以我就想做一个软件，把这些都集中在一个软件里面，然后可以设置双击打开这个软件，然后再打开想要的功能或软件。当然，后来我玩到了欧加的新款手表，发现它的两个按键都能自定义，有的还能设置单击和双击，一共能快速打开三四个软件，所以如果我一开始就用的是新表的话，可能就不会有这个软件了。",

        indent + "软件轮廓逐渐丰富之后，我觉得不应该止步于此，所以又增加了一些自定义功能，未来还会增加一些实用的工具，就像软件的名字一样，真正做成一个「工具箱」。我想做这个工具箱，不是因为想造福人类，主要只是想方便我自己而已，如果同时也能给你提供一些便利，那我会十分荣幸。",

        indent + "在做这个软件的过程中，或者说在平时用其他软件的时候，就发现了其他软件的一些非常不友好的操作逻辑。",

        indent + "比如很常见的左侧文字右侧开关的布局，就有一些对用户不太友好的操作逻辑。比如有些只能点击开关图标才能切换状态。",

        gapLine,
        switchOnlySwitch,
        gapLine,

        indent + "还有一些一整行都可以点击切换，但只有点击开关图标才有振动反馈。",

        gapLine,
        switchOnlySwitchVibration,
        gapLine,

        indent + "所以我在做开关布局时，就设置了一整行都可以点击切换状态，并且有振动反馈(如果你在设置中开启了振动的话)，还有一整行的点击涟漪效果。",

        gapLine,
        switchMine,
        gapLine,

        indent + "类似的还有进入二级页面的菜单布局。有的只能点击最右侧的图标才可以进入二级页面。并且图标还设计的非常小，还没有垂直居中。",

        gapLine,
        enterOnlyIcon,
        gapLine,

        indent + "所以我的菜单布局就设置为一整行都可以点击，并且有振动反馈和一整行的点击涟漪效果。",

        gapLine,
        enterMine,
        gapLine,

        indent + "虽然都不是大问题，但我感觉这些东西非常影响用户体验，所以在我的软件里，就要在能力范围内尽量做到最好。",

        indent + "在开发过程中，也切身体会到了开发者的不容易。如何平衡性能、美观和体验，这是一个非常复杂的问题。我们常常说某些软件占用的内存或者存储比较多，但当你称为开发者就会发现，有些东西确实很占资源，预先缓存才能提高用户体验，不缓存的话加载时间长，缓存多了就会占用过多资源。当然，也有一些软件滥用缓存，用户体验有没有提升不好说，但资源确实占用了很多。",

        indent + "还有设置项的问题。设置项太少，用户会嫌自定义程度低，不给选择的权利；设置项太多，那就变成了「开关 OS」，稳定性很难保证。",

        indent + "在开发这个软件的过程中，我也参考了一些其他软件的设计，使用了一些优秀的第三方库，我觉得这是一个学习和进步的过程，在参考过程中也加入了自己的思考。正是有了这些优秀的开源项目和开源精神，我们才能站在巨人的肩膀上，创造出更好的产品，所以我也选择将本项目开源，希望可以给其他人提供一些参考。",

        indent + "最后我想用 ColorOS 设计总监陈希老师的一段话来结尾：「学习他人是为了演进自己，而不是成为他人。我喜欢的作家余华说过：『树木在成长时需要阳光的照耀，但是最重要的一点是，树木在阳光下照耀成长的时候，是以树木的方式在成长，不是以阳光的方式在成长。』它还是大树的样子，而不应该是太阳的样子，学习如是，做产品如是。」",

        indent + "感谢你读到这里。如果你有什么想和我说的，欢迎通过 Gitee 联系我。" + newLine,
    )


}
