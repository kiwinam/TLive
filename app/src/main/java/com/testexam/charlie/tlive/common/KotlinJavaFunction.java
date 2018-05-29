package com.testexam.charlie.tlive.common;

import android.content.Context;
import android.content.Intent;

import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity;
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity_;

/**
 * Created by charlie on 2018. 5. 29..
 */

public class KotlinJavaFunction {

    public KotlinJavaFunction(){}

    public void goLive(Context context){
        //context.startActivity(new Intent(context, BroadCasterActivity_))

        BroadCasterActivity_.intent(context).start();

    }
}
