package com.testexam.charlie.tlive.main.place.write

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.yanzhenjie.album.Album
import com.yanzhenjie.album.AlbumConfig
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.api.widget.Widget
import kotlinx.android.synthetic.main.activity_write_review.*
import java.util.*


/**
 * 맛집에 대한 리뷰를 작성하는 Activity
 *
 * 맛집에 대한 평가를 선택하고 (맛있다, 괜찮다, 별로) 리뷰를 작성하는 Activity
 */

private const val GOOD = 30
private const val NORMAL = 20
private const val BAD = 10

class WriteReviewActivity : BaseActivity() , View.OnClickListener{
    private var selectPoint = 0 // 선택한 평점을 저장하는 변수. 맛있다 = 30, 괜찮다 = 20, 별로 = 10, 없음 = 0
    private var albumList : ArrayList<AlbumFile> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        selectPoint = intent.getIntExtra("selectPoint",0) // 인텐트로 전달된 평점을 가져온다.

        changeEmotion(selectPoint)  // 가져온 평점으로 이모션을 초기화한다.

        Album.initialize(AlbumConfig.newBuilder(this) // Album 설정 초기화
                .setAlbumLoader(MediaLoader())
                .setLocale(Locale.KOREA)
                .build())


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
                }else{
                    writeReviewSubmitBtn.text = "완료 ("+s.length+"/2000)"
                    writeReviewSubmitBtn.setTextColor(Color.WHITE)
                    writeReviewSubmitBtn.setBackgroundColor(ContextCompat.getColor(applicationContext,R.color.colorPrimary))
                    writeReviewSubmitBtn.isClickable = true
                }
            }
        })
    }

    // OnClickListener 를 상속받아 구현한 onClick 함수
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
                if(selectPoint==0){
                    // 이러면 실행 안되도록
                }else{

                }
            }
        }
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
                    for(i in 0 until albumList.size){
                        Log.d("album path",albumList[i].path)
                        Log.d("album thum",albumList[i].thumbPath)
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