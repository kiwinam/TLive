package com.testexam.charlie.tlive.main.place.detail.review

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.yanzhenjie.album.mvp.BaseActivity
import kotlinx.android.synthetic.main.activity_review.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 선택한 맛집의 전체 리뷰를 보는 Activity
 */
class ReviewActivity : BaseActivity() {
    private lateinit var reviewList : ArrayList<Review>
    private lateinit var reviewAdapter: ReviewAdapter

    private var placeNo = -1

    private var reviewCount = 0
    private var goodReviewCount = 0
    private var normalReviewCount = 0
    private var badReviewCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        placeNo = intent.getIntExtra("placeNo",-1)

        setRecyclerView()
    }

    /*
     *
     */
    private fun setRecyclerView(){
        reviewList = ArrayList() // 리뷰 리스트 초기화
        reviewAdapter = ReviewAdapter(reviewList,applicationContext,placeNo)

        reviewRv.adapter = reviewAdapter
        reviewRv.layoutManager = LinearLayoutManager(applicationContext)
        reviewRv.isNestedScrollingEnabled = false
        getReview()
    }

    /*
     *
     */
    private fun getReview(){
        val paramArray = ArrayList<Params>()
        paramArray.add(Params("placeNo",placeNo.toString()))

        try{
            val result = HttpTask("getReview.php",paramArray).execute().get()
            if(result != "null"){
                val jsonObject = JSONObject(result)

                reviewCount = jsonObject.getInt("reviewCount")
                goodReviewCount = jsonObject.getInt("goodCount")
                normalReviewCount = jsonObject.getInt("normalCount")
                badReviewCount = jsonObject.getInt("badCount")

                reviewList.clear()
                val reviews = jsonObject.getString("reviews")
                val reviewJSONArray = JSONArray(reviews)
                for(i in 0 until reviewJSONArray.length()){
                    val reviewObject = reviewJSONArray.getJSONObject(i)
                    reviewList.add(Review(
                            reviewObject.getString("userEmail"),        // 리뷰 작성자 이메일
                            reviewObject.getString("userName"),         // 리뷰 작성자 이름
                            reviewObject.getString("userProfile"),      // 리뷰 작성자 프로필 사진 경로
                            reviewObject.getInt("userReviewCount"),     // 리뷰 작성자가 작성한 리뷰의 개수
                            reviewObject.getString("reviewText"),       // 리뷰 텍스트 본문
                            reviewObject.getInt("reviewPoint"),         // 리뷰 점수
                            reviewObject.getString("photoArray"),       // 리뷰할 때 같이 보낸 사진의 jsonArray (String 으로 저장된다.)
                            reviewObject.getString("uploadTime")        // 리뷰 작성한 날짜와 시간
                    ))
                }

                setInformation()
            }

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    /*
     *
     */
    @SuppressLint("SetTextI18n")
    private fun setInformation(){
        runOnUiThread({
            reviewCountTv.text = reviewCount.toString()
            reviewGoodCountTv.text = "맛있다! ("+goodReviewCount.toString()+")"       // 맛있다 리뷰의 총 개수를 reviewGoodCountTv View 에 표시한다.
            reviewNormalCountTv.text = "괜찮다 ("+normalReviewCount.toString()+")"   // 괜찮다 리뷰의 총 개수를 reviewNormalCountTv View 에 표시한다.
            reviewBadCountTv.text = "별로 ("+badReviewCount.toString()+")"         // 별로 리뷰의 총 개수를 reviewBadCountTv View 에 표시한다.

            reviewAdapter.setData(reviewList)
        })
    }
}