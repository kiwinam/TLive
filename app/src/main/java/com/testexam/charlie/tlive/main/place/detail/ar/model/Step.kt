package com.testexam.charlie.tlive.main.place.detail.ar.model

/**
 * AR 길찾기에서 하나의 경로를 가지고 있는 데이터 클래스
 * Map box api 에 경로를 요청하고 리턴된 상세 경로 (Step) 를 가지고 있다.
 */
data class Step(val startLocation : StartLocation?, // 출발위치
                val endLocation: EndLocation?,  // 도착 위치
                val geometry : String?,     // geometry 코드 , map box 유틸로 디코딩 해야한다.
                val type : String?,     // 출발, 도착
                val modifier : String?  // 구분자, 좌회전 , 우회전
                )