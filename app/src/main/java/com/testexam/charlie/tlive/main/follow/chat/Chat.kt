package com.testexam.charlie.tlive.main.follow.chat

/**
 * 1:1 채팅 데이터를 담고 있는 데이터 클래스
 *
 * ChatActivity, ChatAdapter 에서 사용된다.
 */
data class Chat(val senderName : String,    // 채팅 보낸 사람의 이름
                val senderEmail : String,   // 채팅 보낸 사람의 이메일
                val msg : String            // 채팅 메시지
                )