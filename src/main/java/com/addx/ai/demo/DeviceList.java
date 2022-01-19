package com.addx.ai.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.addx.common.utils.LogUtils;
import com.ai.addxbase.DeviceClicent;
import com.ai.addxbase.IDeviceClient;
import com.ai.addxbase.util.ToastUtils;
import com.ai.addx.model.DeviceBean;
import com.ai.addx.model.request.BaseEntry;
import com.ai.addx.model.response.AllDeviceResponse;
import com.ai.addxbase.mvvm.BaseActivity;
import com.ai.addxbase.ADDXBind;
import com.ai.addxnet.ApiClient;
import com.ai.addxnet.HttpSubscriber;
import com.ai.addxsettings.ADDXSettings;
import com.ai.addxvideo.addxvideoplay.LiveAddxVideoView;
import com.ai.addxvideo.addxvideoplay.SimpleAddxViewCallBack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        showLoadingDialog();
        DeviceClicent.getInstance().queryDeviceListAsync(new IDeviceClient.ResultListener<List<DeviceBean>>() {
            @Override
            public void onResult(@NotNull IDeviceClient.ResponseMessage responseMessage, @Nullable List<DeviceBean> result) {
                dismissLoadingDialog();
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
                    LiveAddxVideoView liveAddxVideoView = new LiveAddxVideoView(DeviceList.this);
                    liveAddxVideoView.init(DeviceList.this, bean, new SimpleAddxViewCallBack(){
                        @Override
                        public void onStartPlay() {
                            super.onStartPlay();
                        }

                        @Override
                        public void onError(int errorCode) {
                            super.onError(errorCode);
                        }
                    });
                    container.addView(liveAddxVideoView);
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