package com.testexam.charlie.tlive.main.follow.channel


/**
 * 내가 팔로우 하고 있는 채널을 담고 있는 데이터 클래스
 *
 * FollowChannelFragment 에서 사용된다.
 */
data class Channel(val name : String,       // 채널 호스트의 이름
                   val email : String,      // 채널 호스트의 이메일
                   val profileSrc : String, // 채널 호스트의 프로필 사진 경로
                   val followerNum : Int,   // 채널의 팔로우 수
                   val isLive : Int)        // 현재 채널이 라이브 방송 중인지 아닌지 확인하는 변수