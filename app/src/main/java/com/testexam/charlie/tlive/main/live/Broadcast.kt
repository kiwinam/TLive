package com.testexam.charlie.tlive.main.live

/**
 * Live Fragment 에 있는 현재 방송 목록의 데이터 클래스
 *
 * Created by charlie on 2018. 5. 24
 */

data class Broadcast(val hostEmail : String,        // 호스트의 이메일
                     val hostName : String,         // 호스트의 이름
                     val hostProfileUrl : String,   // 호스트의 프로필 사진 경로
                     // 방송의 정보
                     val roomNo : Int,              // 데이터 베이스 상 방의 번호
                     val roomSessionNo : Int,       // Node.js 상 방의 세션 번호
                     val roomName : String,         // 방 이름
                     val roomTag : String,          // 방 태그
                     var likeNum : Int,             // 좋아요 개수
                     var viewerNum : Int,           // 라이브 시청자 수
                     var isLive : Int,              // 라이브 방송 유무
                     val uploadTime : String,       // 방송이 시작된 시간
                     val previewUrl : String,       // 방송 미리보기 사진의 경로
                     val vodUrl : String,           // VOD 경로
                     var isLike : Boolean,          // 내가 좋아요를 눌렀는지 여부
                     var isSubscribe : Boolean      // 내가 구독하는 호스트인지 여부
                     )