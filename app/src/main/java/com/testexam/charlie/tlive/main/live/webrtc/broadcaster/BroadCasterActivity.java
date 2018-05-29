package com.testexam.charlie.tlive.main.live.webrtc.broadcaster;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;
import com.testexam.charlie.tlive.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.Camera1Enumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

/**
 * Created by charlie on 2018. 5. 28
 */

@EActivity(R.layout.activity_broadcaster)
public class BroadCasterActivity extends MvpActivity<BroadCasterView, BroadCasterPresenter> implements BroadCasterView, View.OnClickListener {

    private static final String TAG = BroadCasterActivity.class.getSimpleName();


    private EglBase rootEglBase;
    private ProxyRenderer localProxyRenderer;
    private Toast logToast;


    @ViewById(R.id.liveBroadWebRtcSurfaceView)
    protected SurfaceViewRenderer vGLSurfaceViewCall;

    @ViewById(R.id.liveBroadRoomNameEt)
    protected EditText roomNameEt;

    @ViewById(R.id.liveBroadRoomTagTv)
    protected TextView roomTagTv;

    protected Button liveStartBtn;
    @AfterViews
    protected void init() {

        // 클릭 리스너 설정.
        setOnClickListeners();

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        // Peer 연결 설정
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        localProxyRenderer.setTarget(vGLSurfaceViewCall);

        presenter.initPeerConfig();
    }

    private void setOnClickListeners(){
        liveStartBtn = findViewById(R.id.liveBroadStartBtn);
        liveStartBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.liveBroadStartBtn:
                String roomName = roomNameEt.getText().toString();
                String roomTag = roomTagTv.getText().toString();
                // 방송 시작의 필수 사항인 방 제목이 없으면 방송을 시작하지 않는다.
                if(!roomName.isEmpty()){
                    presenter.startCall();
                }else{
                    // 방송 제목이 필요하다는 Toast 메시지를 띄워준다.
                    Toast.makeText(getApplicationContext(),"방송 제목을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void disconnect() {
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if (Build.VERSION.SDK_INT < 23) {
            presenter.startCall();
        } else{
            Log.e(TAG,"onResume sdk 23 over");
        }*/
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if(cameraPermission == PackageManager.PERMISSION_GRANTED){
            presenter.setVideoToSurfaceView();

            //presenter.startCall();
            Log.e(TAG,"start call");
        }else{
            Log.e(TAG,"camera permission error");
        }
    }



    @NonNull
    @Override
    public BroadCasterPresenter createPresenter() {
        return new BroadCasterPresenter(getApplication());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.disconnect();
    }

    @Override
    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override
    public VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && presenter.getDefaultConfig().isUseCamera2();
    }

    private boolean captureToTexture() {
        return presenter.getDefaultConfig().isCaptureToTexture();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

}