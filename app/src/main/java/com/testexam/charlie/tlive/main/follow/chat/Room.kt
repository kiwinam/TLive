package com.testexam.charlie.tlive.main.follow.chat

/**
 * 채팅 방의 정보를 가지고 있는 데이터 클래스
 *
 * FollowChatFragment 에서 채팅 방 목록을 표시할 때 사용된다.
 */
data class Room(val roomNo : Int,               // 채팅방 번호
                val targetEmail : String,       // 상대방의 이메일
                val targetName : String,        // 상대방의 이름
                val currentChat : String,       // 가장 최근 나눈 채팅
                val badgeCount : Int            // 읽지 않은 채팅의 개수
                )

