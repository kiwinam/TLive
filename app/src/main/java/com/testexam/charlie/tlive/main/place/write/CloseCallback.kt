package com.testexam.charlie.tlive.main.place.write

/**
 * 리뷰 취소 유무를 전달하는 콜백 리스너
 */
interface CloseCallback {
    fun isClose(close : Boolean)    // 리뷰 쓰기를 취소한다면 true, 취소하지 않겠다면 false 를 매개 변수로 전달한다.
}