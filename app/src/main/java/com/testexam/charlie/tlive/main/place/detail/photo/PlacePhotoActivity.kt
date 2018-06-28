package com.testexam.charlie.tlive.main.place.detail.photo

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import com.testexam.charlie.tlive.main.place.detail.imageSlide.ImageSlideActivity
import com.yanzhenjie.album.mvp.BaseActivity
import kotlinx.android.synthetic.main.activity_place_photo.*
import org.json.JSONArray

class PlacePhotoActivity : BaseActivity() {
    private lateinit var photoList : ArrayList<Photo>
    private lateinit var photoAdapter: PhotoAdapter
    private var placeNo = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_photo)

        placeNo = intent.getIntExtra("placeNo",-1) // intent 로 전달받은 placeNo 값을 가져온다.

        setRecyclerView()   // photoRv 설정을 한다.
        getAllPhotos()      // 서버에 현재 선택한 맛집의 전체 사진을 가져온다.

        placePhotoCloseIv.setOnClickListener({
            finish()
        })
    }

    /*
     * 현재 선택한 맛집에 대한 모든 사진을 서버에서 불러오는 메소드
     */
    private fun getAllPhotos(){
        Thread{
            runOnUiThread({
                photoPb.visibility = View.VISIBLE
            })

            val paramsArray = ArrayList<Params>() // 파라미터 배열 생성
            paramsArray.add(Params("placeNo",placeNo.toString())) // placeNo 파라미터에 추가

            val result = HttpTask("getAllPhotos.php",paramsArray).execute().get() // Http 통신으로 getAllPhotos.php 에 전체 사진 목록을 요청한다.
            Log.d("placePhoto ","result $result")
            if(result != "null"){
                photoList.clear()
                try{
                    val photoJSONArray = JSONArray(result)
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
                photoAdapter.setData(photoList)     // photoAdapter 에 setData 함수를 호출하여 데이터를 새로 초기화한다.
                photoPb.visibility = View.GONE
            })
        }.start()
    }

    /*
     * 전체 사진을 보여주기 위해 placePhotoRv 를 초기화 및 설정하는 메소드
     */
    private fun setRecyclerView(){
        photoList = ArrayList() // photoList 를 초기화한다.
        photoAdapter = PhotoAdapter(photoList, applicationContext, placeNo) // photoAdapter 초기화
        photoAdapter.setAllphoto(true)
        placePhotoRv.adapter = photoAdapter // placePhotoRv 에 어댑터를 photoAdapter 로 설정한다.

        val gridLayoutManager = GridLayoutManager(applicationContext,4) // Grid 형식의 레이아웃 매니저를 초기화한다.
        placePhotoRv.layoutManager = gridLayoutManager // placePhotoRv 에 레이아웃 매니저를 gridLayoutManager 로 설정한다.

        // placePhotoRv 에 클릭 리스너를 추가한다.
        // 클릭 이벤트가 발생하면 placeNo 와 현재 클릭한 position 을 intent 에 담고 ImageSlideActivity 로 이동한다.
        placePhotoRv.addOnItemTouchListener(RecyclerItemClickListener(
                applicationContext, placePhotoRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                Log.d("photo Rv", "onItemClick ($position)")
                val intent = Intent(applicationContext, ImageSlideActivity::class.java)
                intent.putParcelableArrayListExtra("photoArray",photoList)
                intent.putExtra("startPosition",position)   // 현재 position 값을 intent 에 담는다.
                startActivity(intent)
            }

            override fun onLongItemClick(view: View?, position: Int) {
            }})
        )
    }
}