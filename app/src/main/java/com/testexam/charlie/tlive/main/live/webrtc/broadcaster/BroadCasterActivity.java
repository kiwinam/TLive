package com.testexam.charlie.tlive.main.live.webrtc.broadcaster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.DefaultSocketService;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionClient;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionParameters;
import com.nhancv.webrtcpeer.rtc_peer.SignalingEvents;
import com.nhancv.webrtcpeer.rtc_peer.SignalingParameters;
import com.nhancv.webrtcpeer.rtc_peer.StreamMode;
import com.nhancv.webrtcpeer.rtc_peer.config.DefaultConfig;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;
import com.nhancv.webrtcpeer.rtc_plugins.RTCAudioManager;
import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.common.BaseActivity;
import com.testexam.charlie.tlive.common.kurento.KurentoPresenterRTCClient;
import com.testexam.charlie.tlive.common.kurento.models.CandidateModel;
import com.testexam.charlie.tlive.common.kurento.models.response.ServerResponse;
import com.testexam.charlie.tlive.common.kurento.models.response.TypeResponse;
import com.testexam.charlie.tlive.main.live.webrtc.broadChat.Chat;
import com.testexam.charlie.tlive.main.live.webrtc.broadChat.ChatAdapter;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class BroadCasterActivity extends BaseActivity implements View.OnClickListener,SignalingEvents, PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = BroadCasterActivity.class.getSimpleName(); // 로그 출력을 위한 태그 설정. 현재 클래스의 이름으로 저장함.

    private static final String STREAM_HOST = "wss://13.125.64.135:8443/one2many"; // 서버에 WebRTC 스트리밍을 하기 위한 url 과 port, JS 파일 경로 설정

    private SocketService socketService; // 서버 WebSocket 과 연결하기 위한 SocketService
    private Gson gson; // 서버에서 결과값으로 넘어오거나 서버로 전달해주는 데이터들을 JSON 형식으로 만들어 보내기 위한 GSON

    private PeerConnectionClient peerConnectionClient;
    private KurentoPresenterRTCClient rtcClient;
    private PeerConnectionParameters peerConnectionParameters;
    private DefaultConfig defaultConfig;
    private RTCAudioManager audioManager;
    private SignalingParameters signalingParameters;

    private boolean iceConnected;

    private EglBase rootEglBase;
    private ProxyRenderer localProxyRenderer;
    private Toast logToast;

    private int viewNum = 0;

    private SurfaceViewRenderer vGLSurfaceViewCall;

    private EditText roomNameEt;

    private TextView roomTagTv;
    private TextView liveBroadCountTv;
    private TextView liveBroadSetTv;

    private RelativeLayout liveBroadInfoLo;
    private RelativeLayout liveBroadCountLo;

    private ImageButton liveBroadCloseIb;
    private Button liveBroadStopBtn;
    private Button liveBroadStartBtn;

    private RecyclerView liveBroadChatRv;

    private String ownerEmail;
    private String ownerName;

    private ChatAdapter chatAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        init();
    }

    private void init() {
        socketService = new DefaultSocketService(getApplication()); // WebRTC 연결을 위해 소켓을 생성한다.
        gson = new Gson();

        SharedPreferences prefs = getSharedPreferences("login",MODE_PRIVATE); // SharedPreferences 에서 로그인 정보를 가져온다.
        ownerEmail = prefs.getString("email",null); // 현재 사용자의 이메일을 가져온다.
        ownerName = prefs.getString("name",null); // 현재 사용자의 이름을 가져온다.
        setFindViews(); // 사용할 View 들을 연결
        setOnClickListeners(); // 클릭 리스너 설정.
        setChatRecyclerView(); // 채팅 리사이클러뷰 설정

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        // Peer 연결 설정
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        localProxyRenderer.setTarget(vGLSurfaceViewCall);

        initPeerConfig();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /*
     * 사용할 View 들을 연결한다.
     */
    private void setFindViews(){
        vGLSurfaceViewCall = findViewById(R.id.liveBroadWebRtcSurfaceView);
        roomNameEt = findViewById(R.id.liveBroadRoomNameEt);
        roomTagTv = findViewById(R.id.liveBroadRoomTagTv);
        liveBroadStartBtn = findViewById(R.id.liveBroadStartBtn);
        liveBroadInfoLo = findViewById(R.id.liveBroadInfoLo);
        liveBroadCloseIb = findViewById(R.id.liveBroadCloseIb);
        liveBroadSetTv = findViewById(R.id.liveBroadSetTv);
        liveBroadStopBtn = findViewById(R.id.liveBroadStopBtn);
        liveBroadChatRv = findViewById(R.id.liveBroadChatRv);
        liveBroadCountTv = findViewById(R.id.liveBroadCountTv);
        liveBroadCountLo = findViewById(R.id.liveBroadCountLo);
    }

    private void setOnClickListeners(){
        liveBroadStartBtn.setOnClickListener(this);
        liveBroadCloseIb.setOnClickListener(this);
        liveBroadStopBtn.setOnClickListener(this);
        liveBroadCountTv = findViewById(R.id.liveBroadCountTv);
        liveBroadCountTv.setOnClickListener(this);
    }

    private void setChatRecyclerView(){
        chatAdapter = new ChatAdapter(new ArrayList<>(),getApplicationContext(),1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        //linearLayoutManager.setReverseLayout(true);
        liveBroadChatRv.setHasFixedSize(true);
        liveBroadChatRv.setLayoutManager(linearLayoutManager);
        liveBroadChatRv.setAdapter(chatAdapter);
        liveBroadChatRv.setItemAnimator(new DefaultItemAnimator());
    }

    private void receiveChatMessage(String sender, String message) {
        runOnUiThread(()-> {
            chatAdapter.setData(new Chat(sender,message,0));
            liveBroadChatRv.scrollToPosition(chatAdapter.getItemCount()-1);
        });
    }

    @SuppressLint("SetTextI18n")
    public void setViewerNum(boolean isUp){
        if(isUp){
            viewNum++;
        }else{
            viewNum--;
        }
        Log.d("setViewerNum",viewNum+"..");
        runOnUiThread(()-> liveBroadCountTv.setText("시청자 "+viewNum+"명"));
    }

    @SuppressLint("SetTextI18n")
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
                    rtcClient.setInfo(ownerEmail,roomName,roomTag);
                    startCall();
                    liveBroadStartBtn.setVisibility(View.GONE);
                    liveBroadInfoLo.setVisibility(View.GONE);
                    liveBroadCloseIb.setVisibility(View.GONE);
                    liveBroadSetTv.setText(ownerName+"님의 생방송");
                    liveBroadStopBtn.setVisibility(View.VISIBLE);
                    liveBroadChatRv.setVisibility(View.VISIBLE);
                    liveBroadCountLo.setVisibility(View.VISIBLE);
                    hideSystemUI();
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
                showDisconnectDialog();
                break;
        }
    }

    private void showDisconnectDialog(){
        runOnUiThread(()->{
            AlertDialog.Builder stopDialog = new AlertDialog.Builder(BroadCasterActivity.this,R.style.myDialog);
            stopDialog.setTitle("방송 종료")
                    .setMessage("방송을 종료하시겠습니까?")
                    .setPositiveButton("종료", (dialog, which) -> disconnect())
                    .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                    .show();
        });
    }

    public void initPeerConfig(){
        rtcClient = new KurentoPresenterRTCClient(socketService);
        defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.SEND_ONLY);
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, this );

        peerConnectionClient.setVideoEnabled(true);
    }

    /*
    WebSocket 연결 해제
     */
    public void disconnect(){
        if(rtcClient != null){
            rtcClient = null;
        }

        if(peerConnectionClient != null){
            peerConnectionClient.close();
            peerConnectionClient = null;
        }

        if(audioManager != null){
            audioManager.stop();
            audioManager = null;
        }

        if(socketService != null){
            socketService.close();
        }
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }
        viewNum = 0;
        finish();
    }

    public void startCall(){
        // rtcClient 가 없다면 에러 로그 발생 후 종료
        if(rtcClient == null){
            Log.e(TAG,"rtcClient is null");
            return;
        }
        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback(){
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                super.onOpen(serverHandshake);
                logAndToast("Socket connected");
                SignalingParameters parameters = new SignalingParameters(
                        new LinkedList<PeerConnection.IceServer>(){
                            {
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);
            }

            @Override
            public void onMessage(String serverResponse_) {
                super.onMessage(serverResponse_);
                try{
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);

                    switch (serverResponse.getIdRes()){
                        case PRESENTER_RESPONSE:

                            if(serverResponse.getTypeRes() == TypeResponse.REJECTED){
                                logAndToast(serverResponse.getMessage());
                            }else{
                                Log.e(TAG+"::presenterResponse",TypeResponse.ACCEPTED.toString());
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, serverResponse.getSdpAnswer());

                                onRemoteDescription(sdp);
                                Log.e(TAG+"::presenterResponse","onRemoteDescription");
                            }

                            break;

                        case ICE_CANDIDATE:
                            Log.e(TAG+"::onMessage","ICE_CANDIDATE");
                            CandidateModel candidateModel = serverResponse.getCandidate();
                            onRemoteIceCandidate(
                                    new IceCandidate(candidateModel.getSdpMid(),candidateModel.getSdpMLineIndex(),candidateModel.getSdp()));
                            break;

                        case VIEWER_CHANGE:
                            Log.e(TAG+"::onMessage","VIEWER_CHANGE");
                            boolean isUp = false;
                            if(serverResponse.getMessage().equals("up")){
                                isUp = true;
                            }
                            setViewerNum(isUp);
                            break;
                        case CHAT:
                            Log.d("chat Sender : "+serverResponse.getSender(), "msg : "+serverResponse.getMessage());
                            receiveChatMessage(serverResponse.getSender(), serverResponse.getMessage());
                            break;
                    }
                }catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                super.onClose(i, s, b);
                runOnUiThread(()->{
                    logAndToast("Socket closed");
                    disconnect();
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                runOnUiThread(()->{
                    logAndToast(e.getMessage());
                    disconnect();
                });
            }
        });

        // 오디오 라우팅 (오디오 모드, 오디오 장치 열거 등)을 처리할 오디오 관리자 만듬.
        audioManager = RTCAudioManager.create(getApplicationContext());
        // 기존 오디오 설정 저장 및 오디오 모드 변경
        // 가능한 최상의 VoIP 성능을 제공
        Log.d(TAG,"Starting audio manager");
        audioManager.start((audioDevice, availableAudioDevices) ->
                Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                        + "selected: " + audioDevice));
    }


    public DefaultConfig getDefaultConfig() { return defaultConfig; }

    private void callConnected(){
        if(peerConnectionClient == null){
            Log.w(TAG,"Call is connected in closed or error state");
            return;
        }

        // 통계 콜백 활성화
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    /*
    신호가 연결 되었을 때
     */
    @Override
    public void onSignalConnected(SignalingParameters params) {
        runOnUiThread(()->{
            signalingParameters = params;
            VideoCapturer videoCapturer = null;

            if(peerConnectionParameters.videoCallEnabled){
                videoCapturer = createVideoCapturer();
            }

            peerConnectionClient.createPeerConnection(getEglBaseContext(), getLocalProxyRenderer(), new ArrayList<>(), videoCapturer, signalingParameters);

            if(signalingParameters.initiator){
                // 시간 내에 클라이언트에 응답하기 위해 SDP 를 보낸다.
                logAndToast("Creating OFFER");
                peerConnectionClient.createOffer();
            } else {
                if(params.offerSdp != null){
                    peerConnectionClient.setRemoteDescription(params.offerSdp);
                    logAndToast("Creating ANSWER");
                    peerConnectionClient.createAnswer();
                }
                if(params.iceCandidates != null){
                    // 방에서 원격 ICE 참가자를 추가한다
                    for (IceCandidate iceCandidate : params.iceCandidates){
                        peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                    }
                }
            }
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        runOnUiThread(()->{
            if(peerConnectionClient == null) {
                Log.e(TAG,"Received remote SDP for non-initialized peer connection");
                return;
            }

            peerConnectionClient.setRemoteDescription(sdp);

            if(!signalingParameters.initiator){
                logAndToast("Creating ANSWER");

                peerConnectionClient.createAnswer();
                Log.e(TAG+"::onRemoteDescription","createAnswer");
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate iceCandidate) {
        runOnUiThread(()->{
            if(peerConnectionClient == null){
                Log.e(TAG,"Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(iceCandidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        runOnUiThread(()->{
            if(peerConnectionClient == null){
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(iceCandidates);
        });
    }

    @Override
    public void onChannelClose() {
        runOnUiThread(()->{
            logAndToast("Remote end hung up; dropping Peer Connection");
            disconnect();
        });
    }

    @Override
    public void onChannelError(String description) { Log.e(TAG,"onChannelError: "+description); }

    @Override
    public void onLocalDescription(SessionDescription sessionDescription) {
        runOnUiThread(()->{
            if(rtcClient != null){
                if(signalingParameters.initiator){
                    rtcClient.sendOfferSdp(sessionDescription);
                } else{
                    rtcClient.sendAnswerSdp(sessionDescription);
                }
            }

            if(peerConnectionParameters.videoMaxBitrate > 0){
                Log.d(TAG, "Set video maximum bitrate : "+peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.e(TAG,"onIceCandidate");
        runOnUiThread(()->{
            if(rtcClient != null){
                rtcClient.sendLocalIceCandidate(iceCandidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        runOnUiThread(()->{
            if(rtcClient != null){
                rtcClient.sendLocalIceCandidateRemovals(iceCandidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(()->{
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(()->{
            logAndToast("ICE disconnected");
            iceConnected = false;
            disconnect();
        });
    }

    @Override
    public void onPeerConnectionClosed() {
        Log.e(TAG, "onPeerConnectionClosed");
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] statsReports) {
        runOnUiThread(()->{
            if(iceConnected){
                Log.e(TAG, "run : "+ Arrays.toString(statsReports));
            }
        });
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG,"onPeerConnectionError : "+s);
    }






    @Override
    public void onResume() {
        super.onResume();
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if(cameraPermission == PackageManager.PERMISSION_GRANTED){
            //presenter.startCall();
            Log.e(TAG,"start call");
        }else{
            Log.e(TAG,"camera permission error");
        }
    }


    @Override
    public void onBackPressed() {
        showDisconnectDialog();
    }

    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        runOnUiThread(()->{
            logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            logToast.show();
        });
    }

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

    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getDefaultConfig().isUseCamera2();
    }

    private boolean captureToTexture() {
        return getDefaultConfig().isCaptureToTexture();
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI(){

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

//    private void showSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }
}
