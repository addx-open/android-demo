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
//        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MzUyMjU3NzQiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiJlMjkzYzE2YmNjOGY0MjM2YThjOTZiODg4ZjE0ZjFiYiIsImV4cCI6MjYzNTIyNTc3MywidXNlcklkIjoxMDg3fQ.qHnzbUWLlA_K6k89FRVwVKmsBsuwGQF_DkigHXEmhSZH0-_wZCXreGjkXzpzQLI7QDsA6QWf8kMJ9eovtLA7gQ";//netvue  cn
        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6ImE0eC1wcm9kMSIsImFjY291bnRJZCI6InNka2RlbW8iLCJzZWVkIjoiNjNmYjNkMmU1ZGVjNDRjNTlhNjk2ZmZmYTU0NDE2YjIiLCJleHAiOjI2NDQ5MTIwNTAsInVzZXJJZCI6MTI3NTk5fQ.A98xaFTU-IdJ4jgzFINki-WqKGE35kzTD-ncHRz72IKfyOq0vWn5jHgoEezIDh8cfOmIMzUZUDalKHNEhwpttA";
//            A4xContext.getInstance().initA4xSdk(getApplicationContext(), "guard", "zh", "CN", A4xContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_CN, token, new AddxVideoContextInitCallBack() {
        A4xContext.getInstance().initA4xSdk(getApplicationContext(), "vicohome", "en", "US", A4xContext.BuildEnv.PROD, AddxNode.PROD_NODE_US, token, new AddxVideoContextInitCallBack() {
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
