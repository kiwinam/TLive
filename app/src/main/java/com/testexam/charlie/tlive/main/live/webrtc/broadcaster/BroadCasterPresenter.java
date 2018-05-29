package com.testexam.charlie.tlive.main.live.webrtc.broadcaster;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.DefaultSocketService;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionClient;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionParameters;
import com.nhancv.webrtcpeer.rtc_peer.SignalingEvents;
import com.nhancv.webrtcpeer.rtc_peer.SignalingParameters;
import com.nhancv.webrtcpeer.rtc_peer.StreamMode;
import com.nhancv.webrtcpeer.rtc_peer.config.DefaultConfig;
import com.nhancv.webrtcpeer.rtc_plugins.RTCAudioManager;
import com.testexam.charlie.tlive.common.RxScheduler;
import com.testexam.charlie.tlive.common.kurento.KurentoPresenterRTCClient;
import com.testexam.charlie.tlive.common.kurento.models.CandidateModel;
import com.testexam.charlie.tlive.common.kurento.models.response.ServerResponse;
import com.testexam.charlie.tlive.common.kurento.models.response.TypeResponse;

import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by charlie on 2018. 5. 28
 */

public class BroadCasterPresenter extends MvpBasePresenter<BroadCasterView> implements SignalingEvents, PeerConnectionClient.PeerConnectionEvents{

    private static final String TAG = BroadCasterPresenter.class.getSimpleName(); // 로그 출력을 위한 태그 설정. 현재 클래스의 이름으로 저장함.

    private static final String STREAM_HOST = "wss://13.125.64.135:8443/one2many"; // 서버에 WebRTC 스트리밍을 하기 위한 url 과 port, JS 파일 경로 설정

    private Application application;
    private SocketService socketService;
    private Gson gson;

    private PeerConnectionClient peerConnectionClient;
    private KurentoPresenterRTCClient rtcClient;
    private PeerConnectionParameters peerConnectionParameters;
    private DefaultConfig defaultConfig;
    private RTCAudioManager audioManager;
    private SignalingParameters signalingParameters;

    private boolean iceConnected;

    public BroadCasterPresenter(Application application){
        this.application = application;
        this.socketService = new DefaultSocketService(application);
        this.gson = new Gson();
    }

    public void initPeerConfig(){
        rtcClient = new KurentoPresenterRTCClient(socketService);
        defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.SEND_ONLY);
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(application.getApplicationContext(), peerConnectionParameters, this );
        //

        peerConnectionClient.setVideoEnabled(true);

    }

    /*
    방송 시작 전에 SurfaceView 설정하는 메소드
     */
    public void setVideoToSurfaceView(){
        peerConnectionClient.startVideoSource();
//        signalingParameters = null;
//        VideoCapturer videoCapturer = null;
//
//        if(peerConnectionParameters.videoCallEnabled){
//            videoCapturer = getView().createVideoCapturer();
//        }
//
//        peerConnectionClient.createPeerConnection(getView().getEglBaseContext(), getView().getLocalProxyRenderer(), new ArrayList<>(), videoCapturer, signalingParameters);
        //VideoCapturer capturer = getView().createVideoCapturer();
        //getView().getLocalProxyRenderer().renderFrame(capturer);

    }

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

        if(isViewAttached()){
            getView().disconnect();
        }
    }

    public void startCall(){
        if(rtcClient == null){
            Log.e(TAG,"AppRTC client is not allocated for a call");
            return;
        }
        rtcClient.setInfo("ownerEmail","roomName","roomTag");
        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback(){
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                super.onOpen(serverHandshake);
                RxScheduler.runOnUi(o->{
                   if(isViewAttached()){
                       getView().logAndToast("Socket connected");
                   }
                });
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
                                RxScheduler.runOnUi(o->{
                                   if(isViewAttached()){
                                       getView().logAndToast(serverResponse.getMessage());
                                   }
                                });
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
                    }
                }catch (JsonSyntaxException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                super.onClose(i, s, b);
                RxScheduler.runOnUi(o->{
                   if(isViewAttached()){
                       getView().logAndToast("Socket closed");
                   }
                   disconnect();
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                RxScheduler.runOnUi(o->{
                    if(isViewAttached()){
                        getView().logAndToast(e.getMessage());
                    }
                    disconnect();
                });
            }
        });

        // 오디오 라우팅 (오디오 모드, 오디오 장치 열거 등)을 처리할 오디오 관리자 만듬.
        audioManager = RTCAudioManager.create(application.getApplicationContext());
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
        RxScheduler.runOnUi(o -> {
           if(isViewAttached()){
               signalingParameters = params;
               VideoCapturer videoCapturer = null;

               if(peerConnectionParameters.videoCallEnabled){
                   videoCapturer = getView().createVideoCapturer();
               }

               peerConnectionClient.createPeerConnection(getView().getEglBaseContext(), getView().getLocalProxyRenderer(), new ArrayList<>(), videoCapturer, signalingParameters);

               if(signalingParameters.initiator){
                   if(isViewAttached()) getView().logAndToast("Creating OFFER");
                   // 시간 내에 클라이언트에 응답하기 위해 SDP 를 보낸다.
                   peerConnectionClient.createOffer();
               } else {
                   if(params.offerSdp != null){
                       peerConnectionClient.setRemoteDescription(params.offerSdp);
                       if(isViewAttached()) getView().logAndToast("Creating ANSWER");
                       peerConnectionClient.createAnswer();
                   }
                   if(params.iceCandidates != null){
                       // 방에서 원격 ICE 참가자를 추가한다
                       for (IceCandidate iceCandidate : params.iceCandidates){
                           peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                       }
                   }
               }
           }
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        RxScheduler.runOnUi(o -> {
            if(peerConnectionClient == null) {
                Log.e(TAG,"Received remote SDP for non-initialized peer connection");
                return;
            }

            peerConnectionClient.setRemoteDescription(sdp);

            if(!signalingParameters.initiator){
                if(isViewAttached()) getView().logAndToast("Creating ANSWER");

                peerConnectionClient.createAnswer();
                Log.e(TAG+"::onRemoteDescription","createAnswer");
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate iceCandidate) {
        RxScheduler.runOnUi(o->{
            if(peerConnectionClient == null){
                Log.e(TAG,"Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(iceCandidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        RxScheduler.runOnUi(o->{
            if(peerConnectionClient == null){
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(iceCandidates);
        });
    }

    @Override
    public void onChannelClose() {
        RxScheduler.runOnUi(o->{
            if(isViewAttached()) getView().logAndToast("Remote end hung up; dropping Peer Connection");
            disconnect();
        });
    }

    @Override
    public void onChannelError(String description) { Log.e(TAG,"onChannelError: "+description); }

    @Override
    public void onLocalDescription(SessionDescription sessionDescription) {
        RxScheduler.runOnUi(o->{
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
        RxScheduler.runOnUi(o->{
            if(rtcClient != null){
                rtcClient.sendLocalIceCandidate(iceCandidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        RxScheduler.runOnUi(o->{
            if(rtcClient != null){
                rtcClient.sendLocalIceCandidateRemovals(iceCandidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        RxScheduler.runOnUi(o->{
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        RxScheduler.runOnUi(o->{
            if (isViewAttached()) getView().logAndToast("ICE disconnected");
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
        RxScheduler.runOnUi(o->{
            if(iceConnected){
                Log.e(TAG, "run : "+statsReports);
            }
        });
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG,"onPeerConnectionError : "+s);
    }

}
