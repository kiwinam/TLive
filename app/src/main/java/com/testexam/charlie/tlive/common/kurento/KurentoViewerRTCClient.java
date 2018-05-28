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
 * Created by charlie on 2018. 5. 28..
 */

public class KurentoViewerRTCClient implements RTCClient{

    private static final String TAG = KurentoViewerRTCClient.class.getSimpleName();

    private SocketService socketService;

    public KurentoViewerRTCClient(SocketService socketService){
        this.socketService = socketService;
    }

    public void connectToRoom(String host, BaseSocketCallback socketCallback){
        socketService.connect(host,socketCallback);
    }

    @Override
    public void sendOfferSdp(SessionDescription sessionDescription) {
        try{
            JSONObject object = new JSONObject();
            object.put("id","viewer");
            object.put("sdpOffer",sessionDescription.description);

            socketService.sendMessage(object.toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendAnswerSdp(SessionDescription sessionDescription) {
        Log.e(TAG,"sendAnswerSdp: ");
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
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] iceCandidates) {
        Log.e(TAG,"sendLocalIceCandidateRemovals");
    }
}
