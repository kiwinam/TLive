package com.testexam.charlie.tlive.main.live.webrtc.viewer;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.testexam.charlie.tlive.common.kurento.KurentoViewerRTCClient;
import com.testexam.charlie.tlive.common.kurento.models.CandidateModel;
import com.testexam.charlie.tlive.common.kurento.models.response.ServerResponse;
import com.testexam.charlie.tlive.common.kurento.models.response.TypeResponse;
import com.testexam.charlie.tlive.main.live.webrtc.broadChat.Chat;
import com.testexam.charlie.tlive.main.live.webrtc.broadChat.ChatAdapter;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import timber.log.Timber;


/**
 * WebRTC SFU 방식으로 라이브 스트리밍을 시청하는 Activity.
 *
 */
public class ViewerActivity extends BaseActivity implements SignalingEvents, PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = ViewerActivity.class.getSimpleName();       // 로그를 출력할 때, 어떤 Activity 에서 출력하는지 확인하기 위한 태그
    private static final String STREAM_HOST = "wss://13.125.64.135:8443/one2many";  // 스트리밍을 전송 받는 서버의 URL, Server.js 에서 해당 URL 로 스트리밍을 전송한다.

    private SocketService socketService;    // 서버와 통신하기 위한 WebSocket
    private Gson gson;                      // 서버와 통신할 때 JSON 형식을 사용해서 통신하는데 이 때 JSON 형태를 다루기 위해 사용하는 GSON 객체

    private PeerConnectionClient peerConnectionClient;  // Peer 연결을 하기 위한 Client 객체
    private KurentoViewerRTCClient rtcClient;   // Kurento 에 시청자 연결하기 위한
    private PeerConnectionParameters peerConnectionParameters;  // Peer 연결시 사용할 파라미터 객체
    private RTCAudioManager audioManager;   //  WebRtc 에서 사운드 설정을 변경하기 위해 사용하는 오디오 매니저
    private SignalingParameters signalingParameters;     // 시그널링 때 사용하는 파라미터 객체
    private boolean iceConnected;       // ICE 가 연결 되었는지 저장하는 변수

    private EglBase rootEglBase;    // EGL 상태를 저장하는 EglBase, EGL -> Embedded-system Graphics Library, 크로스 렌더링 api (OpenGL) 과 윈도우 시스템 간의 인터페이스
    private ProxyRenderer remoteProxyRenderer;  // SurfaceView 에 장면을 렌더링하는 객체
    private Toast logToast; // 토스트 객체

    private SurfaceViewRenderer vGLSurfaceViewCall; // Layout 에서 화면을 표시하는 SurfaceViewRenderer

    private int presenterSessionId; // BJ 의 Node.js 상 세션 아이디

    private String name;    // 시청자의 이름

    private ChatAdapter chatAdapter; // 채팅 recycler view 와 연결하는 어댑터
    private RecyclerView chatRecyclerView; // 채팅 RecyclerView
    private EditText viewerChatEt;  // 채팅 내용을 입력하는 EditText
    private Button viewerChatSendBtn;   // 채팅 전송 버튼

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        name = getSharedPreferences("login",MODE_PRIVATE).getString("name",null);
        presenterSessionId = getIntent().getIntExtra("presenterSessionId",-1);
        init(); // 변수 및 뷰 초기화
    }

    /*
     * 변수 및 뷰 초기화
     *
     * 뷰를 연결하고 클릭 리스너를 설정하고 채팅 리사이클러 뷰를 설정한다.
     * surfaceView 를 설정한다.
     */
    private void init() {
        //config peer
        vGLSurfaceViewCall = findViewById(R.id.vGLSurfaceViewCall); // 서페이스 뷰 객체에 뷰 연결
        socketService = new DefaultSocketService(getApplication()); // 소켓 연결
        gson = new Gson();
        remoteProxyRenderer = new ProxyRenderer();  // 프록시 렌더러 초기화
        rootEglBase = EglBase.create(); // EglBase 객체 생성

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);     // 서페이스 뷰 초기화
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);    // 서페이스 뷰의 크기를 화면 전체로 설정한다.
        vGLSurfaceViewCall.setEnableHardwareScaler(true);       // 서페이스 뷰의 크기를 하드웨어 크기에 맞게 설정한다.
        vGLSurfaceViewCall.setMirror(true);     // 서페이스 뷰를 반전한다.
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);      // remoteProxyRenderer 의 타겟을 서페이스 뷰로 설저한다.

        viewerChatEt = findViewById(R.id.viewerChatEt);     // 채팅 내용을 입력하는 EditText
        viewerChatSendBtn = findViewById(R.id.viewerChatSendBtn);   // 채팅 전송 버튼

        /*
         * 채팅 전송 버튼 리스너
         * 채팅 전송 버튼을 누르게 되면 현재 로그인하고 있는 사용자의 이름과 채팅 내용을 JSON Object 로 만든다.
         * 만들어진 JSON Object 는 현재 연결된 WebRTC Peer socket 으로 전송된다.
         * 전송한 후 자신의 채팅 어댑터에 전송된 채팅 데이터를 추가한다.
         */
        viewerChatSendBtn.setOnClickListener(v -> {
            try{
                JSONObject chatObj = new JSONObject();
                chatObj.put("id","chat");
                chatObj.put("sender",name);
                chatObj.put("message",viewerChatEt.getText().toString());
                socketService.sendMessage(chatObj.toString());
                runOnUiThread(()->{
                    chatAdapter.addChatItem(new Chat(name,viewerChatEt.getText().toString(),0));
                    viewerChatEt.setText("");
                });
            } catch (JSONException e){
                e.printStackTrace();
            }

        });

        /*
         * 채팅 내용이 있을 때만 채팅 보내기 버튼이 활성화 되도록 하는 리스너
         * 채팅 내용이 있으면 채팅 보내기 버튼이 활성화 된다.
         * 채팅 내용이 없다면 채팅 보내기 버튼이 비활성화 된다.
         */
        viewerChatEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @RequiresApi(api = Build.VERSION_CODES.M) // getColor 를 지원하는 API 21 버전부터 사용가능 , 현재 프로젝트의 MinSDK 가 21 이여서 어노테이션을 추가했다.
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count != 0){ // 텍스트의 총 길이가 0이 아닐 경우, 텍스트가 있을 경우는 보내기 버튼 활성화
                    viewerChatSendBtn.setTextColor(getColor(R.color.colorPrimary));
                    viewerChatSendBtn.setClickable(true);
                }else{          // 텍스트의 총 합이 0 일 경우는 아무런 글자도 입력되지 않을 때 이므로, 채팅 보내기 버튼을 비활성화 한다.
                    viewerChatSendBtn.setTextColor(getColor(R.color.colorGray));
                    viewerChatSendBtn.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        initPeerConfig();   // 피어 설정
        setChatRecyclerView();  // 채팅 리사이클러뷰 설정
    }

    /* 채팅 리사이클러뷰를 설정하는 메소드 */
    private void setChatRecyclerView(){
        chatAdapter = new ChatAdapter(new ArrayList<>(), getApplicationContext(),0);

        chatRecyclerView = findViewById(R.id.viewerChatRv);
        chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
    }

    /*
     *
     * WebRTC 소켓으로부터 새로운 채팅을 받았을 때 호출되는 메소드
     *
     * 새로운 채팅을 전달 받았을 경우 chatAdapter 에 새로운 채팅 객체를 생성하여 전달한다.
     * chatRecyclerView 를 항상 최근 메시지를 보여주기 위해 스크롤을 가장 아래로 한다.
     */
    private void receiveChatMessage(String sender, String message){
        runOnUiThread(() ->{
            chatAdapter.addChatItem(new Chat(sender, message,0));
            chatRecyclerView.scrollToPosition(chatAdapter.getItemCount()-1);
        });
    }

    public EglBase.Context getEglBaseContext() { return rootEglBase.getEglBaseContext(); }   // EglBase 컨텍스트를 리턴한다.

    public VideoRenderer.Callbacks getRemoteProxyRenderer() { return remoteProxyRenderer; } // 리모트 렌더러를 리턴한다.

    /*
     * PeerConfig 를 초기화한다.
     */
    public void initPeerConfig() {
        rtcClient = new KurentoViewerRTCClient(socketService);  // 소켓 서비스를 매개 변수로 방송 송출자용 RTC Client 객체를 생성한다.
        DefaultConfig defaultConfig = new DefaultConfig();   // 디폴트 설정을 초기화한다.
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.RECV_ONLY);  // PeerConnectionParameter 에 스트림 모드를 받기 전용으로 변경한다.
        peerConnectionClient = PeerConnectionClient.getInstance();  // PeerConnection 객체를 초기화한다.
        peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, this);   // PeerConnection 을 생성한다.
        rtcClient.setPresenterSID(presenterSessionId);  // 방송 송출자의 세션 아이디를 설정한다.
        startCall();    // Peer 연결을 시작한다.
    }

    /*
     * 서버와 PeerConnection 을 연결하는 메소드
     */
    public void startCall() {
        if (rtcClient == null) {    // rtcClient 가 없다면 에러 로그 발생 후 종료
            Timber.tag(TAG).e("AppRTC client is not allocated for a call.");
            return;
        }

        // Kurento-media-server 와 소켓 연결을 시작한다.
        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {   // 소켓이 연결 되었을 때
                super.onOpen(serverHandshake);

                logAndToast("Socket connected");
                SignalingParameters parameters = new SignalingParameters(    // Ice 서버리스트를 추가한다.
                        new LinkedList<PeerConnection.IceServer>() {
                            {
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));  // 구글에서 제공하는 STUN 서버를 추가한다
                                //add(new PeerConnection.IceServer("kurento:kurentopw@turn:13.125.64.135"));
                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);    // 신호가 연결되었다면 카메라를 SurfaceView 에 연결한다.
            }

            @Override
            public void onMessage(String serverResponse_) { // 소켓에서 메시지를 받은 경우
                super.onMessage(serverResponse_);
                try {
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);   // 메시지의 JSON 형식을 풀어 ServerResponse 객체로 만든다.

                    switch (serverResponse.getIdRes()) {    // serverResponse id
                        case VIEWER_RESPONSE:   // 방송 연결 응답
                            if (serverResponse.getTypeRes() == TypeResponse.REJECTED) { // 연결이 거절 되었으면
                                logAndToast(serverResponse.getMessage());   // 토스트 메시지를 띄운다.
                            } else {    // 방송 시청자로 연결이 성공 했다면
                                // 서버에서 받은 SDP 를 서버 SDP 에 등록한다.
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, serverResponse.getSdpAnswer());
                                onRemoteDescription(sdp);   // 서버 sdp 를 등록한다.
                            }
                            break;
                        case ICE_CANDIDATE:  // 원격 ice 후보지를 받은 경우
                            CandidateModel candidateModel = serverResponse.getCandidate();  // CandidateModel 객체를 가져온다.
                            onRemoteIceCandidate(   // 원격 후보지를 추가한다.
                                    new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(), candidateModel.getSdp()));
                            break;
                        case STOP_COMMUNICATION:    // 방송 종료 메시지를 받은 경우
                            stopCommunication();    // 방송 시청을 종료한다.
                            break;
                        case CHAT:      // 채팅 메시지를 전달 받은 경우
                            Timber.tag("chat Sender : " + serverResponse.getSender()).d("msg : %s", serverResponse.getMessage());
                            receiveChatMessage(serverResponse.getSender(), serverResponse.getMessage());    // 채팅 메시지를 표시한다.
                            break;
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {   // 소켓 연결이 종료된 경우
                super.onClose(i, s, b);
                logAndToast("Socket closed");   // 소켓 연결이 끊어졌다는 메시지를 띄운다.
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                logAndToast(e.getMessage());
            }

        });

        // 오디오 라우팅 (오디오 모드, 오디오 장치 열거 등)을 처리할 오디오 관리자 만듬.
        audioManager = RTCAudioManager.create(getApplicationContext());
        // 기존 오디오 설정 저장 및 오디오 모드 변경
        // 가능한 최상의 VoIP 성능을 제공
        Timber.tag(TAG).d("Starting the audio manager...");
        audioManager.start((audioDevice, availableAudioDevices) ->
                Timber.tag(TAG).d("onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                        + "selected: " + audioDevice));
    }

    private void callConnected() {
        if (peerConnectionClient == null) {
            Timber.w("Call is connected in closed or error state");
            return;
        }
        // 통계 콜백 활성화
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    /*
     * 서버와 소켓 연결이 되었을 때
     */
    @Override
    public void onSignalConnected(SignalingParameters params) {
        runOnUiThread(()->{
            signalingParameters = params;
            peerConnectionClient
                    .createPeerConnection(getEglBaseContext(), null,    // peerConnection 를 생성한다.
                            getRemoteProxyRenderer(), null, // PeerConnection 에서 받아온 데이터를 proxy 렌더러에 연결한다.
                            signalingParameters);

            // 매개변수로 전달받은 파라미터에 initiator 가 존재한다면
            if (signalingParameters.initiator) {
                // Offer 를 생성한다.
                // Offer SDP 는 PeerConnectionEvent 에서 클라이언트에 응답하기 위해 SDP 가 전송된다.
                logAndToast("Creating OFFER...");
                peerConnectionClient.createOffer();
            } else {
                if (params.offerSdp != null) { // 매개변수로 전달받은 시그널 파라미터에 offerSdp 를 전달 받았다면
                    peerConnectionClient.setRemoteDescription(params.offerSdp); // 원격지 SDP 에 offerSDP 정보를 넣는다.
                    peerConnectionClient.createAnswer();  // sdp 응답을 생성한다.
                }
                if (params.iceCandidates != null) { // 매개변수로 전달받은 시그널 파라미터에 IceCandidates 가 존재한다면
                    // 방에서 원격 ICE 참가자를 추가한다
                    for (IceCandidate iceCandidate : params.iceCandidates) {
                        peerConnectionClient.addRemoteIceCandidate(iceCandidate);  // 원격 ice 후보지를 추가한다.
                    }
                }
            }
        });
    }

    /*
     * WebSocket 연결 해제
     */
    public void disconnect(boolean isFinish) {
        runOnUiThread(()->{
            remoteProxyRenderer.setTarget(null);    // 렌더러 타겟 초기화
            if (vGLSurfaceViewCall != null) {
                vGLSurfaceViewCall.release(); // 피어 연결을 끊는다.
                vGLSurfaceViewCall = null; // 피어 연결 초기화
            }
            if (rtcClient != null) {
                rtcClient = null;   // rtcClient 초기화
            }
            if (peerConnectionClient != null) {
                peerConnectionClient.close();   // 서페이스 뷰에서 렌더러를 release 한다.
                peerConnectionClient = null;    // 서페이스 뷰를 초기화한다.
            }

            if (audioManager != null) {
                audioManager.stop();    // 오디오 매니저를 중지한다.
                audioManager = null;    // 오디오 매니저 초기화
            }

            if (socketService != null) {
                socketService.close();  // 소켓 연결을 종료한다.
            }
            if(isFinish){
                finish();   // 액티비티를 종료한다.
            }
        });
    }

    /* 뒤로가기 버튼을 누르면 disconnect() 메소드를 호출한다. */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        disconnect(true);
    }

    /* 방송 송출자가 방송을 종료하면 onBackPressed() 메소드를 호출한다. */
    public void stopCommunication() {
        onBackPressed();
    }

    /* 로그와 토스트를 띄우는 메소드 */
    public void logAndToast(String msg) {
        Timber.tag(TAG).d(msg);
        if (logToast != null) {
            logToast.cancel();
        }
        runOnUiThread(()->{
            logToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
            logToast.show();
        });
    }

    /*
     * 서버에서 받은 SDP 를 등록한다.
     */
    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        runOnUiThread(()->{
            if (peerConnectionClient == null) { // PeerConnectionClient 객체가 없다면 메소드를 종료한다.
                Timber.tag(TAG).e("Received remote SDP for non-initialized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp); // 서버에서 받은 SDP 를 원격지 sdp 로 등록한다.
            if (!signalingParameters.initiator) {   // 서버에서 받은 sdp 가 offer 를 새로 시작하는게 아니였다면
                logAndToast("Creating ANSWER...");
                peerConnectionClient.createAnswer();    // answer SDF 를 생성한다.
                Timber.tag(TAG + "::onRemoteDescription").e("createAnswer");
            }
        });
    }

    /*
     * 서버에서 응답받은 Ice candidate 를 peerConnectionClient ice 에 추가한다.
     */
    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        runOnUiThread(()->{
            if (peerConnectionClient == null) {
                Timber.tag(TAG).e("Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);  // peerConnectionClient ice 에 추가한다.
        });
    }

    /*
     * ICE Candidate 가 삭제 됐을 때 호출되는 메소드
     */
    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(()->{
            if (peerConnectionClient == null) {
                Timber.tag(TAG).e("Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates); // peerConnectionClient 에서 ice candidate 를 삭제한다.
        });
    }

    /* WebRTC 채널이 닫혔을 때 호출되는 메소드 */
    @Override
    public void onChannelClose() { disconnect(true); }  // WebSocket 연결을 닫는다.

    @Override
    public void onChannelError(String description) { Timber.tag(TAG).e("onChannelError: %s", description); }    // WebRTC 채널 에러 발생 시 로그 출력

    /*
     * 디바이스의 SDP 를 서버로 전송한다.
     *
     * signalingParameters.initiator 가 true 라면 나의 sdp 로 offer 를 생성하고,
     * false 라면 answer 를 생성하여 전송한다.
     */
    @Override
    public void onLocalDescription(SessionDescription sdp) {
        if (rtcClient != null) {
            if (signalingParameters.initiator) {
                rtcClient.sendOfferSdp(sdp);    // 나의 sdp 로 offer 생성 후 전송
            } else {
                rtcClient.sendAnswerSdp(sdp);    // 나의 sdp 로 answer 생성 후 전송
            }
        }
        if (peerConnectionParameters.videoMaxBitrate > 0) { // SDP 에 비트레이트가 명시 되어 있는 경우
            Timber.tag(TAG).d("Set video maximum bitrate: %s", peerConnectionParameters.videoMaxBitrate);
            peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);  // 비디오 비트레이트를 설정한다.
        }
    }

    /* WebRTC 소켓 채널로 디바이스의 iceCandidate 를 전송한다. */
    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Timber.tag(TAG).e("onIceCandidate");
        runOnUiThread(()->{
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidate(candidate); // WebRTC 소켓 채널로 디바이스의 iceCandidate 를 전송한다.
            }
        });
    }

    /* IceCandidate 삭제 */
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(()->{
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidateRemovals(candidates);    // IceCandidate 삭제
            }
        });
    }

    /*
     * Ice 서버와 연결이 된 경우.
     * iceConnected 에 true 를 입력한다.
     * callConnected 함수를 호출하여 PeerConnectionClient 의 상태를 Event enable 로 변경한다.
     */
    @Override
    public void onIceConnected() {
        runOnUiThread(()->{
            iceConnected = true;
            callConnected();
        });
    }

    /*
     * ICE 서버와 연결이 해제된 경우.
     * 토스트 메시지를 띄우고 disconnect() 메서드를 호출하여 Peer 연결을 해제한다.
     */
    @Override
    public void onIceDisconnected() {
        logAndToast("ICE disconnected");
        iceConnected = false;
        disconnect(true);
    }

    @Override
    public void onPeerConnectionClosed() { Timber.tag(TAG).e("onPeerConnectionClosed: "); }   // 피어 연결이 끊킨 경우 로그를 출력한다.

    /* Ice Server 와 연결 상태를 출력하는 메소드 */
    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        if (iceConnected) { // Ice 와 연결 되어 있는 경우
            Timber.tag(TAG).e("run: %s", Arrays.toString(reports)); // ICE 연결 상태를 출력한다.
        }
    }

    /*
     * 서버와 Peer 연결이 에러난 경우
     * 로그를 출력하여 로그 메시지를 확인한다.
     */
    @Override
    public void onPeerConnectionError(String description) { Timber.tag(TAG).e("onPeerConnectionError: %s", description); }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){// 세로 전환시
            setContentView(R.layout.activity_viewer);
            remoteProxyRenderer.setTarget(vGLSurfaceViewCall);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { // 가로 전환시
            setContentView(R.layout.activity_viewer_land);
//            disconnect(false);
//            init();
            remoteProxyRenderer.setTarget(vGLSurfaceViewCall);
        }
    }
}