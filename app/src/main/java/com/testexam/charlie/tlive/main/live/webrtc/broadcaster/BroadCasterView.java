package com.testexam.charlie.tlive.main.live.webrtc.broadcaster;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

/**
 * Created by charlie on 2018. 5. 28..
 */

public interface BroadCasterView extends MvpView{
    void logAndToast(String msg);

    void disconnect();

    VideoCapturer createVideoCapturer();

    EglBase.Context getEglBaseContext();

    VideoRenderer.Callbacks getLocalProxyRenderer();
}
