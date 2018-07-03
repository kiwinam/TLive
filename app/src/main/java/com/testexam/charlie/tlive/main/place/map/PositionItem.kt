package com.testexam.charlie.tlive.main.place.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * 맵에 마커를 표시할 때 사용하는 데이터 클래스
 */
class PositionItem(no : Int,name : String,lat : Double, lon : Double) : ClusterItem{

    private var position : LatLng = LatLng(lat,lon) // 마커의 위경도 값

    private var number : Int = 0        // 마커의 평점 순서
    private var placeName : String = "" // 맛집 이름
    init {
        number = no
        placeName = name
    }

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return position
    }

    fun getNo() : Int{
        return number
    }
}