package com.addx.ai.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.addx.common.utils.BitmapUtils;
import com.addx.common.utils.CommonUtil;
import com.addx.common.utils.LogUtils;
import com.ai.addxsettings.ADDXSettings;
import com.ai.addxvideo.addxvideoplay.LiveAddxVideoView;

public class VideoViewTest extends NocontrolVideoViewTest {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtil.hideNavKey(this);
        findViewById(R.id.bbb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("dd","getThumbImagePath-----"+((LiveAddxVideoView)findViewById(R.id.setup_videoview)).getThumbImagePath());
//                ((ImageView)findViewById(R.id.img)).setImageBitmap(BitmapUtils.getBitmap(((LiveAddxVideoView)findViewById(R.id.setup_videoview)).getThumbImagePath()));
                ADDXSettings.launchPlaybackPage(VideoViewTest.this, allDevice.get(0));
            }
        });
    }
    @Override
    protected int getResid(){
        return R.layout.activity_video_view_test2;
    }

    @Override
    protected void beginAutoPlay(){

    }
}
