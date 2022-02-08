package com.addx.ai.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.addx.common.utils.LogUtils;
import com.ai.addx.model.DeviceBean;
import com.ai.addxbase.A4xContext;
import com.ai.addxbase.AddxNode;
import com.ai.addxbase.AddxVideoContextInitCallBack;
import com.ai.addxbase.permission.PermissionHelp;
import com.ai.addxbase.util.ToastUtils;
import com.ai.addxbase.ADDXBind;
import com.ai.addxsettings.ADDXSettings;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity {
    public static final String TAG = "MainActivity";
    private View mDemoListView;


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
        mDemoListView = findViewById(R.id.demolist);
        initSettingParams();
    }

    /**
     * 初始化Setting SDK的配置,需要在进入Setting页面之前配置完成
     */
    private void initSettingParams() {
        ADDXSettings.Builder builder = new ADDXSettings.Builder().setActivityZoneClickCallback((v, deviceBean) -> {
            ToastUtils.showShort("click ActivityZone");
            return true;
        }).setSDCardClickCallback((v, deviceBean) -> {
            ToastUtils.showShort("click SDCard");
            return true;
        });
        if (Global.Settings.userMoreSettings) {
            builder.ennableMoreSettings(new ADDXSettings.SettingsMoreClickListener() {
                @Override
                public void onSettingsMoreClick(@NotNull View v, @NotNull DeviceBean deviceBean) {
                    ToastUtils.showShort("click MoreSetting");
                }
            }, null);
        }
        if (Global.Settings.disableMotionPageSettings) {
            builder.disableDurationAndResolution();
        }
        ADDXSettings.setFunctionBuilder(builder);
    }


    public void clickSdcardPlayActivity(View v) {
        Intent devicelistintent = new Intent(this, SdcardPlayActivity.class);
        startActivity(devicelistintent);
    }

    /**
     * Query the list of requests to bind my device by scanning the shared QR code of my device
     * 查询通过扫描我的设备二维码，绑定我设备的请求列表
     *
     * 可以通过推送的方式监听，或者在其他合适的位置查询
     */
    public void clickQuerySharedList(View view) {
        ADDXSettings.getAndShowAllRequestQrDialog(this, null);//查询并处理请求
//        DeviceClicent.getInstance().queryAllSharedInfo(xxx);  仅查询请求列表
    }

    public void onClickLogin(View v) {
        if (Global.isSDKInited) {
            Global.isSDKInited = false;
            ToastUtils.showShort(R.string.logout_success);
            mDemoListView.setVisibility(View.INVISIBLE);
            A4xContext.getInstance().releaseA4xSdk();
        } else {
            initSDK();
        }
    }

    private void initSDK() {
        showLoadingDialog();
        String token = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6Inp5al90ZXN0XzE2MzM3NTA5OTkiLCJhY2NvdW50SWQiOiJ0ZXN0QWNjb3VudF8xODYxMyIsInNlZWQiOiI0MzViMjVmMTA4YjM0YjNkOTFjNGQ3MmUyZjBlOTk4NSIsImV4cCI6MjYzMzc1MDk5OSwidXNlcklkIjo5ODd9.c3oqGbWlKnE5JvkQL92lnoeVKqgbEGyDo0ugrCG15W8n9-W-HJCVLZhKutuSCCir4oRvN1h8wFNA7lmyyH-qVA";//netvue  cn
        A4xContext.getInstance().initA4xSdk(getApplicationContext(), "guard", "zh", "CN", A4xContext.BuildEnv.STAGING, AddxNode.STRAGE_NODE_CN, token, Global.Settings.enableZendesk, new AddxVideoContextInitCallBack() {
            @Override
            public void success() {
                Global.isSDKInited = true;
                showSuccessPage();
                dismissLoadingDialog();
            }

            @Override
            public void fail(String message) {
                LogUtils.d(TAG, "sdk init failed : %s", message);
                showFailedPage();
                dismissLoadingDialog();
            }
        });
    }


    /**
     * show failed page when sdk init failed
     */
    private void showFailedPage() {
        ToastUtils.showShort(R.string.login_failed);
    }

    /**
     * show success page when sdk init failed
     *
     * 成功的页面包含如下两条逻辑
     *
     * 1.账号已绑定了设备
     *   直接使用Live SDK中的UI展示设备列表，并且仍然显示添加设备按钮
     * 2.账号未绑定设备，此时设备列表无设备
     *    展示添加设备的按钮，
     *
     */
    private void showSuccessPage() {
        ToastUtils.showShort(R.string.login_sccessful_sdk);
        mDemoListView.setVisibility(View.VISIBLE);
    }

    /**
     * click event
     * Add friend`s device
     * 1.Enter The Scan Qr Code Page
     * 2.The QRCode is one of device in the device list page
     * 3.In order to add a friend`s device , you need to go to this page to scan the QR code  of the friend`s device
     * on the sharing page
     */
    private static final int REQUST_CODE_SCAN_QR = 11;
    public void clickAddFriendDevice(View v) {
        ADDXSettings.launchScanFriendQRPageForResult(this, REQUST_CODE_SCAN_QR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUST_CODE_SCAN_QR) {
            if (resultCode == RESULT_OK) {
                ToastUtils.showShort(R.string.shared_request_success);
            } else {
                ToastUtils.showShort(R.string.shared_request_failed);
            }
        }
    }

    /**
     * click event
     * Enter device list page
     * 1.Show All devices you binded
     * 2.Include settings , Device Shares functions
     */
    public void clickShowDeviceList(View v) {
        Intent devicelistintent = new Intent(this, DeviceList.class);
        startActivity(devicelistintent);
    }

    /**
     * click event
     * Enter custom player device list page
     */
    public void clickCustomerPlayerDeviceList(View v) {
        Intent devicelistintent = new Intent(this, PlayerDeviceList.class);
        startActivity(devicelistintent);
    }
    /**
     * click event
     * Enter the device bind page
     * It provides a way to add devices to the device list
     */
    public void clickToAddDevice(View v) {
        ADDXBind.lanchBind(this, new ADDXBind.Builder()
                .withBindCallback(new ADDXBind.BindInterface() {
            @Override
            public void onBindCancel() {
            }

            @Override
            public void onBindSccess(String sn) {
                ToastUtils.showShort("bind success sn :" + sn);
            }

            @Override
            public void onBindStart(String callBackUrl) {
                ToastUtils.showShort("bind start callBackUrl :" + callBackUrl);
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
    public void clickBindAlexa(View v) {
        Intent devicelistintent = new Intent(this, AlexaActivity.class);
        startActivity(devicelistintent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        PermissionHelp.checkStoragePermissions(this, null);
    }
}
