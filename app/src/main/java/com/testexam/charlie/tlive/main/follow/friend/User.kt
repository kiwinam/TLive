package com.testexam.charlie.tlive.main.follow.friend

/**
 * 친구의 정보를 담고 있는 User 데이터 클래스
 */
data class User(var email : String,         // 친구 이메일
                var name : String,          // 친구 이름
                var profileUrl : String,    // 친구 프로필 사진 경로
                val isRequest : Boolean,    // 새로운 친구 요청인지 확인하는 변수
                val friendNo : Int          // 친구의 user_no
                )

