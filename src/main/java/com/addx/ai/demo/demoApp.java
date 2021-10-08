package com.addx.ai.demo;

import android.util.Log;

import com.ai.addxbase.AddxVideoContextInitCallBack;
import com.ai.addxbase.AddxNode;
import com.ai.addxbase.A4xContext;
import com.ai.addxsettings.ADDXSettings;
//import com.ai.guard.vicohome.AddxInternalApp;
import androidx.multidex.MultiDexApplication;

import org.jetbrains.annotations.NotNull;

public class demoApp  extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MzE3MDE0MTQiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiIzMzczZjJiNjNjM2E0MmQyYjliNTAxYjY5ZDIxZTMyMSIsImV4cCI6MjYzMTcwMTQxMywidXNlcklkIjo5NzB9.XJYoZIInPLDg1ym02o-8zneyjQIyCGA6itTDRTzi2mbMT6qpcVko-oh7VFGj3cLzKXGP6NK-2I8dw2ON3HhafQ";//netvue  cn
        A4xContext.getInstance().initA4xSdk(getApplicationContext(), "guard", "zh", "CN", A4xContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_CN, token, new AddxVideoContextInitCallBack() {
            @Override
            public void success() {

            }

            @Override
            public void fail(String message) {

            }
        });
        ADDXSettings.setCallBack(new ADDXSettings.CallBack() {
            @Override
            public void onDeviceBeDeleteed(@NotNull String deviceSn, boolean isAdmin) {
                Log.d("ddd","onDeviceBeDeleteed-----");
            }
        });
    }

}
