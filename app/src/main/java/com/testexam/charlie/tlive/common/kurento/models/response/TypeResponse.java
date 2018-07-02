package com.testexam.charlie.tlive.common.kurento.models.response;

/**
 * WebRTC SFT BroadCast 중, 실시간 방송 요청이 수락 되었는지 거절 되었는지 응답해주는 데이터 클래스
 *
 * 방송 요청은 시청자와 송출자 모두 TypeResponse 에 정의된 ACCEPTED 와 REJECTED 형식으로 리턴된다.
 * Created by charlie on 2018. 5. 28..
 */

public enum TypeResponse {
    ACCEPTED("accepted"),   // 요청 수락에 대한 응답
    REJECTED("rejected");   // 요청 거절에 대한 응답

    private String id;

    TypeResponse(String id){ this.id = id; }    // TypeResponse 객체를 생성할 때 id 변수를 초기화한다.

    /*
     * accepted 와 rejected 중에 어떤 타입이랑 일치하는지 검사하고, 일치하는 타입을 리턴한다.
     */
    public static TypeResponse getType(String type){
        for(TypeResponse typeResponse : TypeResponse.values()){
            if(type.equals(typeResponse.getId())){  // 매개변수로 전달받은 type 과 일치하는 타입이라면
                return typeResponse;        // TypeResponse 객체를 전달한다.
            }
        }
        return REJECTED;    // 일치하는 타입이 없다면 요청 거절 타입을 전달한다.
    }

    public String getId(){ return id; }
}
