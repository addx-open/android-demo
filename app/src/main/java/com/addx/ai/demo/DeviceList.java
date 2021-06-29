package com.addx.ai.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.ai.addxbase.util.ToastUtils;
import com.ai.addx.model.DeviceBean;
import com.ai.addx.model.request.BaseEntry;
import com.ai.addx.model.response.AllDeviceResponse;
import com.ai.addxbase.mvvm.BaseActivity;
import com.ai.addxnet.ApiClient;
import com.ai.addxnet.HttpSubscriber;
import com.ai.addxsettings.ADDXSettings;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class DeviceList extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_list;
    }

    LinearLayoutCompat container;

    @Override
    protected void initView() {
        super.initView();
        container = findViewById(R.id.list_device);
        listDeviceInfo();
    }

    void listDeviceInfo() {
        ApiClient.getInstance()
                .listDevice(new BaseEntry())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HttpSubscriber<AllDeviceResponse>() {

                    @Override
                    public void doOnNext(AllDeviceResponse response) {
                        if (response == null || response.getResult() != 0) {
                            ToastUtils.showShort("设备列表null code = " + response == null ? null : response.getResult());
                            return;
                        }
                        int i = 0;
                        for (DeviceBean bean : response.getData().getList()) {
                            i++;
                            View root = LayoutInflater.from(DeviceList.this).inflate(R.layout.item_device_demo, null, false);
                            ((TextView)root.findViewById(R.id.item_device_name)).setText(bean.getDeviceName());
                            root.findViewById(R.id.item_setting).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ADDXSettings.Companion.startSetting(DeviceList.this, bean);
                                }
                            });
                            container.addView(root);
                        }
                        ToastUtils.showShort("设备列表个数 == " +  i);
                    }

                    @Override
                    public void doOnError(Throwable e) {
                        super.doOnError(e);
                        ToastUtils.showShort("请求异常 == " +  e.getMessage());
                    }
        });
    }
}
