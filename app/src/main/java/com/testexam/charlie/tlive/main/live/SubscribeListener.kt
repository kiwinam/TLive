package com.testexam.charlie.tlive.main.live

/**
 * 구독하기 버튼의 리스너
 *
 * 구독하기 혹은 구독 취소하기 버튼을 누르면 subscribe 의 매개 변수로 구독 상태를 프레그먼트로 전달해준다.
 */
interface SubscribeListener {
    fun subscribe(isSubscribe : Boolean)
}