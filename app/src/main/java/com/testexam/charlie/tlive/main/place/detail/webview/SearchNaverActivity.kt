package com.testexam.charlie.tlive.main.place.detail.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import com.testexam.charlie.tlive.R
import com.yanzhenjie.album.mvp.BaseActivity
import kotlinx.android.synthetic.main.activity_search_naver.*
import java.net.URLEncoder

/**
 * 네이버 웹뷰를 띄워 맛집 블로그 검색 결과를 보여주는 Activity
 */
class SearchNaverActivity : BaseActivity() {
    private var naverUrl = ""
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_naver)

        val placeName = intent.getStringExtra("placeName")  // 맛집의 이름을 인텐트로 전달받는다.
        val query = URLEncoder.encode(placeName,"UTF-8")    // 맛집의 이름을 UTF-8 형식으로 인코딩한다.
        naverUrl = "https://search.naver.com/search.naver?where=post&sm=tab_jum&query=$query"   // 네이버에 검색할 url 를 만든다.

        runOnUiThread({
            naverTitleTv.text = placeName   // 타이틀을 맛집 이름으로 설정한다.
            naverPb.visibility = View.VISIBLE   // 프로그레스 바를 표시한다.
        })

        naverWebView.webViewClient = WebViewClient()    // 웹뷰 클라이언트를 초기화한다.
        naverWebView.loadUrl(naverUrl)  // 검색 url 를 로드한다.
        naverWebView.settings.javaScriptEnabled = true  // 웹뷰에 자바스크립트를 허용한다.
        runOnUiThread({
            naverPb.visibility = View.GONE  // 프로그레스 바를 보이지 않게 한다.
        })
        naverCloseIv.setOnClickListener({ finish() })   // 닫기 버튼을 누르면 액티비티를 종료한다.
    }

}