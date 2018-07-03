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
    private lateinit var reviewList : ArrayList<Review>     // 해당 맛집에 대한 리뷰 리스트
    private lateinit var reviewAdapter: ReviewAdapter       // 리뷰 어댑터

    private var placeNo = -1        // 맛집 번호

    private var reviewCount = 0         // 해당 맛집에 달린 총 리뷰의 개수
    private var goodReviewCount = 0     // 해당 맛집에 달린 좋아요 리뷰의 개수
    private var normalReviewCount = 0   // 해당 맛집에 달린 괜찮다 리뷰의 개수
    private var badReviewCount = 0      // 해당 맛집에 달린 별로 리뷰의 개수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        placeNo = intent.getIntExtra("placeNo",-1)  // 인텐트에서 맛집의 번호를 가져온다.
        setRecyclerView()   // 리뷰 RecyclerView 를 설정한다.
    }

    /*
     *  리뷰 RecyclerView 를 설정한다.
     */
    private fun setRecyclerView(){
        reviewList = ArrayList() // 리뷰 리스트 초기화
        reviewAdapter = ReviewAdapter(reviewList,applicationContext,placeNo)    // 리뷰 어댑터 초기화

        reviewRv.adapter = reviewAdapter    // reviewRv 어댑터 설정
        reviewRv.layoutManager = LinearLayoutManager(applicationContext)    // reviewRv 레이아웃 매니저 설정
        reviewRv.isNestedScrollingEnabled = false   // reviewRv 를 내부 레이아웃에서도 부드럽게 스크롤 되도록 설정
        getReview() // 서버에 해당 맛집의 번호로 모든 리뷰를 요청한다.
    }

    /*
     *  서버에 해당 맛집의 번호로 모든 리뷰를 요청한다.
     */
    private fun getReview(){
        val paramArray = ArrayList<Params>()        // 파라미터를 담고 있는 ArrayList
        paramArray.add(Params("placeNo",placeNo.toString()))    // placeNo 를 파라미터에 담는다.

        try{
            val result = HttpTask("getReview.php",paramArray).execute().get()   // 서버에 해당 맛집에 작성된 모든 리뷰를 요청한다. 결과는 result 에 저장한다.
            if(result != "null"){   // 리뷰 결과가 있는 경우
                val jsonObject = JSONObject(result) // JSONObject 로 result 를 변환한다.

                reviewCount = jsonObject.getInt("reviewCount")          // 리뷰의 총 개수
                goodReviewCount = jsonObject.getInt("goodCount")        // 좋아요 리뷰 개수
                normalReviewCount = jsonObject.getInt("normalCount")    // 괜찮다 리뷰 개수
                badReviewCount = jsonObject.getInt("badCount")          // 별로 리뷰 개수

                reviewList.clear()  // 리뷰 리스트를 초기화한다.
                val reviews = jsonObject.getString("reviews")   // 리뷰들의 JSONArray 를 String 으로 가져온다.
                val reviewJSONArray = JSONArray(reviews)        // reviews 를 JSONArray 형식으로 변환한다.
                for(i in 0 until reviewJSONArray.length()){
                    val reviewObject = reviewJSONArray.getJSONObject(i)
                    reviewList.add(Review(  // 리뷰 리스트에 Review 객체를 추가한다.
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
                setInformation()    // 받아온 정보를 View 에 설정한다.
            }

        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    /*
     * 받아온 정보를 View 에 설정한다.
     */
    @SuppressLint("SetTextI18n")
    private fun setInformation(){
        runOnUiThread({
            reviewCountTv.text = reviewCount.toString()
            reviewGoodCountTv.text = "맛있다! ("+goodReviewCount.toString()+")"       // 맛있다 리뷰의 총 개수를 reviewGoodCountTv View 에 표시한다.
            reviewNormalCountTv.text = "괜찮다 ("+normalReviewCount.toString()+")"   // 괜찮다 리뷰의 총 개수를 reviewNormalCountTv View 에 표시한다.
            reviewBadCountTv.text = "별로 ("+badReviewCount.toString()+")"         // 별로 리뷰의 총 개수를 reviewBadCountTv View 에 표시한다.

            reviewAdapter.setData(reviewList)   // 리뷰 어댑터에 데이터 세트가 변경되었음을 알려준다.
        })
    }
}