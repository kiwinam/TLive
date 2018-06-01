package com.testexam.charlie.tlive.main.live.webrtc.viewer;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.mosby.mvp.MvpView;

import org.webrtc.EglBase;
import org.webrtc.VideoRenderer;

/**
 * Created by charlie on 2018. 5. 28..
 */

public interface ViewerView extends MvpView {
    void stopCommunication();

    void logAndToast(String msg);

    void disconnect();

    EglBase.Context getEglBaseContext();

    VideoRenderer.Callbacks getRemoteProxyRenderer();
}
