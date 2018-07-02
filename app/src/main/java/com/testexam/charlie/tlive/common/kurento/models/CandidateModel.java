package com.testexam.charlie.tlive.common.kurento.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Google STUN 서버에서 리턴해 주는 ICE 후보지들 데이터의 형식을 정의해 놓은 클래스
 *
 * ICE Candidate 들은 spdMid, spdMLineIndex, candidate 로 이루어져 있다.
 * Created by charlie on 2018. 5. 28..
 */

public class CandidateModel implements Serializable {
    @SerializedName("sdpMid")
    private String sdpMid;      // SDP 의 ID, SDP 는 Peer 연결 간 미디어와 네트워크에 관한 정보를 전달하고 전달 받기위해 사용하는데, SDP 의 ID 값을 저장하고 있는 변수
    @SerializedName("sdpMLineIndex")
    private int sdpMLineIndex;  // SDP 에선 상세 설명을 Line 으로 구분하는데, 미디어 라인에 대한 인덱스를 저장하는 변수
    @SerializedName("candidate")
    private String sdp;

    /*
     * CandidateModel 객체를 생성할 때 변수들을 초기화한다.
     */
    public CandidateModel(String sdpMid, int sdpMLineIndex, String sdp){
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.sdp = sdp;
    }

    public String getSdpMid(){ return sdpMid; }

    public int getSdpMLineIndex() { return sdpMLineIndex; }

    public String getSdp() { return sdp; }

    // JSON 형식으로 이루어진 CandidateModel 을 String 형태로 리턴한다.
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
