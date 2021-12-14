package com.addx.ai.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ai.addxbase.ADDXBind;
import com.ai.addxbase.permission.PermissionHelp;
import com.ai.addxbase.util.ToastUtils;
//import com.ai.addxbind.devicebind.ADDXBind;
import com.ai.addxsettings.ADDXSettings;
//import com.ai.guard.vicohome.SplashActivity;

public class MainActivity extends BaseActivity {
    public static final String TAG = "MainActivity";

    public static class Event {
        int type;

        public Event(int t) {
            type = t;
        }
    }

    public static final String LOGIN_OK = "LOGIN_OK";
    public static final String PLAY_OK = "PLAY_OK";

    @Override
    protected int getResid() {
        return R.layout.activity_demo_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void clickLogin(View v){
        Intent devicelistintent = new Intent(this, Logintest.class);
        startActivity(devicelistintent);
    }

    public void clickHome(View v){
//        Intent devicelistintent = new Intent(this, SplashActivity.class);
//        startActivity(devicelistintent);
    }

    public void clickLibrary(View v){
//        Intent devicelistintent = new Intent(this, SplashActivity.class).putExtra("module","library");
//        startActivity(devicelistintent);
    }
    /**
     * click event
     * Add friend`s device
     * 1.Enter The Scan Qr Code Page
     * 2.The QRCode is one of device in the device list page
     * 3.In order to add a friend`s device , you need to go to this page to scan the QR code  of the friend`s device
     * on the sharing page
     */
    public void clickAddFriendDevice(View v) {
        ADDXSettings.launchScanFriendQRPageForResult(this, 0);
    }

    /**
     * click event
     * Enter device list page
     * 1.Show All devices you binded
     * 2.Include settings , Device Shares functions
     */
    public void clickShowDeviceList(View v) {
        Intent devicelistintent = new Intent(this, DeviceListActivity.class);
        startActivity(devicelistintent);
    }

    /**
     * click event
     * Enter custom device list page
     * 1.Show All devices you binded
     * 2.Include settings , Device Shares functions
     */
    public void clickShowCustomDeviceList(View v) {
        Intent devicelistintent = new Intent(this, CustomerDeviceListActivity.class);
        startActivity(devicelistintent);
    }
    public void clickSdcardPlayActivity(View v) {
        Intent devicelistintent = new Intent(this, SdcardPlayActivity.class);
        startActivity(devicelistintent);
    }

    /**
     * click event
     * Enter the device bind page
     * It provides a way to add devices to the device list
     */
    public void clickToAddDevice(View v) {
        ADDXBind.lanchBind(this, new ADDXBind.Builder().withBindCallback(new ADDXBind.BindInterface() {
            @Override
            public void onBindCancel() {
            }

            @Override
            public void onBindSccess(String sn) {
                ToastUtils.showShort("bind success sn :" + sn);
            }

            @Override
            public void onBindStart(String callBackUrl) {
                ToastUtils.showShort("bind success callBackUrl :" + callBackUrl);
            }
        }));
    }

    /**
     * click event
     * Enter the device video list page
     * It provides a way to add devices to the device list
     */
    public void clickShowVideoList(View v) {
        Intent devicelistintent = new Intent(this, PirListActivity.class);
        startActivity(devicelistintent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        PermissionHelp.checkStoragePermissions(this, null);
    }
}
