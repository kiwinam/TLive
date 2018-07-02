package com.testexam.charlie.tlive.common.kurento.models.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.testexam.charlie.tlive.common.kurento.models.CandidateModel;
import com.testexam.charlie.tlive.common.kurento.models.IdModel;

import java.io.Serializable;

/**
 * Kurento-Media-Server 에서 리턴해주는 Value 에 대한 Key 값들을 정의해 놓은 데이터 클래스
 *
 * Created by charlie on 2018. 5. 28..
 */

public class ServerResponse extends IdModel implements Serializable {
    @SerializedName("response")     // 서버에서 응답의 형태를 알려줄 때 사용하는 변수, ex ) response : rejected
    private String response;
    @SerializedName("sdpAnswer")    // sdp 응답에 사용하는 변수
    private String sdpAnswer;
    @SerializedName("candidate")
    private CandidateModel candidate;
    @SerializedName("message")      // 서버에서 보내는 응답 메시지, 채팅이 아닌 경우 에러 로그가 들어온다.
    private String message;
    @SerializedName("success")      // 서버와 세션 연결 성공 여부를 저장하는 변수
    private boolean success;
    @SerializedName("from")
    private String from;
    @SerializedName("sender")       // 채팅을 보낼 때 보낸 이가 누구인지 저장하는 변수
    private String sender;

    public IdResponse getIdRes() { return IdResponse.getIdRes(getId()); }   // IdResponse 객체를 리턴한다.

    public TypeResponse getTypeRes() { return TypeResponse.getType(getResponse()); }  // TypeResponse 객체를 리턴한다.

    public String getResponse() { return response; }

    public String getSdpAnswer() { return sdpAnswer; }

    public CandidateModel getCandidate() { return candidate; }

    public String getMessage() { return message; }

    public boolean isSuccess() { return success; }

    public String getFrom() { return from; }

    public String getSender() { return sender; }

    // JSON 으로 작성된 ServerResponse 를 String 형식으로 리턴한다.
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
