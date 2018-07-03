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

import timber.log.Timber;

/**
 * 실시간 라이브 방송을 진행하는 방송 송출자용 Activity
 *
 * WebRTC PeerConnectionClient 를 이용하여 Kurento-Media-Server 와 소켓 채널 연결을 한다.
 * 소켓 연결 후 방송 정보를 설정하고 데이터를 전송한다.
 */
public class BroadCasterActivity extends BaseActivity implements View.OnClickListener,SignalingEvents, PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = BroadCasterActivity.class.getSimpleName(); // 로그 출력을 위한 태그 설정. 현재 클래스의 이름으로 저장함.
    private static final String STREAM_HOST = "wss://13.125.64.135:8443/one2many"; // 서버에 WebRTC 스트리밍을 하기 위한 url 과 port, JS 파일 경로 설정

    private SocketService socketService; // 서버 WebSocket 과 연결하기 위한 SocketService
    private Gson gson; // 서버에서 결과값으로 넘어오거나 서버로 전달해주는 데이터들을 JSON 형식으로 만들어 보내기 위한 GSON

    private PeerConnectionClient peerConnectionClient;          // Peer 연결을 하기 위한 Client 객체
    private KurentoPresenterRTCClient rtcClient;                // Kurento 에 방송 송출자로 연결하기 위한 클라이언트 객체
    private PeerConnectionParameters peerConnectionParameters;  // Peer 연결시 사용할 파라미터 객체
    private DefaultConfig defaultConfig;        // 파라미터 객체에 기본 설정값을 불러올 때 사용하는 객체
    private RTCAudioManager audioManager;       //  WebRtc 에서 사운드 설정을 변경하기 위해 사용하는 오디오 매니저
    private SignalingParameters signalingParameters;    // 시그널링 때 사용하는 파라미터 객체

    private boolean iceConnected;       // ICE 가 연결 되었는지 저장하는 변수

    private EglBase rootEglBase;        // EGL 상태를 저장하는 EglBase, EGL -> Embedded-system Graphics Library, 크로스 렌더링 api (OpenGL) 과 윈도우 시스템 간의 인터페이스
    private ProxyRenderer localProxyRenderer;   // SurfaceView 에 장면을 렌더링하는 객체
    private Toast logToast; // 토스트 객체

    private int viewNum = 0;    // 현재 시청자의 숫자를 저장하고 있는 변수

    private SurfaceViewRenderer vGLSurfaceViewCall; // Layout 에서 화면을 표시하는 SurfaceViewRenderer

    private EditText roomNameEt;    // 방의 이름을 설정하는 EditText
    private EditText roomTagTv;     // 방의 태그를 설정하는 EditText

    private TextView liveBroadCountTv;  // 라이브 방송의 시청자 숫자를 표시하는 TextView
    private TextView liveBroadSetTv;    // 라이브 방송의 방 제목을 표시하는 TextView

    private RelativeLayout liveBroadInfoLo;     // 라이브 방송의 정보를 표시하는 레이아웃
    private RelativeLayout liveBroadCountLo;    // 라이브 방송의 시청자 숫자를 표시하는 레이아웃

    private ImageButton liveBroadCloseIb;   // 방송 시작 전 종료 버튼
    private Button liveBroadStopBtn;        // 방송 중지 버튼
    private Button liveBroadStartBtn;       // 방송 시작 버튼

    private RecyclerView liveBroadChatRv;   // 라이브 방송중 채팅 RecyclerView

    private String ownerEmail;  // 방송 호스트의 이메일
    private String ownerName;   // 방송 호스트의 이름

    private ChatAdapter chatAdapter;    // 채팅 어댑터

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        init(); // WebRtc 방송을 위한 초기화
    }

    /*
     * 필요한 데이터를 초기화한다.
     *
     * 뷰를 연결하고 클릭 리스너를 설정하고 채팅 리사이클러 뷰를 설정한다.
     * surfaceView 를 설정한다.
     */
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

        // SurfaceView 와 렌더러를 설정한다.
        localProxyRenderer = new ProxyRenderer();   // 프록시 렌더러 초기화
        rootEglBase = EglBase.create();             // EglBase 객체 생성

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);     // 서페이스 뷰 초기화
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);    // 서페이스 뷰의 크기를 화면 전체로 설정한다.
        vGLSurfaceViewCall.setEnableHardwareScaler(true);       // 서페이스 뷰의 크기를 하드웨어 크기에 맞게 설정한다.
        vGLSurfaceViewCall.setMirror(true);         // 서페이스 뷰를 반전한다.
        localProxyRenderer.setTarget(vGLSurfaceViewCall);   // localProxyRenderer 의 타겟을 서페이스 뷰로 설정한다.

        initPeerConfig();   // 서버와 WebRTC Peer Connection 설정을 한다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   // 화면에서 status bar 와 컨트롤 바를 보이지 않게한다.
    }

    /*
     * Activity 에서 사용할 View 들을 findViewById 메소드를 통해 변수와 연결한다.
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

    /* 클릭 리스너를 설정한다. */
    private void setOnClickListeners(){
        liveBroadStartBtn.setOnClickListener(this);
        liveBroadCloseIb.setOnClickListener(this);
        liveBroadStopBtn.setOnClickListener(this);
        liveBroadCountTv.setOnClickListener(this);
    }

    /* 라이브 채팅 RecyclerView 를 설정한다. */
    private void setChatRecyclerView(){
        chatAdapter = new ChatAdapter(new ArrayList<>(),getApplicationContext(),1);     // 채팅 어댑터를 초기화한다.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext()); // LinearLayoutManager 를 초기화한다.
        linearLayoutManager.setStackFromEnd(true);  // 새로운 아이템을 리스트의 끝에서 부터 붙인다.
        liveBroadChatRv.setHasFixedSize(true);  // RecyclerView 의 크기가 변경되지 않는 경우 setHasFixedSize 를 설정하여 성능을 개선한다
        liveBroadChatRv.setLayoutManager(linearLayoutManager);  // liveBroadChatRv 에 레이아웃 매니저를 설정한다.
        liveBroadChatRv.setAdapter(chatAdapter);        // liveBroadChatRv 에 어댑터를 설정한다.
        liveBroadChatRv.setItemAnimator(new DefaultItemAnimator()); // liveBroadChatRv 에 디폴트 애니메이션을 설정한다.
    }

    /*
     * WebRTC 소켓으로부터 새로운 채팅을 받았을 때 호출되는 메소드
     *
     * 새로운 채팅을 전달 받았을 경우 chatAdapter 에 새로운 채팅 객체를 생성하여 전달한다.
     * liveBroadChatRv 를 항상 최근 메시지를 보여주기 위해 스크롤을 가장 아래로 한다.
     */
    private void receiveChatMessage(String sender, String message) {
        runOnUiThread(()-> {
            chatAdapter.addChatItem(new Chat(sender,message,0));    // 새로운 채팅을 Adapter 을 통해 채팅 리스트에 추가한다.
            liveBroadChatRv.scrollToPosition(chatAdapter.getItemCount()-1); // liveBroadChatRv 를 가장 아래로 스크롤한다.
        });
    }

    /*
     * WebRTC 소켓으로부터 시청자 변화를 전달 받았을 때 호출되는 메소드
     *
     * 매개 변수로 전달받은 isUp 값에 따라 시청자 수를 늘리거나 줄인다.
     * 변화된 시청자 수를 liveBroadCountTv 에 표시한다.
     */
    @SuppressLint("SetTextI18n")
    public void setViewerNum(boolean isUp){
        if(isUp){   // 시청자가 증가했다면
            viewNum++;  // 시청자의 수를 증가시킨다.
        }else{  // 시청자가 감소했다면
            viewNum--;  // 시청자의 수를 감소시킨다.
        }
        Timber.tag("setViewerNum").d("%s", viewNum);
        runOnUiThread(()-> liveBroadCountTv.setText("시청자 "+viewNum+"명"));   // liveBroadCountTv 에 변경된 시청자 수를 표시한다.
    }

    /*
     * OnClickListener 에서 onClick 메소드를 오버라이딩하여 클릭 이벤트를 처리하는 메소드
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 방송 시작 버튼
            // 버튼을 누르게 되면 방송 제목이 있는지 확인 한 다음
            // 제목이 있을 경우 방송을 시작한다.
            // 없을 경우 방송 제목이 필요하다는 메시지를 보여준다.
            case R.id.liveBroadStartBtn:
                String roomName = roomNameEt.getText().toString();  // 방송 제목을 roomNameEt 에서 가져온다
                String roomTag = roomTagTv.getText().toString();    // 방송 태그를 roomTagTv 에서 가져온다
                // 방송 시작의 필수 사항인 방 제목이 없으면 방송을 시작하지 않는다.
                if(!roomName.isEmpty()){    // 방송 제목이 있는 경우에만 시작
                    rtcClient.setInfo(ownerEmail,roomName,roomTag); // rtcClient 에 방송을 시작하기전에 라이브 방송의 정보 (호스트 이메일, 방송 제목, 방송 태그)를 넣는다.
                    startCall();    // WebRTC PeerConnection 을 통해 서버와 연결하고 비디오와 오디오 데이터를 전송한다.
                    liveBroadStartBtn.setVisibility(View.GONE);     // 방송 시작 버튼을 보이지 않게 한다.
                    liveBroadInfoLo.setVisibility(View.GONE);       // 방송 정보 레이아웃을 보이지 않게 한다.
                    liveBroadCloseIb.setVisibility(View.GONE);      // 방송 시작전 닫기 버튼을 보이지 않게한다.
                    liveBroadSetTv.setText(ownerName+"님의 생방송");    // 방송 시작 후 누가 방송을 시작했는지 보여준다.
                    liveBroadStopBtn.setVisibility(View.VISIBLE);       // 방송 중지 버튼을 보여준다.
                    liveBroadChatRv.setVisibility(View.VISIBLE);        // 라이브 방송중 채팅 RecyclerView 를 보여준다.
                    liveBroadCountLo.setVisibility(View.VISIBLE);       // 방송 시청자 레이아웃을 표시한다.
                    hideSystemUI();     // 시스템 UI 를 가린다,
                }else{
                    // 방송 제목이 필요하다는 Toast 메시지를 띄워준다.
                    Toast.makeText(getApplicationContext(),"방송 제목을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }
                break;

            // 방송 시작 전 종료 버튼
            case R.id.liveBroadCloseIb:
                finish();   // 현재 액티비티를 종료한다.
                break;

            // 방송 시작 후 종료 버튼
            // 방송 종료 의사를 묻는 다이얼로그를 띄운 후 종료한다면 종료를, 취소하면 그대로 방송을 진행한다.
            // 종료 시 서버와 소켓 연결을 종료한다.
            case R.id.liveBroadStopBtn:
                showDisconnectDialog(); // 서버와 소켓 연결을 종료하는 의사를 묻는 다이얼로그를 띄운다.
                break;
        }
    }

    /*
     * 방송 종료 의사를 묻는 다이얼로그
     *
     * 종료 버튼을 누르게되면 서버와 Peer 연결을 끊고 방송을 종료한다.
     * 취소 버튼을 누르게되면 다이얼로그를 취소한다.
     */
    private void showDisconnectDialog(){
        runOnUiThread(()->{
            AlertDialog.Builder stopDialog = new AlertDialog.Builder(BroadCasterActivity.this,R.style.myDialog);
            stopDialog.setTitle("방송 종료")
                    .setMessage("방송을 종료하시겠습니까?")
                    .setPositiveButton("종료", (dialog, which) -> disconnect())   // 종료 버튼을 누르게되면 서버와 Peer 연결을 끊는 disconnect() 메소드를 호출한다.
                    .setNegativeButton("취소", (dialog, which) -> dialog.cancel())    // 취소 버튼을 누르게되면 다이얼로그를 취소한다.
                    .show();
        });
    }

    /*
     * PeerConfig 를 초기화한다.
     */
    public void initPeerConfig(){
        rtcClient = new KurentoPresenterRTCClient(socketService);       // 소켓 서비스를 매개 변수로 방송 송출자용 RTC Client 객체를 생성한다.
        defaultConfig = new DefaultConfig();        // 디폴트 설정을 초기화한다.
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.SEND_ONLY);  // PeerConnectionParameter 에 스트림 모드를 보내기 전용으로 변경한다.
        peerConnectionClient = PeerConnectionClient.getInstance();  // PeerConnection 객체를 초기화한다.
        peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, this ); // PeerConnection 을 생성한다.
        peerConnectionClient.setVideoEnabled(true);
    }

    /*
     * WebSocket 연결 해제
     */
    public void disconnect(){
        if(rtcClient != null){
            rtcClient = null;   // rtcClient 초기화
        }

        if(peerConnectionClient != null){
            peerConnectionClient.close();   // 피어 연결을 끊는다.
            peerConnectionClient = null;    // 피어 연결 초기화
        }

        if(audioManager != null){
            audioManager.stop();    // 오디오 매니저를 중지한다.
            audioManager = null;    // 오디오 매니저 초기화
        }

        if(socketService != null){
            socketService.close();  // 소켓 연결을 종료한다.
        }
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();   // 서페이스 뷰에서 렌더러를 release 한다.
            vGLSurfaceViewCall = null;      // 서페이스 뷰를 초기화한다.
        }
        viewNum = 0;    // 시청자 수를 0으로 초기화한다.
        finish();       // 액티비티를 종료한다.
    }

    /*
     * 서버와 PeerConnection 을 연결하는 메소드
     */
    public void startCall(){
        // rtcClient 가 없다면 에러 로그 발생 후 종료
        if(rtcClient == null){
            Timber.e("rtcClient is null");
            return;
        }
        // Kurento-media-server 와 소켓 연결을 시작한다.
        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback(){
            @Override
            public void onOpen(ServerHandshake serverHandshake) {   // 소켓이 연결 되었을 때
                super.onOpen(serverHandshake);
                logAndToast("Socket connected");
                SignalingParameters parameters = new SignalingParameters(   // Ice 서버리스트를 추가한다.
                        new LinkedList<PeerConnection.IceServer>(){
                            {
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));  // 구글에서 제공하는 STUN 서버를 추가한다
                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);  // 신호가 연결되었다면 카메라를 SurfaceView 에 연결한다.
            }

            @Override
            public void onMessage(String serverResponse_) { // 소켓에서 메시지를 받은 경우
                super.onMessage(serverResponse_);
                try{
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);   // 메시지의 JSON 형식을 풀어 ServerResponse 객체로 만든다.

                    switch (serverResponse.getIdRes()){ // serverResponse id
                        case PRESENTER_RESPONSE:    // 방송 연결 응답
                            if(serverResponse.getTypeRes() == TypeResponse.REJECTED){   // 연결이 거절되었으면
                                logAndToast(serverResponse.getMessage());   // 토스트 메시지를 띄운다.
                            }else{  // 방송 송출자로 연결이 성공 했다면
                                Timber.tag(TAG + "::presenterResponse").e(TypeResponse.ACCEPTED.toString());
                                // 서버에서 받은 SDP 를 서버 SDP 에 등록한다.
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, serverResponse.getSdpAnswer());
                                onRemoteDescription(sdp);   // 서버 sdp 를 등록한다.
                                Timber.tag(TAG + "::presenterResponse").e("onRemoteDescription");
                            }
                            break;

                        case ICE_CANDIDATE:     // 원격 ice 후보지를 받은 경우
                            Timber.tag(TAG + "::onMessage").e("ICE_CANDIDATE");
                            CandidateModel candidateModel = serverResponse.getCandidate();  // CandidateModel 객체를 가져온다.
                            onRemoteIceCandidate(   // 원격 후보지를 추가한다.
                                    new IceCandidate(candidateModel.getSdpMid(),candidateModel.getSdpMLineIndex(),candidateModel.getSdp()));
                            break;

                        case VIEWER_CHANGE:     // 시청자 변화를 전달 받은 경우
                            Timber.tag(TAG + "::onMessage").e("VIEWER_CHANGE");
                            boolean isUp = false;   // 시청자가 증가했는지 감소했는지 판단하는 변수
                            if(serverResponse.getMessage().equals("up")){       // 시청자가 증가했다면
                                isUp = true;    // isUp 을 true 로 변경한다.
                            }
                            setViewerNum(isUp); // 시청자 변화를 표시한다.
                            break;
                        case CHAT:          // 채팅 메시지를 전달 받은 경우
                            Timber.tag("chat Sender : " + serverResponse.getSender()).d("msg : %s", serverResponse.getMessage());
                            receiveChatMessage(serverResponse.getSender(), serverResponse.getMessage());    // 채팅 메시지를 표시한다.
                            break;
                    }
                }catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {   // 소켓 연결이 종료된 경우
                super.onClose(i, s, b);
                runOnUiThread(()->{
                    logAndToast("Socket closed");   // 소켓 연결이 끊어졌다는 메시지를 띄운다.
                    disconnect();   // 연결을 종료한다.
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
        Timber.tag(TAG).d("Starting audio manager");
        audioManager.start((audioDevice, availableAudioDevices) ->
                Timber.tag(TAG).d("onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                        + "selected: " + audioDevice));
    }


    public DefaultConfig getDefaultConfig() { return defaultConfig; }   // WebRTC 파라미터의 기본 설정을 불러온다.

    private void callConnected(){
        if(peerConnectionClient == null){
            Timber.tag(TAG).w("Call is connected in closed or error state");
            return;
        }

        // 통계 콜백 활성화
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    /*
     * 서버와 소켓이 연결 되었을 때
     *
     * 1. 비디오 캡쳐를 카메라에서 가져온다.
     * 2. Peer 연결을 만든다. 피어에 내 비디오 데이터를 넘긴다.
     */
    @Override
    public void onSignalConnected(SignalingParameters params) {
        runOnUiThread(()->{
            signalingParameters = params;
            // 1. 비디오 캡쳐를 카메라에서 가져온다.
            VideoCapturer videoCapturer = null;
            if(peerConnectionParameters.videoCallEnabled){
                videoCapturer = createVideoCapturer();  // 비디오 캡쳐러 초기화
            }
            // 2. Peer 연결을 만든다. 피어에 내 비디오 데이터를 넘긴다.
            peerConnectionClient.createPeerConnection(getEglBaseContext(), getLocalProxyRenderer(), new ArrayList<>(), videoCapturer, signalingParameters);

            // 매개변수로 전달받은 파라미터에 initiator 가 존재한다면
            if(signalingParameters.initiator){
                // 시간 내에 클라이언트에 응답하기 위해 SDP 를 보낸다.
                logAndToast("Creating OFFER");
                peerConnectionClient.createOffer(); // 응답을 생성한다.
            } else {
                if(params.offerSdp != null){    // 매개변수로 전달받은 시그널 파라미터에 offerSdp 를 전달 받았다면
                    peerConnectionClient.setRemoteDescription(params.offerSdp); // 원격지 SDP 에 offerSDP 정보를 넣는다.
                    logAndToast("Creating ANSWER");
                    peerConnectionClient.createAnswer();    // sdp 응답을 생성한다.
                }
                if(params.iceCandidates != null){   // 매개변수로 전달받은 시그널 파라미터에 IceCandidates 가 존재한다면
                    // 방에서 원격 ICE 참가자를 추가한다
                    for (IceCandidate iceCandidate : params.iceCandidates){
                        peerConnectionClient.addRemoteIceCandidate(iceCandidate);   // 원격 ice 후보지를 추가한다.
                    }
                }
            }
        });
    }

    /*
     * 서버에서 받은 SDP 를 등록한다.
     */
    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        runOnUiThread(()->{
            if(peerConnectionClient == null) {  // PeerConnectionClient 객체가 없다면 메소드를 종료한다.
                Timber.tag(TAG).e("Received remote SDP for non-initialized peer connection");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp); // 서버에서 받은 SDP 를 원격지 sdp 로 등록한다.

            if(!signalingParameters.initiator){ // 서버에서 받은 sdp 가 offer 를 새로 시작하는게 아니였다면
                logAndToast("Creating ANSWER");

                peerConnectionClient.createAnswer();    // answer SDF 를 생성한다.
                Timber.tag(TAG + "::onRemoteDescription").e("createAnswer");
            }
        });
    }

    /*
     * 서버에서 응답받은 Ice candidate 를 peerConnectionClient ice 에 추가한다.
     */
    @Override
    public void onRemoteIceCandidate(IceCandidate iceCandidate) {
        runOnUiThread(()->{
            if(peerConnectionClient == null){
                Timber.tag(TAG).e("Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(iceCandidate);   // peerConnectionClient ice 에 추가한다.
        });
    }

    /*
     * ICE Candidate 가 삭제 됐을 때 호출되는 메소드
     */
    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        runOnUiThread(()->{
            if(peerConnectionClient == null){
                Timber.tag(TAG).e("Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(iceCandidates);  // peerConnectionClient 에서 ice candidate 를 삭제한다.
        });
    }

    /*
     * WebRTC 채널이 닫혔을 때 호출되는 메소드
     *
     * Web socket 연결을 닫는다.
     */
    @Override
    public void onChannelClose() {
        runOnUiThread(()->{
            logAndToast("Remote end hung up; dropping Peer Connection");
            disconnect();   // web socket 연결을 닫는다.
        });
    }

    @Override
    public void onChannelError(String description) { Timber.tag(TAG).e("onChannelError: %s", description); }    // WebRTC 채널 에러 발생시 로그 출력

    /*
     * 디바이스의 SDP 를 서버로 전송한다.
     *
     * signalingParameters.initiator 가 true 라면 나의 sdp 로 offer 를 생성하고,
     * false 라면 answer 를 생성하여 전송한다.
     */
    @Override
    public void onLocalDescription(SessionDescription sessionDescription) {
        runOnUiThread(()->{
            if(rtcClient != null){
                if(signalingParameters.initiator){
                    rtcClient.sendOfferSdp(sessionDescription);     // 나의 sdp 로 offer 생성 후 전송
                } else{
                    rtcClient.sendAnswerSdp(sessionDescription);    // 나의 sdp 로 answer 생성 후 전송
                }
            }

            if(peerConnectionParameters.videoMaxBitrate > 0){   // SDP 에 비트레이트가 명시 되어 있는 경우
                Timber.tag(TAG).d("Set video maximum bitrate : %s", peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);  // 비디오 비트레이트를 설정한다.
            }
        });
    }

    /* WebRTC 소켓 채널로 디바이스의 iceCandidate 를 전송한다. */
    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Timber.tag(TAG).e("onIceCandidate");
        runOnUiThread(()->{
            if(rtcClient != null){
                rtcClient.sendLocalIceCandidate(iceCandidate); // WebRTC 소켓 채널로 디바이스의 iceCandidate 를 전송한다.
            }
        });
    }

    /* IceCandidate 삭제 */
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        runOnUiThread(()->{
            if(rtcClient != null){
                rtcClient.sendLocalIceCandidateRemovals(iceCandidates); // IceCandidate 삭제
            }
        });
    }

    /* IceCandidate 와 연결이 성공했을 때 호출되는 메소드 */
    @Override
    public void onIceConnected() {
        runOnUiThread(()->{
            iceConnected = true;
            callConnected();
        });
    }

    /* IceCandidate 와 연결이 끊켰을 때 호출되는 메소드 */
    @Override
    public void onIceDisconnected() {
        runOnUiThread(()->{
            logAndToast("ICE disconnected");
            iceConnected = false;
            disconnect();   // 연결을 종료한다.
        });
    }

    @Override
    public void onPeerConnectionClosed() { Timber.tag(TAG).e("onPeerConnectionClosed"); } // 피어 연결이 끊킨 경우 로그를 출력한다.

    /* Ice Server 와 연결 상태를 출력하는 메소드 */
    @Override
    public void onPeerConnectionStatsReady(StatsReport[] statsReports) {
        runOnUiThread(()->{
            if(iceConnected){       // Ice 와 연결 되어 있는 경우
                Timber.tag(TAG).e("run : %s", Arrays.toString(statsReports));   //  ICE 연결 상태를 출력한다.
            }
        });
    }

    @Override
    public void onPeerConnectionError(String s) { Timber.tag(TAG).e("onPeerConnectionError : %s", s); } // 피어 연결에서 에러가 생긴 경우 로그를 출력한다.

    /*
     * Activity 가 onResume() 되었을 때 카메라 권한을 확인한다.
     */
    @Override
    public void onResume() {
        super.onResume();
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if(cameraPermission == PackageManager.PERMISSION_GRANTED){
            //presenter.startCall();
            Timber.tag(TAG).e("start call");
        }else{
            Timber.tag(TAG).e("camera permission error");
        }
    }

    /* 뒤로가기 버튼을 눌렀을 때 방송을 종료할 것인지 물어보는 다이얼로그를 띄운다. */
    @Override
    public void onBackPressed() {
        showDisconnectDialog(); // 다이얼로그를 띄운다.
    }

    /*
     * 로그와 토스트 메시지를 출력하는 메소드
     */
    public void logAndToast(String msg) {
        Timber.tag(TAG).d(msg);
        if (logToast != null) {
            logToast.cancel();
        }
        runOnUiThread(()->{
            logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);   // 토스트 초기화
            logToast.show();    // 토스트 출력
        });
    }

    /*
     * 카메레에서 비디오 출력을 받아 videoCapture 를 초기화하고 리턴하는 메소드
     */
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

    public EglBase.Context getEglBaseContext() { return rootEglBase.getEglBaseContext(); }  // rootEglBase 의 getEglBaseContext 를 리턴한다.

    public VideoRenderer.Callbacks getLocalProxyRenderer() { return localProxyRenderer; }   // localProxyRenderer 를 리턴한다.

    /* Camera2Enumerator 를 지원하면서 디폴트 설정이 카메라2를 허용하면 트루를 리턴한다. */
    private boolean useCamera2() { return Camera2Enumerator.isSupported(this) && getDefaultConfig().isUseCamera2(); }

    /* capture 를 Texture 로 변환하는 설정이 가능한지 리턴한다. */
    private boolean captureToTexture() { return getDefaultConfig().isCaptureToTexture(); }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // 전면 카메라가 사용 가능한지 확인한다.
        // 사용 가능하면 전면 카메라를 사용하여 VideoCapturer 객체를 초기화한다.
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) { // 전면 카메라가 가능하다면
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);  // VideoCapturer 를 전면 카메라를 이용해 초기화한다.

                if (videoCapturer != null) {
                    return videoCapturer;   // videoCapturer 가 초기화 되었다면 return 해준다.
                }
            }
        }

        // 전면 카메라가 없다면 전면 카메라가 아닌 카메라를 사용해 VideoCapturer 객체를 초기화한다.
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer; // videoCapturer 가 초기화 되었다면 return 해준다.
                }
            }
        }
        return null;
    }

    /* 포커스가 변경 되었을 때 포커스가 있다면 시스템 UI 를 숨긴다. */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /*
     * 시스템 UI 를 숨긴다.
     */
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
}
