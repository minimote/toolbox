/*
 * Copyright (c) 2024-2025 minimote(微尘). All rights reserved.
 * 本项目遵循 MIT 许可协议，请务必保留此声明和署名。
 */

package cn.minimote.toolbox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import cn.minimote.toolbox.R
import cn.minimote.toolbox.activity.MainActivity
import cn.minimote.toolbox.constant.ViewList
import cn.minimote.toolbox.constant.ViewTypes
import cn.minimote.toolbox.constant.ViewTypes.BackupAndRecovery.titleViewSet
import cn.minimote.toolbox.helper.ConfigHelper.clearUserAndBackupConfig
import cn.minimote.toolbox.helper.ConfigHelper.useRecommendedConfig
import cn.minimote.toolbox.helper.DialogHelper
import cn.minimote.toolbox.helper.FileHelper
import cn.minimote.toolbox.helper.PathHelper.getUserConfigFile
import cn.minimote.toolbox.helper.StoredToolHelper.deleteStorageFile
import cn.minimote.toolbox.helper.StoredToolHelper.getStoredFile
import cn.minimote.toolbox.helper.VibrationHelper
import cn.minimote.toolbox.viewModel.MyViewModel
import com.google.android.flexbox.FlexboxLayoutManager


class BackupAndRecoveryAdapter(
    private val myActivity: MainActivity,
    private val viewModel: MyViewModel,
) : RecyclerView.Adapter<BackupAndRecoveryAdapter.ViewHolder>() {

    private val viewList = ViewList.backupAndRecoveryViewList
    private val viewTypes = ViewTypes.BackupAndRecovery


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            if(viewModel.isWatch) {
                itemView.layoutParams.width = LayoutParams.MATCH_PARENT
            } else {
                val layoutParams = itemView.layoutParams as? FlexboxLayoutManager.LayoutParams
                layoutParams?.flexGrow = 1f
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            when(viewType) {
                in titleViewSet -> {
                    R.layout.item_setting_title
                }

                else -> {
//                    R.layout.item_my_check_update
                    R.layout.item_tool
                    // throw IllegalArgumentException("非法的视图类型：$viewType")
                }
            },
            parent, false,
        )

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder.itemViewType) {
            viewTypes.COLLECTION_TITLE -> {
                setTitle(holder = holder, titleId = R.string.collection_management)
            }


            viewTypes.SETTING_TITLE -> {
                setTitle(holder = holder, titleId = R.string.setting_management)
            }


            viewTypes.COLLECTION_IMPORT -> {
                setNormalText(
                    holder = holder,
                    titleId = R.string.import_collection,
                    drawableId = R.drawable.ic_import,
                    action = {
                        VibrationHelper.vibrateOnDangerousOperation(viewModel)

                        DialogHelper.setAndShowDefaultDialog(
                            context = myActivity,
                            viewModel = viewModel,
                            messageText = myActivity.getString(
                                R.string.confirm_import_collection,
                            ),
                            positiveAction = {
                                FileHelper.importFile(
                                    myActivity = myActivity,
                                    viewModel = viewModel,
                                    destinationFile = viewModel.getStoredFile(),
                                )
                            },
                        )
                    },
                )
            }


            viewTypes.COLLECTION_EXPORT -> {
                setNormalText(
                    holder = holder,
                    titleId = R.string.export_collection,
                    drawableId = R.drawable.ic_export,
                    action = {
                        FileHelper.exportFile(
                            myActivity = myActivity,
                            sourceFile = viewModel.getStoredFile(),
                        )
                    },
                )
            }


            viewTypes.COLLECTION_CLEAR -> {
                setNormalText(
                    holder = holder,
                    titleId = R.string.clear_collection,
                    drawableId = R.drawable.ic_trash_can,
                    action = {
                        VibrationHelper.vibrateOnDangerousOperation(viewModel)

                        DialogHelper.setAndShowDefaultDialog(
                            context = myActivity,
                            viewModel = viewModel,
                            messageText = myActivity.getString(
                                R.string.confirm_clear_collection,
                            ),
                            positiveAction = {
                                viewModel.deleteStorageFile()
                                viewModel.loadStorageActivities()
                                Toast.makeText(
                                    myActivity,
                                    myActivity.getString(
                                        R.string.clear_collection_success
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        )
                    },
                )
            }


            viewTypes.SETTING_IMPORT -> {
                setNormalText(
                    holder = holder,
                    titleId = R.string.import_setting,
                    drawableId = R.drawable.ic_import,
                    action = {
                        VibrationHelper.vibrateOnDangerousOperation(viewModel)

                        DialogHelper.setAndShowDefaultDialog(
                            context = myActivity,
                            viewModel = viewModel,
                            messageText = myActivity.getString(
                                R.string.confirm_import_setting,
                            ),
                            positiveAction = {
                                FileHelper.importFile(
                                    myActivity = myActivity,
                                    viewModel = viewModel,
                                    destinationFile = viewModel.getUserConfigFile(),
                                )
                            },
                        )
                    },
                )
            }


            viewTypes.SETTING_EXPORT -> {
                setNormalText(
                    holder = holder,
                    titleId = R.string.export_setting,
                    drawableId = R.drawable.ic_export,
                    action = {
                        FileHelper.exportFile(
                            myActivity = myActivity,
                            sourceFile = viewModel.getUserConfigFile(),
                        )
                    },
                )
            }


            viewTypes.SETTING_RECOMMEND -> {
                setNormalText(
                    holder = holder,
                    drawableId = R.drawable.ic_recommend,
                    titleId = R.string.recommend_setting,
                    action = {
                        VibrationHelper.vibrateOnDangerousOperation(viewModel)

                        DialogHelper.setAndShowDefaultDialog(
                            context = myActivity,
                            viewModel = viewModel,
                            messageText = myActivity.getString(
                                R.string.confirm_use_recommend_setting,
                            ),
                            positiveAction = {
                                viewModel.useRecommendedConfig()
                                Toast.makeText(
                                    myActivity,
                                    myActivity.getString(
                                        R.string.use_recommended_setting_success
                                    ),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                        )
                    },
                )
            }


            viewTypes.SETTING_RESTORE -> {
                setNormalText(
                    holder = holder,
                    titleId = R.string.restore_setting,
                    drawableId = R.drawable.ic_restore,
                    action = {
                        if(viewModel.userConfig.isNotEmpty()) {
                            VibrationHelper.vibrateOnDangerousOperation(viewModel)

                            DialogHelper.setAndShowDefaultDialog(
                                context = myActivity,
                                viewModel = viewModel,
                                messageText = myActivity.getString(
                                    R.string.confirm_restore_default_setting,
                                ),
                                positiveAction = {
                                    viewModel.clearUserAndBackupConfig()

                                    Toast.makeText(
                                        myActivity,
                                        myActivity.getString(
                                            R.string.restore_default_setting_success
                                        ),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                },
                            )
                        } else {
                            Toast.makeText(
                                myActivity,
                                myActivity.getString(
                                    R.string.already_default
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                )
            }
        }
    }


    // 设置标题
    private fun setTitle(
        holder: ViewHolder,
        titleId: Int = R.string.none,
    ) {
        val textViewTitle: TextView = holder.itemView.findViewById(R.id.textView_name)
        textViewTitle.text = myActivity.getString(titleId)
        if(titleId == R.string.none) {
            textViewTitle.visibility = View.GONE
        }
    }


    private fun setNormalText(
        holder: ViewHolder,
        titleId: Int,
        drawableId: Int,
        action: () -> Unit,
    ) {
        val itemView = holder.itemView

//        itemView.layoutParams = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            itemView.layoutParams.height
//        )


        val textViewTitle: TextView = itemView.findViewById(R.id.textView_app_name)
        textViewTitle.text = myActivity.getString(titleId)

        val textViewActivityName: TextView = itemView.findViewById(R.id.textView_activity_name)
        textViewActivityName.visibility = View.GONE

        val icon: ImageView = itemView.findViewById(R.id.imageView_app_icon)
        icon.setImageDrawable(
            AppCompatResources.getDrawable(
                myActivity, drawableId,
            )
        )

        itemView.setOnClickListener {
            VibrationHelper.vibrateOnClick(viewModel)
            action()
        }
    }


    override fun getItemCount(): Int = viewList.size


    override fun getItemViewType(position: Int): Int {
        return viewList[position]
    }

}
