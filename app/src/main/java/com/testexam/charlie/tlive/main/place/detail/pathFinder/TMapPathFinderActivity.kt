package com.testexam.charlie.tlive.main.place.detail.pathFinder

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import kotlinx.android.synthetic.main.activity_tmap_path_finder.*

class TMapPathFinderActivity : BaseActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tmap_path_finder)

        val startLatLng : LatLng = intent.getParcelableExtra("startLatLng")
        val endLatLng : LatLng  = intent.getParcelableExtra("endLatLng")

        //LoadTMap(applicationContext,tMapLayout,startLatLng,endLatLng).execute()

        val tMapView = TMapView(applicationContext) // T map view 객체 초기화
        // T Map 표시
        tMapView.setSKTMapApiKey("cd9f1c1c-af0e-47fd-9a39-2cf75af026b7") // API 키 설정

        tMapLayout.addView(tMapView)    // 기존 뷰에 T map view 추가

        val startPoint = TMapPoint(startLatLng.latitude,startLatLng.longitude) // 출발지 위치 (내 현재 위치)
        val endPoint = TMapPoint(endLatLng.latitude,endLatLng.longitude)    // 도착 위치 (맛집 위치)

        // 출발지에 마커 생성
        val startMarker = TMapMarkerItem()
        startMarker.tMapPoint = startPoint
        startMarker.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
        startMarker.visible = TMapMarkerItem.VISIBLE
        startMarker.canShowCallout = true // 풍선뷰 사용
        startMarker.autoCalloutVisible = true // 풍선뷰 자동 활성화
        startMarker.calloutTitle = "출발" // 풍선뷰 타이틀
        tMapView.addMarkerItem("startMarker",startMarker)

        // 목적지에 마커 생성
        // 1. 마커 drawable 를 비트맵으로 변환한다.
        val markerBitmap = BitmapFactory.decodeResource(resources,R.drawable.ic_orange_marker)
        // 2. 마커를 생성하고 T map view 에 추가한다.
        val endMarker = TMapMarkerItem() // 마커 초기화
        endMarker.icon = markerBitmap // 마커 아이콘을 레이아웃 비트맵으로 설정한다.
        endMarker.tMapPoint = endPoint // 맛집 위치를 마커의 위치로 설정한다.
        endMarker.setPosition(0.5f, 1.0f) // 마커의 중심점을 중앙, 하단으로 설정
        endMarker.visible = TMapMarkerItem.VISIBLE
        endMarker.canShowCallout = true // 풍선뷰 사용
        endMarker.autoCalloutVisible = true // 풍선뷰 자동 활성화
        endMarker.calloutTitle = "도착" // 풍선뷰 타이틀
        tMapView.addMarkerItem("endMarker",endMarker) // T map view 에 마커를 추가한다.

        // 자동차 경로 안내
        Thread{
            try{
                val polyLine = TMapData().findPathData(startPoint,endPoint) // 경로 안내를 하는 polyLine 를 초기화한다.
                polyLine.lineColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary) // 경로 안내 선의 색상
                polyLine.outLineColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary) // 경로 안내 선의 색상
                polyLine.lineWidth = 6.0f // 경로 안내 선의 굵기
                polyLine.outLineWidth = 10.0f
                tMapView.addTMapPolyLine("Line",polyLine) // T map view 에 경로 안내 선을 그린다.

                // 지도 초기 위치 조정
                tMapView.setCenterPoint(startLatLng.longitude,startLatLng.latitude)// 현재 위치로 지도 중심점 변경
                tMapView.zoomLevel = 17 // 지도 확대 레벨 설정
            }catch (e:Exception){
                e.printStackTrace()
            }
        }.start()
    }
}