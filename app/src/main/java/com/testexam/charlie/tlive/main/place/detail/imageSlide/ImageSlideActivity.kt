package com.testexam.charlie.tlive.main.place.detail.imageSlide

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.detail.photo.Photo
import com.yanzhenjie.album.mvp.BaseActivity
import kotlinx.android.synthetic.main.activity_image_slide.*

class ImageSlideActivity : BaseActivity(){
    private lateinit var photoList : ArrayList<Photo>
    private var position = -1
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_slide)

        initData()

        slideCloseIv.setOnClickListener({
            finish()
        })
    }

    /*
     * 초기 데이터 받아오는 메소드
     */
    @SuppressLint("SetTextI18n")
    private fun initData(){
        photoList = intent.getParcelableArrayListExtra("photoArray")
        position = intent.getIntExtra("startPosition",0)

        runOnUiThread({
            slidePositionTv.text = (position+1).toString()+"/"+photoList.size
        })
        setViewPager()
    }

    /*
     * ViewPager 설정하는 메소드
     */
    private fun setViewPager(){
        val slideAdapter = ImageSlideAdapter(applicationContext,layoutInflater,photoList) // ImageSlideAdapter 생성
        slideViewPager.adapter = slideAdapter // slideViewPager 에 어댑터 설정
        slideViewPager.currentItem = position // startPosition 에서 가져온 위치로 슬라이드 변경
        slideViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) { }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                runOnUiThread({
                    slidePositionTv.text = (position+1).toString()+"/"+photoList.size
                })
            }
        })
    }
}