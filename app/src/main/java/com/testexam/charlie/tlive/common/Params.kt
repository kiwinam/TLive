package com.testexam.charlie.tlive.common

/**
 * 파라미터 데이터 클래스
 *
 * HttpTask 클래스를 이용하여 Http 통신을 할 때 파라미터를 넣는 데이터 클래스
 * ArrayList 로 파라미터 리스트를 만들고 그 안에 Params 객체를 선언하여 사용한다.
 *
 * Created by charlie on 2018. 5. 30..
 */
data class Params(var key : String,     // 파라미터의 key 값
                  var value : String    // 파라미터의 value 값
)