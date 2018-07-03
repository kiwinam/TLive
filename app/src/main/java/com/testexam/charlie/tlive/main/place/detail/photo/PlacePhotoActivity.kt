package com.testexam.charlie.tlive.main.place.detail.photo

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import com.testexam.charlie.tlive.main.place.detail.imageSlide.ImageSlideActivity
import com.yanzhenjie.album.mvp.BaseActivity
import kotlinx.android.synthetic.main.activity_place_photo.*
import org.json.JSONArray

/**
 * 맛집 리뷰에 올라온 모든 사진을 모든 Activity
 */
class PlacePhotoActivity : BaseActivity() {
    private lateinit var photoList : ArrayList<Photo>   // 사진 리스트
    private lateinit var photoAdapter: PhotoAdapter     // 사진 어댑터
    private var placeNo = -1    // 맛집의 번호

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_photo)

        placeNo = intent.getIntExtra("placeNo",-1) // intent 로 전달받은 placeNo 값을 가져온다.

        setRecyclerView()   // photoRv 설정을 한다.
        getAllPhotos()      // 서버에 현재 선택한 맛집의 전체 사진을 가져온다.

        placePhotoCloseIv.setOnClickListener({ finish() })  // 모든 사진 닫기 버튼을 누르면 액티비티를 종료한다.
    }

    /*
     * 현재 선택한 맛집에 대한 모든 사진을 서버에서 불러오는 메소드
     */
    private fun getAllPhotos(){
        Thread{
            runOnUiThread({
                photoPb.visibility = View.VISIBLE   // 프로그레스 바를 보이게한다.
            })

            val paramsArray = ArrayList<Params>() // 파라미터 배열 생성
            paramsArray.add(Params("placeNo",placeNo.toString())) // placeNo 파라미터에 추가

            val result = HttpTask("getAllPhotos.php",paramsArray).execute().get() // Http 통신으로 getAllPhotos.php 에 전체 사진 목록을 요청한다.
            if(result != "null"){   // 요청 결과가 null 이 아니라면
                photoList.clear()   // 사진 리스트를 초기화한다.
                try{
                    val photoJSONArray = JSONArray(result)  // result 를 JSONArray 로 변환한다.
                    for(i in 0 until photoJSONArray.length()){
                        val photoObject = photoJSONArray.getJSONObject(i)
                        val src = photoObject.getString("src")
                        photoList.add(Photo(src))       // photoList 에 사진의 경로를 가지고 있는 Photo 객체를 추가한다.
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            runOnUiThread({
                photoAdapter.setData(photoList)     // photoAdapter 에 stData 함수를 호출하여 데이터를 새로 초기화한다.
                photoPb.visibility = View.GONE      // 프로그레스 바를 안보이게한다.
            })
        }.start()
    }

    /*
     * 전체 사진을 보여주기 위해 placePhotoRv 를 초기화 및 설정하는 메소드
     */
    private fun setRecyclerView(){
        photoList = ArrayList() // photoList 를 초기화한다.
        photoAdapter = PhotoAdapter(photoList, applicationContext, placeNo) // photoAdapter 초기화
        photoAdapter.setAllPhoto(true)
        placePhotoRv.adapter = photoAdapter // placePhotoRv 에 어댑터를 photoAdapter 로 설정한다.

        val gridLayoutManager = GridLayoutManager(applicationContext,4) // Grid 형식의 레이아웃 매니저를 초기화한다.
        placePhotoRv.layoutManager = gridLayoutManager // placePhotoRv 에 레이아웃 매니저를 gridLayoutManager 로 설정한다.

        // placePhotoRv 에 클릭 리스너를 추가한다.
        // 클릭 이벤트가 발생하면 placeNo 와 현재 클릭한 position 을 intent 에 담고 ImageSlideActivity 로 이동한다.
        placePhotoRv.addOnItemTouchListener(RecyclerItemClickListener(
                applicationContext, placePhotoRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                val intent = Intent(applicationContext, ImageSlideActivity::class.java)
                intent.putParcelableArrayListExtra("photoArray",photoList)  // 전체 사진 리스트를 인텐트에 담는다.
                intent.putExtra("startPosition",position)   // 현재 position 값을 intent 에 담는다.
                startActivity(intent)   // 이미지 슬라이드 액티비티로 이동한다.
            }
            override fun onLongItemClick(view: View?, position: Int) {}})
        )
    }
}