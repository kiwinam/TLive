package com.testexam.charlie.tlive.main.place.write

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.retrofit.ConnectionList
import com.testexam.charlie.tlive.retrofit.RetrofitConn
import com.testexam.charlie.tlive.retrofit.SimpleResponse
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.api.widget.Widget
import kotlinx.android.synthetic.main.activity_write_review.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * 맛집에 대한 리뷰를 작성하는 Activity
 *
 * 맛집에 대한 평가를 선택하고 (맛있다, 괜찮다, 별로) 리뷰를 작성하는 Activity
 */

private const val GOOD = 5
private const val NORMAL = 3
private const val BAD = 1
private const val WRITE_CODE = 1000

class WriteReviewActivity : BaseActivity() , View.OnClickListener{
    private var selectPoint = 0 // 선택한 평점을 저장하는 변수. 맛있다 = 30, 괜찮다 = 20, 별로 = 10, 없음 = 0
    private var albumList : ArrayList<AlbumFile> = ArrayList()
    private var placeNo = -1
    private var isTextOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        selectPoint = intent.getIntExtra("selectPoint",0) // 인텐트로 전달된 평점을 가져온다.
        placeNo = intent.getIntExtra("placeNo",-1)

        changeEmotion(selectPoint)  // 가져온 평점으로 이모션을 초기화한다.

        try{
            Album.initialize(AlbumConfig.newBuilder(this) // Album 설정 초기화
                    .setAlbumLoader(MediaLoader())
                    .setLocale(Locale.KOREA)
                    .build())
        }catch (e : Exception){
            e.printStackTrace()
        }


        setClickListeners() // 클릭 리스너 설정

        // 글자 길이가 0 이상일 때 완료 버튼을 활성화한다.
        // 글자 길이가 0 이라면 완료 버튼을 활성화 하지 않고 색상을 회색으로 변경한다.
        // 글자 길이가 변경되면 완려 버튼에 변경된 길이를 표시한다.
        writeReviewEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isEmpty()){
                    writeReviewSubmitBtn.text = "완료 (0/2000)"
                    writeReviewSubmitBtn.setTextColor(Color.WHITE)
                    writeReviewSubmitBtn.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.colorGray))
                    writeReviewSubmitBtn.isClickable = false
                    isTextOn = false
                }else{
                    writeReviewSubmitBtn.text = "완료 ("+s.length+"/2000)"
                    writeReviewSubmitBtn.setTextColor(Color.WHITE)
                    writeReviewSubmitBtn.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.colorPrimary))
                    writeReviewSubmitBtn.isClickable = true
                    isTextOn = true
                }
            }
        })
    }

    // OnClickListener 를 상속받아 재구현한 onClick 함수
    // 클릭 리스너를 등록한 View 가 클릭 되면 호출된다.
    // 메소드를 호출한 View 의 Id 값으로 when 문을 통해 구별하여 각 View 에 맞는 처리를 한다.
    override fun onClick(v: View?) {
        when(v){
            writeReviewBackIv->{    // 닫기 버튼
                val closeBottomSheet = CloseBottomSheet.newInstance()
                closeBottomSheet.setCallback(object : CloseCallback {
                    override fun isClose(close: Boolean) {
                        if(close){
                            finish()
                        }
                    }
                })
                closeBottomSheet.show(supportFragmentManager,"closeBS")
            }

            writeReviewAlbumIv->{   // 앨범 버튼
                selectAlbum() // 앨범에서 사진을 가져오는 메소드 호출
            }

            writeReviewGoodLo->{    // 맛있다 레이아웃
                changeEmotion(GOOD)
                selectPoint = GOOD
            }

            writeReviewNormalLo->{  // 괜찮다 레이아웃
                changeEmotion(NORMAL)
                selectPoint = NORMAL
            }

            writeReviewBadLo->{     // 별로 레이아웃
                changeEmotion(BAD)
                selectPoint = BAD
            }

            writeReviewSubmitBtn->{ // 완료 버튼, 리뷰 작성을 완료하고 서버로 데이터를 전송한다.
                if(selectPoint != 0){   // 평가가 선택되지 않았거나
                    if(isTextOn){       // 리뷰가 작성되지 않으면 리뷰를 보내지 않는다.
                        submitReview()  // 리뷰를 서버로 전송한다.
                    }
                }
            }
        }
    }

    /*
     * 작성된 리뷰를 서버로 제출하는 메소드
     *
     * 리뷰 작성 완료 버튼 ( writeReviewSubmitBtn ) 의 onClick 메서드로 인해 호출된다.
     * 리뷰 작성 완료 버튼은 리뷰 점수(GOOD,NORMAL,BAD) 를 선택하지 않거나 작성한 리뷰의 글자의 길이가 0일 경우 활성화 되지 않는다.
     *
     * 1. 작성된 리뷰 데이터를 파라미터로 설정한다.
     * 2. 사진을 추가한 만큼 파라미터로 설정한다.
     * 3. 서버로 전송한다.
     * 4. 결과에 따른 메시지를 출력한다.
     */
    @SuppressLint("SimpleDateFormat")
    private fun submitReview(){
        runOnUiThread({
            writeReviewPb.visibility = View.VISIBLE // Progress Bar 보이기.
        })
        writeReviewSubmitBtn.isClickable = false

        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val now = sdf.format(Date(System.currentTimeMillis())) // 현재 시간 저장

        // 1. 작성된 데이터 파라미터로 설정
        val paramMap = HashMap<String, RequestBody>() // 파라미터를 담을 해쉬맵 초기화
        val userEmail = getSharedPreferences("login", Context.MODE_PRIVATE).getString("email",null) // SharedPreference 에서 유저 이메일을 가져온다.

        paramMap["userEmail"] = RequestBody.create(MediaType.parse("multipart/form-data"), userEmail) // 파라미터에 유저 이메일 추가
        paramMap["placeNo"] = RequestBody.create(MediaType.parse("multipart/form-data"), placeNo.toString()) // 맛집 번호 추가
        paramMap["review"] = RequestBody.create(MediaType.parse("multipart/form-data"),writeReviewEt.text.toString()) // 리뷰 추가
        paramMap["reviewPoint"] = RequestBody.create(MediaType.parse("multipart/form-data"), selectPoint.toString()) // 사진 개수 추가
        paramMap["photoCount"] = RequestBody.create(MediaType.parse("multipart/form-data"), albumList.size.toString()) // 사진 개수 추가
        paramMap["now"] = RequestBody.create(MediaType.parse("multipart/form-data"), now) // 현재 시간 추가

        // 2. 사진 추가
        for(i in 0 until albumList.size){ // albumList 의 크기 만큼 파라미터에 사진 추가 작업을 한다.
            try{
                val file = File(albumList[i].path) // albumList 에서 이미지 파일 경로를 가져온다.
                val img = RequestBody.create(MediaType.parse("image/*"),file) // 만들어진 파일을 RequestBody 형식에 담는다.
                paramMap["img$i\"; filename=\"review_$userEmail$i$placeNo.png\""] = img // 파라미터 추가
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        // 3. 서버로 전송
        val conn = RetrofitConn.getRetrofit().create(ConnectionList::class.java) // 레트로핏 커넥션 초기화
        val call = conn.insertReview(paramMap)
        call.enqueue( object : Callback<SimpleResponse>{
            @SuppressLint("LogNotTimber")
            override fun onResponse(call: Call<SimpleResponse>?, response: Response<SimpleResponse>?) {
                if(response!!.isSuccessful){ // 리뷰 작성이 성공했다면
                    runOnUiThread({
                        writeReviewPb.visibility = View.GONE // Progress Bar 를 없앤다.
                        Toast.makeText(applicationContext,"리뷰가 작성되었습니다.",Toast.LENGTH_SHORT).show() // 토스트 메시지를 띄워 리뷰 작성 성공을 사용자에게 알린다.

                        val resultIntent = Intent() // 이 Activity 를 호출한 PlaceDetail 에게 결과 값을 반환하기 위한 Intent
                        resultIntent.putExtra("isRefresh",true) // resultIntent 에 isRefresh 값을 true 로 넣는다.
                        setResult(WRITE_CODE,resultIntent) // WRITE_CODE (1000) 로 결과를 반환한다.
                        finish() // 현재 Activity 를 종료한다.
                    })

                }else{
                    runOnUiThread({
                        writeReviewPb.visibility = View.GONE // Progress Bar 를 없앤다.
                        Toast.makeText(applicationContext,"리뷰 작성 중 에러가 발생하였습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                    })
                    Log.d("retrofit response",response.message())
                    writeReviewSubmitBtn.isClickable = true
                }
            }
            override fun onFailure(call: Call<SimpleResponse>?, t: Throwable?) {
                t!!.printStackTrace()
                writeReviewSubmitBtn.isClickable = true
            }
        })
    }

    /*
     * 앨범에서 사진을 가져오는 메소드
     *
     * 맛집 리뷰를 남길 때 같이 올릴 사진을 앨범에서 가져온다.
     * 가져온 사진들은 albumList 에 담는다.
     */
    @SuppressLint("LogNotTimber")
    private fun selectAlbum(){

        Album.album(this)
                .multipleChoice()
                .columnCount(3)
                .selectCount(10)
                .camera(true)
                .cameraVideoQuality(1)
                .checkedList(albumList)
                .widget(
                        Widget.newDarkBuilder(this)
                                .statusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))
                                .toolBarColor(ContextCompat.getColor(this,R.color.colorPrimary))
                                .title("리뷰할 사진 선택")
                                .build()
                )
                .onResult({
                    albumList = it
                    if(albumList.size > 0){
                        writeReviewAlbumCountTv.text = albumList.size.toString()
                        writeReviewAlbumCountTv.visibility = View.VISIBLE
                    }else{
                        writeReviewAlbumCountTv.visibility = View.GONE
                    }
                }).start()
    }

    /*
     * 맛집에 대한 평가를 선택할 때, 선택한 평점은 오렌지 색으로 변경하고 선택되지 않은 평점은 회색으로 변경한다.
     */
    private fun changeEmotion(selectPoint : Int){
        when(selectPoint){
            GOOD->{ // 맛있다 버튼을 클릭하고 들어온 경우, 맛있다 버튼을 활성화한다.
                // 선택된 맛있다 레이아웃을 오렌지 색으로 변경한다.
                writeReviewGoodIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_good_orange))
                writeReviewGoodTv.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))

                // 선택되지 않은 레이아웃들을 회색으로 변경한다.
                writeReviewNormalIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_normal))
                writeReviewNormalTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
                writeReviewBadIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_bad))
                writeReviewBadTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
            }
            NORMAL->{
                // 선택된 괜찮다 레이아웃을 오렌지 색으로 변경한다.
                writeReviewNormalIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_normal_orange))
                writeReviewNormalTv.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))

                // 선택되지 않은 레이아웃들을 회색으로 변경한다.
                writeReviewGoodIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_good))
                writeReviewGoodTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
                writeReviewBadIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_bad))
                writeReviewBadTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
            }
            BAD->{
                // 선택된 별로 레이아웃을 오렌지 색으로 변경한다.
                writeReviewBadIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_bad_orange))
                writeReviewBadTv.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary))

                // 선택되지 않은 레이아웃들을 회색으로 변경한다.
                writeReviewGoodIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_good))
                writeReviewGoodTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
                writeReviewNormalIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_normal))
                writeReviewNormalTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
            }
            else->{ // 아무것도 선택되지 않은 상태라면 모든 레이아웃을 회색으로 변경한다.
                writeReviewGoodIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_good))
                writeReviewGoodTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
                writeReviewNormalIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_normal))
                writeReviewNormalTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
                writeReviewBadIv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_emotion_bad))
                writeReviewBadTv.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
            }
        }
    }

    // 클릭 리스너를 설정한다.
    private fun setClickListeners(){
        writeReviewBackIv.setOnClickListener(this)
        writeReviewAlbumIv.setOnClickListener(this)
        writeReviewGoodLo.setOnClickListener(this)
        writeReviewNormalLo.setOnClickListener(this)
        writeReviewBadLo.setOnClickListener(this)
        writeReviewSubmitBtn.setOnClickListener(this)
    }
}