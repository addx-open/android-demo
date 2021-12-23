package com.addx.ai.demo;

import android.util.Log;

import com.ai.addxbase.ADDXBind;
import com.ai.addxbase.AddxVideoContextInitCallBack;
import com.ai.addxbase.AddxNode;
import com.ai.addxbase.A4xContext;
import com.ai.addxsettings.ADDXSettings;
//import com.ai.guard.vicohome.AddxInternalApp;
import androidx.multidex.MultiDexApplication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class demoApp  extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MzUyMjU3NzQiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiJlMjkzYzE2YmNjOGY0MjM2YThjOTZiODg4ZjE0ZjFiYiIsImV4cCI6MjYzNTIyNTc3MywidXNlcklkIjoxMDg3fQ.qHnzbUWLlA_K6k89FRVwVKmsBsuwGQF_DkigHXEmhSZH0-_wZCXreGjkXzpzQLI7QDsA6QWf8kMJ9eovtLA7gQ";//netvue  cn
        A4xContext.getInstance().initA4xSdk(getApplicationContext(), "guard", "zh", "CN", A4xContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_CN, token, new AddxVideoContextInitCallBack() {
            @Override
            public void success() {

            }

            @Override
            public void fail(String message) {

            }
        });
    }

}
