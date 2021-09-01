package com.addx.ai.demo

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.addx.common.ui.FlowLayout
import com.addx.common.utils.LogUtils
import com.addx.common.utils.SizeUtils
import com.ai.addx.model.RecordBean
import com.ai.addxbase.DeviceClicent
import com.ai.addxbase.tagInfos
import com.ai.addxbase.util.TimeUtils
import com.ai.addxbase.view.GridSpacingItemDecoration
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import java.util.*

class PirListActivity : BaseActivity() {
    private var list: ArrayList<RecordBean>? = null

    override fun getResid(): Int {
        return R.layout.activity_pir_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = ArrayList()

        loadData()

        findViewById<RecyclerView>(R.id.pir_list).apply {
            layoutManager = GridLayoutManager(this@PirListActivity, 2)
            setBackgroundColor(Color.TRANSPARENT)
            addItemDecoration(GridSpacingItemDecoration(2, SizeUtils.dp2px(8f), true))
        }


    }

    private fun loadData() {
//        DeviceClicent.getInstance().queryDeviceInfo()
    }

    val currentDayStartSecond: Long
        get() {
            val cal = Calendar.getInstance()
            val firstDay = cal[Calendar.DAY_OF_MONTH]
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            cal[year, month, firstDay, 0, 0] = 0
            LogUtils.e(TAG, "timeStartThisMonth", (cal.timeInMillis / 1000L).toString() + "")
            return cal.timeInMillis / 1000L
        }

    inner class RvRecordAdapter(data: List<RecordBean>) : BaseQuickAdapter<RecordBean, BaseViewHolder>(R.layout.item_video_record,data) {

        override fun convert(helper: BaseViewHolder, item: RecordBean) {
            val simpleDraweeView = helper.getView<SimpleDraweeView>(R.id.item_thumb)
            val controller: DraweeController = Fresco.newDraweeControllerBuilder().setUri(item.imageUrl)
                .setTapToRetryEnabled(true).setOldController(simpleDraweeView.controller).build()

            simpleDraweeView.controller = controller

            val timeStr = TimeUtils.formatHourMinuteFriendly(item.timestamp * 1000L)

            helper.setText(R.id.item_time, timeStr)

            val duration = item.period.toInt()
            val minute = duration / 60
            val sec = duration % 60
            if (duration == -1) {
                helper.setText(R.id.tv_duration, String.format(Locale.getDefault(), "-:-"))
            } else {
                helper.setText(R.id.tv_duration, String.format(Locale.getDefault(), "%02d:%02d", minute, sec))
            }
            helper.setText(R.id.item_device_name, item.deviceName)
            val ivFlag = helper.getView<ImageView>(R.id.item_flag)
            if (item.marked == 1) {
                ivFlag.setImageResource(R.mipmap.library_marked)
            } else {
                if (item.missing == 0) {
                    ivFlag.setImageDrawable(null)
                } else {
                    ivFlag.setImageResource(R.mipmap.library_missing)
                }
            }

            val tags = item.tags
            val view = helper.getView<FlowLayout>(R.id.tag_root)
            view.removeAllViews()
            if (!TextUtils.isEmpty(tags)) {
                try {
                    val tagItems = tags.split(",").toTypedArray()
                    val hashSet = HashSet<Int>()
                    for (itemTag in tagItems) {
                        if (tagInfos.containsKey(itemTag)) {
                            hashSet.add(tagInfos[itemTag]!!.icon)
                        }
                    }
                    hashSet.forEach {
                        view.addView(ImageView(this@PirListActivity).apply { setImageResource(it) })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            helper.setChecked(R.id.cb_library_record, item.isSelect)
            helper.addOnClickListener(R.id.cb_library_record)
        }
    }

    companion object {
        private const val TAG = "PirListActivity"
    }
}