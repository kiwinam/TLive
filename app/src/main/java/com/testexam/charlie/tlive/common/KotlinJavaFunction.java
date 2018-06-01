package com.testexam.charlie.tlive.common;

import android.content.Context;
import android.content.Intent;

import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity;
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity_;
import com.testexam.charlie.tlive.main.live.webrtc.viewer.ViewerActivity;
import com.testexam.charlie.tlive.main.live.webrtc.viewer.ViewerActivity_;
//import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity_;

/**
 * Created by charlie on 2018. 5. 29..
 */

public class KotlinJavaFunction {

    public KotlinJavaFunction(){}

    public void goLive(Context context){
        //context.startActivity(new Intent(context,BroadCasterActivity.class));
        //context.startActivity(new Intent(context, BroadCasterActivity_))

        BroadCasterActivity_.intent(context).start();
    }

    public void goViewer(Context context){
        ViewerActivity_.intent(context).start();
    }
}
