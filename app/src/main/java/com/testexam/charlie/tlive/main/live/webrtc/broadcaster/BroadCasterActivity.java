package com.testexam.charlie.tlive.main.live.webrtc.broadcaster;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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

    @ViewById(R.id.liveBroadStartBtn)
    protected Button liveBroadStartBtn;

    @ViewById(R.id.liveBroadInfoLo)
    protected RelativeLayout liveBroadInfoLo;

    @ViewById(R.id.liveBroadCloseIb)
    protected ImageButton liveBroadCloseIb;

    @ViewById(R.id.liveBroadSetTv)
    protected TextView liveBroadSetTv;

    @ViewById(R.id.liveBroadStopBtn)
    protected Button liveBroadStopBtn;

    @ViewById(R.id.liveBroadChatRv)
    protected RecyclerView liveBroadChatRv;

    private String ownerEmail;
    private String ownerName;
    @AfterViews
    protected void init() {
        SharedPreferences prefs = getSharedPreferences("login",MODE_PRIVATE);
        ownerEmail = prefs.getString("email",null);
        ownerName = prefs.getString("name",null);
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
        liveBroadStartBtn.setOnClickListener(this);
        liveBroadCloseIb.setOnClickListener(this);
        liveBroadStopBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 방송 시작 버튼
            // 버튼을 누르게 되면 방송 제목이 있는지 확인 한 다음
            // 제목이 있을 경우 방송을 시작한다.
            // 없을 경우 방송 제목이 필요하다는 메시지를 보여준다.
            case R.id.liveBroadStartBtn:
                String roomName = roomNameEt.getText().toString();
                String roomTag = roomTagTv.getText().toString();
                // 방송 시작의 필수 사항인 방 제목이 없으면 방송을 시작하지 않는다.
                if(!roomName.isEmpty()){
                    presenter.setInfo(ownerEmail,roomName,roomTag);
                    presenter.startCall();
                    liveBroadStartBtn.setVisibility(View.GONE);
                    liveBroadInfoLo.setVisibility(View.GONE);
                    liveBroadCloseIb.setVisibility(View.GONE);
                    liveBroadSetTv.setText(ownerName+"님의 생방송");
                    liveBroadStopBtn.setVisibility(View.VISIBLE);
                    liveBroadChatRv.setVisibility(View.VISIBLE);
                }else{
                    // 방송 제목이 필요하다는 Toast 메시지를 띄워준다.
                    Toast.makeText(getApplicationContext(),"방송 제목을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }

                break;

            // 방송 시작 전 종료 버튼
            case R.id.liveBroadCloseIb:
                finish();
                break;

            // 방송 시작 후 종료 버튼
            // 방송 종료 의사를 묻는 다이얼로그를 띄운 후 종료한다면 종료를, 취소하면 그대로 방송을 진행한다.
            // 종료 시 서버와 소켓 연결을 종료한다.
            case R.id.liveBroadStopBtn:
                AlertDialog.Builder stopDialog = new AlertDialog.Builder(getApplicationContext());
                stopDialog.setTitle("방송 종료")
                        .setMessage("방송을 종료하시겠습니까?")
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                disconnect();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
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