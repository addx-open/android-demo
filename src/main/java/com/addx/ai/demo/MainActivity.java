package com.addx.ai.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.addx.common.Const;
import com.ai.addxbase.permission.PermissionHelp;
import com.addx.common.utils.LogUtils;
import com.ai.addxbase.util.ToastUtils;
import com.ai.addx.model.request.BaseEntry;
import com.ai.addx.model.response.AllDeviceResponse;
import com.ai.addxbind.devicebind.ADDXBind;
import com.ai.addxnet.ApiClient;
import com.ai.addxnet.HttpSubscriber;
//import com.ai.guard.vicohome.SplashActivity;
//import com.ai.guard.vicohome.modules.HomeActivity;
//import com.ai.guard.vicohome.modules.mine.device.DeviceSettingActivity;
//import com.ai.guard.vicohome.utils.JumpUtils;
import com.blankj.rxbus.RxBus;

import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity  implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    private ProgressBar pb_loading;
    public static class Event{
        int type;
        public Event(int t){
            type = t;
        }
    }
    public static final String LOGIN_OK = "LOGIN_OK";
    public static final String PLAY_OK = "PLAY_OK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.videoview).setOnClickListener(this);
        findViewById(R.id.nocontrolvideoview).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
//        findViewById(R.id.home).setOnClickListener(this);
        findViewById(R.id.devicelist).setOnClickListener(this);
        findViewById(R.id.lib).setOnClickListener(this);
        findViewById(R.id.device_setting).setOnClickListener(this);
        findViewById(R.id.user_setting).setOnClickListener(this);
        findViewById(R.id.addxtest).setOnClickListener(this);
        findViewById(R.id.bind).setOnClickListener(this);
        findViewById(R.id.unbind).setOnClickListener(this);
        pb_loading = findViewById(R.id.pb_loading);

        RxBus.getDefault().subscribe(this, LOGIN_OK, new RxBus.Callback<Event>() {
            @Override
            public void onEvent(Event object) {
                LogUtils.d("dd", "startLogin=======onEvent");
                if(object instanceof Event){
                    if(((Event)object).type == 1){
                        findViewById(R.id.demolist).post(()->{
                            findViewById(R.id.demolist).setVisibility(View.VISIBLE);
                        });
                    }
                }
            }
        });
    }

    @Override
    protected int getResid(){
        return R.layout.activity_demo_main;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getDefault().unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.videoview:
                Intent intent = new Intent(this, VideoViewTest.class);
                startActivity(intent);
                break;
            case R.id.nocontrolvideoview:
                Intent nocontrolvideoviewintent = new Intent(this, NocontrolVideoViewTest.class);
                startActivity(nocontrolvideoviewintent);
                break;
            case R.id.login:
                Intent loginintent = new Intent(this, Logintest.class);
                startActivity(loginintent);
                break;
            case R.id.home:
//                Intent homeintent = new Intent(this, HomeActivity.class);
//                startActivity(homeintent);
                break;
            case R.id.devicelist:
                Intent devicelistintent = new Intent(this, DeviceList.class);
                startActivity(devicelistintent);
                break;
            case R.id.lib:
                Intent libintent = new Intent(this, LibActivity.class);
                startActivity(libintent);

//                Intent libintent = new Intent(this, HomeActivity.class);
//                libintent.putExtra(HomeActivity.STATE_CHECK_INDEX, HomeActivity.INDEX_LIBRARY);
//                startActivity(libintent);
                break;
            case R.id.device_setting:
                listDevice();
                break;
            case R.id.user_setting:
                Intent user_settingintent = new Intent(this, MineActivity.class);
                startActivity(user_settingintent);
                break;
            case R.id.addxtest:
//                Intent addxtestintent = new Intent(this, SplashActivity.class);
//                startActivity(addxtestintent);
                break;
            case R.id.bind:
                ADDXBind.lanchBind(this, new ADDXBind.Builder()  );
                break;
            case R.id.unbind:
                String msn = ADDXBind.Companion.getMSN();
                if (TextUtils.isEmpty(msn)){
                    ToastUtils.showShort("请先绑定设备");
                    return;
                }
                ADDXBind.unBindDevice(ADDXBind.Companion.getMSN(), new ADDXBind.UnBindInterface() {
                    @Override
                    public void onUnbindError(String sn) {
                        ToastUtils.showShort("解绑失败");
                    }

                    @Override
                    public void onUnBindSuccess(String sn) {
                        ToastUtils.showShort("解绑成功");
                    }

                    @Override
                    public void onStartUnBind(String sn) {
                        ToastUtils.showShort("开始解绑");
                    }
                });
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(ADDXBind.Companion.getMSN())) {
            ((Button) findViewById(R.id.unbind)).setText("解绑:" + ADDXBind.Companion.getMSN());
        }
        PermissionHelp.checkStoragePermissions(this, null);
    }

    void listDevice() {
        pb_loading.setVisibility(View.VISIBLE);
        ApiClient.getInstance()
                .listDevice(new BaseEntry())
                .subscribeOn(Schedulers.io())
                .subscribe(new HttpSubscriber<AllDeviceResponse>() {
                    @Override
                    public void doOnNext(AllDeviceResponse allDeviceResponse) {
                        LogUtils.d(TAG,"initPlayer========doOnNext");
                        pb_loading.setVisibility(View.INVISIBLE);
                        if (allDeviceResponse.getResult() < Const.ResponseCode.CODE_OK
                                || allDeviceResponse.getData() == null
                                || allDeviceResponse.getData().getList() == null) {
                            ToastUtils.showShort("获取设备失败");
                            return;
                        }
                        LogUtils.d(TAG,"=doOnNext===ok");

//                        Intent devicesettingintent = new Intent(MainActivity.this, DeviceSettingActivity.class);
//                        devicesettingintent.putExtra(Const.Extra.DEVICE_BEAN, allDeviceResponse.getData().getList().get(0));
//                        startActivity(devicesettingintent);
//                        DeviceSettingActivity.Companion.startAutoJump(MainActivity.this, allDeviceResponse.getData().getList().get(0));
                    }

                    @Override
                    public void doOnError(Throwable e) {
                        super.doOnError(e);
                        pb_loading.setVisibility(View.INVISIBLE);
                        ToastUtils.showShort("获取设备失败");
                    }
                });
    }
}
