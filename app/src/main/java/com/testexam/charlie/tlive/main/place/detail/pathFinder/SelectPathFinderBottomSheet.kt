package com.testexam.charlie.tlive.main.place.detail.pathFinder

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.maps.model.LatLng
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.detail.ar.ARNavigationActivity

/**
 * 맛집 상세보기에서 길찾기를 할 때 어떤 길찾기를 사용할지 선택하는 바텀 시트
 */
class SelectPathFinderBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var selectPathMap : LinearLayout   // T map 길찾기 버튼 레이아웃
    private lateinit var selectPathAr : LinearLayout    // AR 길찾기 버튼 레이아웃

    private lateinit var startLatLng: LatLng    // 출발 위치
    private lateinit var endLatLng: LatLng      // 도착 위치
    lateinit var intent : Intent    // 인텐트 객체

    companion object {
        fun newInstance(): SelectPathFinderBottomSheet = SelectPathFinderBottomSheet()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.bs_select_path_finder, container, false)
        selectPathMap = v.findViewById(R.id.selectPathMap)  // T map 길찾기 버튼 레이아웃
        selectPathAr = v.findViewById(R.id.selectPathAr)    // AR 길찾기 버튼 레이아웃
        selectPathMap.setOnClickListener(this)  // selectPathMap 클릭 리스너 설정
        selectPathAr.setOnClickListener(this)   // selectPathAr 클릭 리스너 설정
        return v
    }

    override fun onClick(v: View?) {
        when(v){
            selectPathMap->{    // 일반 길찾기 버튼을 누르면
                val tMapIntent = Intent(context, TMapPathFinderActivity::class.java)    // 티맵 길찾기 액티비티로 이동한다.
                tMapIntent.putExtra("startLatLng",startLatLng)  // 인텐트에 시작 위경도를 넣는다.
                tMapIntent.putExtra("endLatLng",endLatLng)      // 인텐트에 도착 위경도를 넣는다.
                startActivity(tMapIntent)   // 티맵 액티비티 시작
            }
            selectPathAr-> {    // AR 길찾기 버튼을 누르면
                val arIntent = Intent(context, ARNavigationActivity::class.java)    // AR 길찾기 인텐트 초기화
                arIntent.putExtra("startLat", startLatLng.latitude)     // 시작 위도,
                arIntent.putExtra("startLng", startLatLng.longitude)    // 시작 경도,
                arIntent.putExtra("endLat", endLatLng.latitude)         // 도착 위도,
                arIntent.putExtra("endLng", endLatLng.longitude)        // 도착 경도를 인텐트에 넣는다.
                startActivity(arIntent) // AR 길찾기 액티비티 시작
            }
        }
        dismiss()   // 바텀 시트를 dismiss 한다.
    }

    /*
     * 바텀시트를 호출한 Activity 에서 현재 상세보기하고 있는 맛집의 경로를 전달 받는다.
     */
    fun setLatLng(myLatLng: LatLng, placeLatLng: LatLng){
        startLatLng = myLatLng
        endLatLng = placeLatLng
    }
}