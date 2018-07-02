package com.testexam.charlie.tlive.common.kurento.models.response;

/**
 * WebRTC SFU Broadcast 방송 중, Kurento-Media-Server ID 에 관련된 응답 데이터 클래스
 *
 * server.js (node.js) 에서 리턴해주는 응답 결과를 정의해 놓는다.
 * 리턴해주는 응답과 파라미터로 들어온 응답이 같을 경우 IdResponse 객체에서 정의된 Id 를 리턴해준다.
 * Created by charlie on 2018. 5. 28
 */

public enum IdResponse {

    REGISTER_RESPONSE("registerResponse"),      // 세션 등록에 대한 응답
    PRESENTER_RESPONSE("presenterResponse"),    // 방송 송출자에 등록에 대한 응답 , 방송 송출자가 KMS 에 세션이 연결 되었는지 rejected 와 accepted 로 구분하는 응답
    ICE_CANDIDATE("iceCandidate"),              // ICE 서버 후보 응답, Google STUN 서버에서 KMS 와 세션 연결할 수 있는 ICE 후보지를 리턴해주는 응답
    VIEWER_RESPONSE("viewerResponse"),          // 방송 시청자 응답, 방송 시청자가 KMS 에 세션이 연결 되었는지 rejected 와 accepted 로 구분하는 응답
    STOP_COMMUNICATION("stopCommunication"),    // 방송 송출자가 방송을 종료할 때 시청자 모두에게 방송이 종료되었음을 알려주는 응답
    //CLOSE_ROOM_RESPONSE("closeRoomResponse"),   // 방송 종료 응답, 사용하지 않음.
    //INCOMING_CALL("incomingCall"),
    //START_COMMUNICATION("startCommunication"),
    //CALL_RESPONSE("callResponse"),
    VIEWER_CHANGE("viewerNumChange"),           // 방송 시청자 수가 변경되었음을 알려주는 응답
    CHAT("chat"),                               // 채팅을 전달받았음을 알려주는 응답

    UN_KNOWN("unknown");                        // 알려지지 않은 응답 형태에 대응하는 응답

    private String id;

    // IdResponse 를 생성할 때 id 를 초기하화한다.
    IdResponse(String id) {
        this.id = id;
    }

    // IdResponse 객체에 있는 Response 응답 형식들 중에 일치하는 응답을 리턴해준다.
    public static IdResponse getIdRes(String idRes) {
        // 전체 Response 응답을 검사한다.
        for (IdResponse idResponse : IdResponse.values()) {
            if (idRes.equals(idResponse.getId())) { // 전달된 idRes 와 일치하는 응답을 리턴해준다.
                return idResponse;
            }
        }
        return UN_KNOWN; // 일치하는 응답 형태가 없다면 UN_KNOWN 응답을 리턴한다.
    }

    // idResponse 객체에 있는 id 를 가져온다.
    public String getId() {
        return id;
    }
}