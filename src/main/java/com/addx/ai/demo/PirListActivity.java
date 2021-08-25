package com.addx.ai.demo;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.addx.common.utils.LogUtils;
import com.ai.addx.model.RecordBean;

import java.util.ArrayList;
import java.util.Date;

public class PirListActivity extends BaseActivity {

    private ArrayList<RecordBean> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        RecyclerView listView = findViewById(R.id.pir_list);
    }

    public long getPreviousMonthFirstDayOfLastWeek(java.util.Calendar cal) {
        int firstDay = cal.getActualMinimum(java.util.Calendar.DAY_OF_MONTH);
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month, firstDay, 0, 0, 0);
        LogUtils.e("timeStartThisMonth", (calendar.getTime().getTime() / 1000L) + "");
        int i = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        int offset = i - 1;
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -offset);
        Date time = calendar.getTime();
        return time.getTime() / 1000L;
    }

    public long getNextMonthLastDayOfFirstWeek(java.util.Calendar cal) {
        int lastDay = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month, lastDay, 0, 0, 0);
        int i = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        int offset = 7 - i;
        calendar.add(java.util.Calendar.DAY_OF_MONTH, offset);
        Date time = calendar.getTime();
        return time.getTime() / 1000L;
    }

    @Override
    protected int getResid() {
        return R.layout.activity_pir_list;
    }
}
