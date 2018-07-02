package com.testexam.charlie.tlive.main.place.map;

/**
 * 맛집 검색 범위 콜백 클래스
 *
 * 맛집 검색 범위를 선택하는 바텀 시트에서 범위를 선택 한 경우
 * changeLimitRange 콜백 메소드를 통해 바텀 시트를 호출한 액티비티나 프레그먼트에서 변경된 범위로 맛집을 재탐색한다.
 */
public interface RangeListener {
    void changeLimitRange(String range, int rangeIndex);    // 맛집 검색 범위 변경 콜백 메소드
}
