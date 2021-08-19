package com.addx.ai.demo;

import android.util.Log;

import com.addx.common.utils.LogUtils;
import com.ai.addxbase.AddxVideoContextInitCallBack;
import com.ai.addxbase.util.ToastUtils;
import com.ai.addxbase.AddxNode;
import com.ai.addxbase.AddxContext;
import com.ai.addxsettings.ADDXSettings;
import com.ai.addxvideo.addxvideoplay.AddxBaseVideoView;
import com.ai.addxvideo.addxvideoplay.LiveAddxVideoView;
//import com.ai.guard.vicohome.AddxInternalApp;
import androidx.multidex.MultiDexApplication;

import org.jetbrains.annotations.NotNull;

public class demoApp  extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        String token ="Bearer eyJhbGciOiJIUzUxMiJ9.eyJzZWVkIjoiYjZhYzRkODY2ZGU1NDc5ZTk0ZDdkYjNjZDcxYTBmOTEiLCJleHAiOjI2MjM5MjQ4NzUsInVzZXJJZCI6MjU4fQ.F2pD2mPzxiNse4QFC7dwpBlZZdTFMlAodsDOQsrUu1VC_MghkCbOze8_-fx9nyK-pLateIAFX2CH-8TQE3piLw";//us staging
//        String token ="Bearer eyJhbGciOiJIUzUxMiJ9.eyJzZWVkIjoiYjcwNGIwZmM2MWNkNGQwYjk5MTkxNGZhODUzMGNlMGQiLCJleHAiOjI2MjQ0MzUwMDEsInVzZXJJZCI6NDI4fQ.bG4SPNxEhxD2XSKKtGZPTXBLlhdPYNtpg3RYuj5cj3U2isohmiEOkRIdiIF1deKzAzE3sn9xloMTPwBJh0j_4w";//cn staging
        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MjkyNTc0MjMiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiJkN2ZkZWE5YzU0ZmU0ZTFiYTk1Yzc5ZTAwMDAzZWRhOCIsImV4cCI6MjYyOTI1NzQyNSwidXNlcklkIjo5NDN9.kXHFhr9urD58nYHlPuCBmD1y9uNnZdJKa8KN8YeCtEma32ReOURyMPl4_y_EL7o5sbTUkSsZe0jl9Y15m9QxPA";//netvue  cn
//        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MjQ1OTI2MDEiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiJiNjg5Nzg4MTc1NGM0NjhkOTU3ZjQxYTllMTE2YThmOCIsImV4cCI6MjYyNDU5MjYwMywidXNlcklkIjo1MzR9.mw8o7zzLiK7Pu82Fxr0e_8_SoMGI4BzBIxnHVx854RxmBSO1goLrpacV4zmGpNusBsKZc99JSLlpS_exCxxMxQ";//netvue  us
//        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MjQ1OTI2MTEiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiJiNmFkOGM0NmJlZTM0OWEwODE4OTkxZThiOTdlMzEyYiIsImV4cCI6MjYyNDU5MjYxNiwidXNlcklkIjo1NDgwfQ.F6MhCvuhhOyIC43RPFn3OvBfGuTTyjO-cRADT_shLMzMYc-3XKhzXQNDvmrAHsJ-iambmOod1-H8FzO-4SM6ew";//netvue  eu
//        AddxVideoContext.getInstance().setmContext(this);
//        AddxInternalApp.getInstance().installAddxVideo(this);
        AddxContext.getInstance().initA4xSdk(getApplicationContext(), "guard", "zh", "CN", AddxContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_CN, token, new AddxVideoContextInitCallBack() {
            @Override
            public void success() {

            }

            @Override
            public void fail(String message) {

            }
        });
//        AddxContext.getInstance().initA4xSdk(getApplicationContext(), "netvue", "zh", "CN", AddxContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_CN, token, new AddxVideoContextInitCallBack() {
//            @Override
//            public void success() {
//
//            }
//
//            @Override
//            public void fail(String message) {
//
//            }
//        });//netvue  cn
//        AddxContext.getInstance().initA4xSdk(getApplicationContext(), "netvue", "zh", "CN", AddxContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_US, token, new AddxVideoContextInitCallBack() {
//            @Override
//            public void success() {
//
//            }
//
//            @Override
//            public void fail(String message) {
//
//            }
//        });//netvue  us
//        AddxContext.getInstance().initA4xSdk(getApplicationContext(), "netvue", "zh", "CN", AddxContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_EU, token, new AddxVideoContextInitCallBack() {
//            @Override
//            public void success() {
//
//            }
//
//            @Override
//            public void fail(String message) {
//
//            }
//        });//netvue  eu
        ADDXSettings.setCallBack(new ADDXSettings.CallBack() {
            @Override
            public void onDeviceBeDeleteed(@NotNull String deviceSn, boolean isAdmin) {
                Log.d("ddd","onDeviceBeDeleteed-----");
            }
        });
    }

}
