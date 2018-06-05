package com.testexam.charlie.tlive.main.live

/**
 * Live Fragment 에 있는 현재 방송 목록의 데이터 클래스
 *
 * Created by charlie on 2018. 5. 24
 */

data class Broadcast(val hostEmail : String,
                     val hostName : String,
                     val hostProfileUrl : String,
                     // Room information
                     val roomNo : Int,
                     val roomSessionNo : Int,
                     val roomName : String,
                     val roomTag : String,
                     val likeNum : Int,
                     val viewerNum : Int,
                     val isLive : Int,
                     val uploadTime : String,
                     val previewUrl : String,
                     val vodUrl : String
                     )