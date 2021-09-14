package com.addx.ai.demo;

import android.view.View;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.addx.ai.demo.videoview.KotlinDemoVideoView;
import com.addx.common.utils.LogUtils;
import com.ai.addx.model.DeviceBean;
import com.ai.addxbase.DeviceClicent;
import com.ai.addxbase.IDeviceClient;
import com.ai.addxbase.mvvm.BaseActivity;
import com.ai.addxbase.util.ToastUtils;
import com.ai.addxbind.devicebind.ADDXBind;
import com.ai.addxvideo.addxvideoplay.SimpleAddxViewCallBack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class CustomerDeviceListActivity extends BaseActivity {

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
        DeviceClicent.getInstance().queryDeviceListAsync(new IDeviceClient.ResultListener<List<DeviceBean>>() {
            @Override
            public void onResult(@NotNull IDeviceClient.ResponseMessage responseMessage, @Nullable List<DeviceBean> result) {
                if (responseMessage.getResponseCode()<0) {
                    ToastUtils.showShort("response error code = " + responseMessage.getResponseCode());
                    return;
                }
                if (result==null||result.isEmpty()){
                    findViewById(R.id.no_device).setVisibility(View.VISIBLE);
                    return;
                }else {
                    findViewById(R.id.no_device).setVisibility(View.INVISIBLE);
                }

                for (DeviceBean bean :result) {
//                            View root = LayoutInflater.from(DeviceList.this).inflate(R.layout.item_device_demo, null, false);
//                            ((TextView)root.findViewById(R.id.item_device_name)).setText(bean.getDeviceName());
//                            root.findViewById(R.id.item_setting).setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    ADDXSettings.Companion.startSetting(DeviceList.this, bean);
//                                }
//                            });

                    LogUtils.d(TAG, "name : " + bean.getDeviceName());
                    KotlinDemoVideoView demoVideoView = new KotlinDemoVideoView(CustomerDeviceListActivity.this);
                    demoVideoView.init(CustomerDeviceListActivity.this, bean, new SimpleAddxViewCallBack(){
                        @Override
                        public void onStartPlay() {
                            super.onStartPlay();
                        }

                        @Override
                        public void onError(int errorCode) {
                            super.onError(errorCode);
                        }
                    });
                    container.addView(demoVideoView);
                }
            }
        });
    }
    
    public void clickAddDevice(View v){
        ADDXBind.lanchBind(this,new ADDXBind.Builder().withBindCallback(new ADDXBind.BindInterface() {
            @Override
            public void onBindCancel() {

            }

            @Override
            public void onBindSccess(@NotNull String sn) {
                listDeviceInfo();
            }

            @Override
            public void onBindStart(@NotNull String callBackUrl) {

            }
        }));
    }
}
