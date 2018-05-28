package com.testexam.charlie.tlive.common.kurento;

import android.util.Log;

import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.RTCClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * Created by charlie on 2018. 5. 28
 */

public class KurentoPresenterRTCClient implements RTCClient {
    private static final String TAG = KurentoPresenterRTCClient.class.getSimpleName(); // Log 에 사용할 Tag , 클래스 명으로 설정

    private SocketService socketService; // Server 에 접근하기 위한 SocketService

    // SocketService 를 초기화한다.
    public KurentoPresenterRTCClient(SocketService socketService){
        this.socketService = socketService;
    }

    // 방에 호스트로 연결한다.
    public void connectToRoom(String host, BaseSocketCallback socketCallback){
        socketService.connect(host, socketCallback);
    }

    @Override
    public void sendOfferSdp(SessionDescription sessionDescription) {
        try{
            JSONObject object = new JSONObject();
            object.put("id","presenter"); // 방송 송출자용 ID
            object.put("sdpOffer",sessionDescription.description);

            socketService.sendMessage(object.toString()); // WebSocket 을 통해 서버에 전송한다.
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendAnswerSdp(SessionDescription sessionDescription) {
        Log.e(TAG,"sendAnswerSdp :");
    }

    @Override
    public void sendLocalIceCandidate(IceCandidate iceCandidate) {
        try{
            JSONObject object = new JSONObject();
            object.put("id","onIceCandidate");
            JSONObject candidate = new JSONObject();
            candidate.put("candidate",iceCandidate.sdp);
            candidate.put("sdpMid",iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex",iceCandidate.sdpMLineIndex);
            object.put("candidate",candidate);

            socketService.sendMessage(object.toString());
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] iceCandidates) {
        Log.e(TAG,"sendLocalIceCandidateRemovals:");
    }
}
