package com.testexam.charlie.tlive.main.place.detail.imageSlide

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.detail.photo.Photo
import com.yanzhenjie.album.mvp.BaseActivity
import kotlinx.android.synthetic.main.activity_image_slide.*

/**
 * 맛집 상세보기에서 리뷰를 남기면서 같이 업로드한 사진들을 슬라이드 형태로 보여주는 Activity
 */
class ImageSlideActivity : BaseActivity(){
    private lateinit var photoList : ArrayList<Photo>   // 사진 리스트
    private var position = -1   // 사진의 현재 위치를 가지고 있는 포지션 값
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_slide)

        initData()  // 초기 데이터를 받아온다.
        slideCloseIv.setOnClickListener({ finish() })   // 슬라이드 닫기 버튼을 누르면 액티비티를 종료한다.
    }

    /*
     * 초기 데이터 받아오는 메소드
     */
    @SuppressLint("SetTextI18n")
    private fun initData(){
        photoList = intent.getParcelableArrayListExtra("photoArray")    // 사진 리스트 데이터를 인텐트에서 가져온다.
        position = intent.getIntExtra("startPosition",0)    // 사용자가 상세보기를 위해 누른 사진의 포지션을 가져온다.

        runOnUiThread({
            slidePositionTv.text = (position+1).toString()+"/"+photoList.size   // 현재 보고 있는 이미지가 몇 번째 이미지인지 나타낸다.
        })
        setViewPager()  // 뷰페이저를 설정한다.
    }

    /*
     * ViewPager 설정하는 메소드
     */
    private fun setViewPager(){
        val slideAdapter = ImageSlideAdapter(applicationContext,layoutInflater,photoList) // ImageSlideAdapter 생성
        slideViewPager.adapter = slideAdapter // slideViewPager 에 어댑터 설정
        slideViewPager.currentItem = position // startPosition 에서 가져온 위치로 슬라이드 변경
        slideViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{ // 뷰페이저에 페이지의 변경을 감지하는 리스너를 추가한다.
            override fun onPageScrollStateChanged(state: Int) { }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                runOnUiThread({
                    slidePositionTv.text = (position+1).toString()+"/"+photoList.size   // 현재 보고 있는 이미지가 몇 번째 이미지인지 나타낸다.
                })
            }
        })
    }
}