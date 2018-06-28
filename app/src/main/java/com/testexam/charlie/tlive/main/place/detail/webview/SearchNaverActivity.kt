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

        val placeName = intent.getStringExtra("placeName")
        val query = URLEncoder.encode(placeName,"UTF-8")
        naverUrl = "https://search.naver.com/search.naver?where=post&sm=tab_jum&query=$query"

        runOnUiThread({
            naverTitleTv.text = placeName
            naverPb.visibility = View.VISIBLE
        })

        naverWebView.webViewClient = WebViewClient()
        naverWebView.loadUrl(naverUrl)
        naverWebView.settings.javaScriptEnabled = true
        runOnUiThread({
            naverPb.visibility = View.GONE
        })


        naverCloseIv.setOnClickListener({
            finish()
        })
    }

}