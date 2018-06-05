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


/**
 * WebRTC SFU 방식으로 라이브 스트리밍을 시청하는 Activity.
 *
 */
public class ViewerActivity extends BaseActivity implements SignalingEvents, PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = ViewerActivity.class.getSimpleName();       // 로그를 출력할 때, 어떤 Activity 에서 출력하는지 확인하기 위한 태그
    private static final String STREAM_HOST = "wss://13.125.64.135:8443/one2many";  // 스트리밍을 전송 받는 서버의 URL, Server.js 에서 해당 URL 로 스트리밍을 전송한다.

    private SocketService socketService;    // 서버와 통신하기 위한 WebSocket
    private Gson gson;                      // 서버와 통신할 때 JSON 형식을 사용해서 통신하는데 이 때 JSON 형태를 다루기 위해 사용하는 GSON 객체

    private PeerConnectionClient peerConnectionClient;
    private KurentoViewerRTCClient rtcClient;
    private PeerConnectionParameters peerConnectionParameters;
    private RTCAudioManager audioManager;
    private SignalingParameters signalingParameters;
    private boolean iceConnected;

    private EglBase rootEglBase;
    private ProxyRenderer remoteProxyRenderer;
    private Toast logToast;

    private SurfaceViewRenderer vGLSurfaceViewCall;

    private int presenterSessionId;

    private String name;

    private ChatAdapter chatAdapter; // 채팅 recycler view 와 연결하는 어댑터

    private EditText viewerChatEt;
    private Button viewerChatSendBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        name = getSharedPreferences("login",MODE_PRIVATE).getString("name",null);
        presenterSessionId = getIntent().getIntExtra("presenterSessionId",-1);
        init();
    }

    /*
     *
     */
    private void init() {
        //config peer
        vGLSurfaceViewCall = findViewById(R.id.vGLSurfaceViewCall);
        socketService = new DefaultSocketService(getApplication());
        gson = new Gson();
        remoteProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);

        viewerChatEt = findViewById(R.id.viewerChatEt);
        viewerChatSendBtn = findViewById(R.id.viewerChatSendBtn);

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
                    chatAdapter.setData(new Chat(name,viewerChatEt.getText().toString()));
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

        initPeerConfig();
        setChatRecyclerView();
    }

    private void setChatRecyclerView(){
        chatAdapter = new ChatAdapter(new ArrayList<>(), getApplicationContext(),0);

        RecyclerView chatRecyclerView = findViewById(R.id.viewerChatRv);
        chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(linearLayoutManager);

    }

    private void receiveChatMessage(String sender, String message){
        runOnUiThread(() -> chatAdapter.setData(new Chat(sender, message)));
    }

    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    public VideoRenderer.Callbacks getRemoteProxyRenderer() {
        return remoteProxyRenderer;
    }

    public void initPeerConfig() {
        rtcClient = new KurentoViewerRTCClient(socketService);
        DefaultConfig defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.RECV_ONLY);
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, this);
        rtcClient.setPresenterSID(presenterSessionId);
        startCall();
    }

    public void startCall() {
        if (rtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }

        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                super.onOpen(serverHandshake);

                logAndToast("Socket connected");
                SignalingParameters parameters = new SignalingParameters(
                        new LinkedList<PeerConnection.IceServer>() {
                            {
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
                                //add(new PeerConnection.IceServer("kurento:kurentopw@turn:13.125.64.135"));
                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);
            }

            @Override
            public void onMessage(String serverResponse_) {
                super.onMessage(serverResponse_);
                try {
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);

                    switch (serverResponse.getIdRes()) {
                        case VIEWER_RESPONSE:
                            if (serverResponse.getTypeRes() == TypeResponse.REJECTED) {
                                logAndToast(serverResponse.getMessage());
                            } else {
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, serverResponse.getSdpAnswer());
                                onRemoteDescription(sdp);
                            }
                            break;
                        case ICE_CANDIDATE:
                            CandidateModel candidateModel = serverResponse.getCandidate();
                            onRemoteIceCandidate(
                                    new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(), candidateModel.getSdp()));
                            break;
                        case STOP_COMMUNICATION:
                            stopCommunication();
                            break;
                        case CHAT:
                            Log.d("chat Sender : "+serverResponse.getSender(), "msg : "+serverResponse.getMessage());
                            receiveChatMessage(serverResponse.getSender(), serverResponse.getMessage());
                            break;
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                super.onClose(i, s, b);
                logAndToast("Socket closed");
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                logAndToast(e.getMessage());
            }

        });

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = RTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start((audioDevice, availableAudioDevices) ->
                Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                        + "selected: " + audioDevice));
    }

//    public DefaultConfig getDefaultConfig() {
//        return defaultConfig;
//    }

    private void callConnected() {
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, 1000);
    }


    @Override
    public void onSignalConnected(SignalingParameters params) {
        runOnUiThread(()->{
            signalingParameters = params;
            peerConnectionClient
                    .createPeerConnection(getEglBaseContext(), null,
                            getRemoteProxyRenderer(), null,
                            signalingParameters);

            if (signalingParameters.initiator) {
                // Offer 를 생성한다.
                // Offer SDP 는 PeerConnectionEvent 에서 클라이언트에 응답하기 위해 SDP 가 전송된다.
                logAndToast("Creating OFFER...");
                peerConnectionClient.createOffer();
            } else {
                if (params.offerSdp != null) {
                    peerConnectionClient.setRemoteDescription(params.offerSdp);
                    //if (isViewAttached()) getView().logAndToast("Creating ANSWER...");
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
                if (params.iceCandidates != null) {
                    // Add remote ICE candidates from room.
                    for (IceCandidate iceCandidate : params.iceCandidates) {
                        peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                    }
                }
            }
        });
    }



    public void disconnect(boolean isFinish) {
        runOnUiThread(()->{
            remoteProxyRenderer.setTarget(null);
            if (vGLSurfaceViewCall != null) {
                vGLSurfaceViewCall.release();
                vGLSurfaceViewCall = null;
            }
            if (rtcClient != null) {
                rtcClient = null;
            }
            if (peerConnectionClient != null) {
                peerConnectionClient.close();
                peerConnectionClient = null;
            }

            if (audioManager != null) {
                audioManager.stop();
                audioManager = null;
            }

            if (socketService != null) {
                socketService.close();
            }
            if(isFinish){
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnect(true);
    }

    public void stopCommunication() {
        onBackPressed();
    }

    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        runOnUiThread(()->{
            logToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
            logToast.show();
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        runOnUiThread(()->{
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                logAndToast("Creating ANSWER...");
                peerConnectionClient.createAnswer();
                Log.e(TAG + "::onRemoteDescription", "createAnswer");
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        runOnUiThread(()->{
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(()->{
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        disconnect(true);
    }

    @Override
    public void onChannelError(String description) {
        Log.e(TAG, "onChannelError: " + description);
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        if (rtcClient != null) {
            if (signalingParameters.initiator) {
                rtcClient.sendOfferSdp(sdp);
            } else {
                rtcClient.sendAnswerSdp(sdp);
            }
        }
        if (peerConnectionParameters.videoMaxBitrate > 0) {
            Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
            peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
        }
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Log.e(TAG, "onIceCandidate");
        runOnUiThread(()->{
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        runOnUiThread(()->{
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidateRemovals(candidates);
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
    public void onPeerConnectionClosed() {
        Log.e(TAG, "onPeerConnectionClosed: ");
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        if (iceConnected) {
            Log.e(TAG, "run: " + Arrays.toString(reports));
        }
    }

    /*
     * 서버와 Peer 연결이 에러난 경우
     * 로그를 출력하여 로그 메시지를 확인한다.
     */
    @Override
    public void onPeerConnectionError(String description) {
        Log.e(TAG, "onPeerConnectionError: " + description);
    }

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