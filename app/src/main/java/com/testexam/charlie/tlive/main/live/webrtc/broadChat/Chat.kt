package com.testexam.charlie.tlive.main.live.webrtc.broadChat

/**
 * 라이브 방송에서 사용하는 채팅 데이터 클래스
 */
data class Chat(var sender : String,    // 보낸 사람 이름
                var message : String,   // 메시지
                var time : Int          // 보낸 시간
                )