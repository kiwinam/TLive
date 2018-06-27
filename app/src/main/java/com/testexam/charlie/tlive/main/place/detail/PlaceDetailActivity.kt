package com.testexam.charlie.tlive.main.place.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.place.Place
import com.testexam.charlie.tlive.main.place.detail.pathFinder.NavigationActivity
import com.testexam.charlie.tlive.main.place.detail.pathFinder.SelectPathFinder
import com.testexam.charlie.tlive.main.place.write.WriteReviewActivity
import kotlinx.android.synthetic.main.activity_place_detail.*
import kotlinx.android.synthetic.main.content_detail_basic_info.*
import kotlinx.android.synthetic.main.content_detail_info.*
import kotlinx.android.synthetic.main.content_detail_location.*
import kotlinx.android.synthetic.main.content_detail_review.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


/**
 * 맛집에 대한 상세 정보를 보여주는 Activity
 *
 */
class PlaceDetailActivity : BaseActivity(), View.OnClickListener, OnMapReadyCallback {
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var photoList : ArrayList<PlacePhoto>

    private lateinit var place : Place

    private var address = ""
    private var favoriteNum = 0
    private var workingTime = ""
    private var breakTime = ""
    private var price = ""

    private lateinit var myLatLng: LatLng

    private lateinit var menuArray : JSONArray
    private lateinit var reviewArray : JSONArray

    private val decimalFormat = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        // 지도 객체 선언
        val mapFragment = supportFragmentManager.findFragmentById(R.id.detailMap) as SupportMapFragment
        Thread{mapFragment.getMapAsync(this)}.run()

        place = intent.getParcelableExtra("place") // 선택한 맛집의 정보가 담겨있는 Place 객체를 Intent 에서 가져온다.

        val myLat = intent.getDoubleExtra("lat",0.0) // 현재 나의 위도
        val myLon = intent.getDoubleExtra("lon",0.0) // 현재 나의 경도
        myLatLng = LatLng(myLat,myLon)  // 위경도를 이용하여 나의 위치를 나타내는 LatLon 객체를 초기화한다.

        getDetailInfo(place.placeNo)    // 맛집의 상세 정보를 불러온다.

        setOnClickListeners() // 클릭 리스너 등록
        setRecyclerViews() // RecyclerView 설정
    }

    /*
     * 맛집의 상세 정보를 서버에서 불러오는 메소드
     *
     * 서버에서 정보를 불러온 다음 View 에 보여주는 setInformation() 메소드를 호출한다.
     */
    private fun getDetailInfo(placeNo : Int){
        runOnUiThread({
            detailPb.visibility = View.VISIBLE
        })
        Thread{
            try{
                val params = ArrayList<Params>()
                params.add(Params("placeNo",placeNo.toString()))
                val result = HttpTask("getDetailPlace.php",params).execute().get()

                //Log.d("getDetailInfo","result : $result")

                if(result != null){
                    val jsonObject = JSONObject(result)
                    address = jsonObject.getString("address")
                    favoriteNum = jsonObject.getInt("favoriteNum")
                    workingTime = jsonObject.getString("workingTime")
                    breakTime = jsonObject.getString("breakTime")
                    price = jsonObject.getString("price")

                    menuArray = JSONArray(jsonObject.getString("menu"))
                    val reviews = jsonObject.getString("reviews")

                    // 리뷰가 있는 경우

                    if(!reviews.equals("null")){ // reviews 가 null 이거나 blank 가 아니라면
                        /*reviewArray = JSONArray(reviews)
                        for( i in 0 until reviewArray.length()-1){
                            val photoObject = reviewArray.getJSONObject(i)
                            photoList.add(PlacePhoto(photoObject.getString("")))
                        }*/
                    }
                }
            }catch (e : Exception ){
                e.printStackTrace()
            }finally {
                setInformation()
            }
        }.start()

    }

    /*
     * 맛집 정보들을 View 에 설정하는 메소드
     */
    @SuppressLint("SetTextI18n")
    private fun setInformation(){
        detailTitleTv.text = place.name // 타이틀 바에 맛집 이름 설정
        detailNameTv.text = place.name  // detailNameTv 에 맛집 이름 설정
        detailViewerTv.text = place.viewNum.toString() // 정보 열람 회수 설정
        detailStarTv.text = favoriteNum.toString() // 가고싶다 개수 설정
        detailReviewTv.text = place.reviewNum.toString() // 리뷰 수 설정
        detailStarScoreTv.text = place.starScore.toString() // 맛집 평점 설정

        detailAddressTv.text = address  // 주소 설정
        detailWorkingTimeTv.text = workingTime // 영업 시간
        if(breakTime != ""){    // 쉬는 시간이 있으면 쉬는 시간을 표시하고 없다면 View 를 안보이게 한다.
            detailBreakTimeTv.text = breakTime
        }else{
            detailBreakTimeTv.visibility = View.GONE
            detailBreakTv.visibility = View.GONE
        }
        detailPriceTv.text = price // 가격
        detailMenu1Tv.text = menuArray.getJSONObject(0).getString("name")
        detailMenu2Tv.text = menuArray.getJSONObject(1).getString("name")
        detailMenu3Tv.text = menuArray.getJSONObject(2).getString("name")

        detailMenuPrice1Tv.text = decimalFormat.format(menuArray.getJSONObject(0).getString("price").toInt())+"원"
        detailMenuPrice2Tv.text = decimalFormat.format(menuArray.getJSONObject(1).getString("price").toInt())+"원"
        detailMenuPrice3Tv.text = decimalFormat.format(menuArray.getJSONObject(2).getString("price").toInt())+"원"
        runOnUiThread({
            detailPb.visibility = View.GONE
        })
    }

    private fun setRecyclerViews(){
        // 사진 미리보기 RecyclerView 설정
        photoList = ArrayList()
        photoAdapter = PhotoAdapter(photoList,applicationContext)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        detailPhotoRv.layoutManager = linearLayoutManager
        detailPhotoRv.adapter = photoAdapter


        // 댓글 RecyclerView 설정

    }

    /*
     * 클릭 리스너에 뷰를 등록한다.
     */
    private fun setOnClickListeners(){
        detailCloseIv.setOnClickListener(this)
        detailWantToGoLo.setOnClickListener(this)
        detailWriteReviewLo.setOnClickListener(this)
        detailUploadPhotoLo.setOnClickListener(this)
        detailGoodReviewLo.setOnClickListener(this)
        detailNormalReviewLo.setOnClickListener(this)
        detailBadReviewLo.setOnClickListener(this)
        detailFindPathLo.setOnClickListener(this)
        detailNavigationLo.setOnClickListener(this)
        detailCallTaxiLo.setOnClickListener(this)
        detailAddrCopyLo.setOnClickListener(this)
        detailCallLo.setOnClickListener(this)
        detailMoreReviewTv.setOnClickListener(this)
        detailSearchBlogTv.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            detailCloseIv->onBackPressed() // 왼쪽 상단 닫기 버튼을 누르면 Activity 종료
            detailWantToGoLo->{ // 가고싶다 버튼

            }
            detailWriteReviewLo->{ // 리뷰쓰기 버튼
                val reviewIntent = Intent(this,WriteReviewActivity::class.java)
                reviewIntent.putExtra("selectPoint",0)
                startActivity(reviewIntent)
            }
            detailUploadPhotoLo->{ // 사진 올리기 버튼

            }
            detailGoodReviewLo->{ // 맛있다 리뷰 버튼
                val reviewIntent = Intent(this,WriteReviewActivity::class.java)
                reviewIntent.putExtra("selectPoint",30)
                startActivity(reviewIntent)
            }
            detailNormalReviewLo->{ // 보통 리뷰 버튼
                val reviewIntent = Intent(this,WriteReviewActivity::class.java)
                reviewIntent.putExtra("selectPoint",20)
                startActivity(reviewIntent)
            }
            detailBadReviewLo->{ // 별로 리뷰 버튼
                val reviewIntent = Intent(this,WriteReviewActivity::class.java)
                reviewIntent.putExtra("selectPoint",10)
                startActivity(reviewIntent)
            }
            detailFindPathLo->{ // 길 찾기 버튼
                val selectPathFinder = SelectPathFinder.newInstance()
                selectPathFinder.setLatLng(myLatLng, LatLng(place.lat,place.lon)) // 나의 위치와 맛집의 위치를 설정한다.
                selectPathFinder.show(supportFragmentManager,"bottomSheet") // 바텀시트를 보여준다.
            }
            detailNavigationLo->{ // 네비게이션 버튼
                val mapBoxIntent = Intent(applicationContext, NavigationActivity::class.java)
                mapBoxIntent.putExtra("startLat",myLatLng.latitude)
                mapBoxIntent.putExtra("startLng",myLatLng.longitude)
                mapBoxIntent.putExtra("endLat",place.lat)
                mapBoxIntent.putExtra("endLng",place.lon)
                startActivity(mapBoxIntent)
            }
            detailCallTaxiLo->{ // 택시부르기 버튼

            }
            detailAddrCopyLo->{ // 주소 복사 버튼
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", address)
                clipboardManager.primaryClip = clipData
                Toast.makeText(applicationContext, "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }
            detailCallLo->{ // 전화하기 버튼

            }
            detailMoreReviewTv->{ // 리뷰 더보기 버튼

            }
            detailSearchBlogTv->{ // 블로그 검색하기 버튼

            }
        }
    }

    /*
     * 구글 지도가 준비된 다음 호출되는 메소드
     */
    override fun onMapReady(googleMap : GoogleMap?) {
        // 맛집 위치로 카메라 이동
        val latLng = LatLng(place.lat,place.lon)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,18.0f)
        googleMap!!.moveCamera(cameraUpdate)

        // 맛집 위치에 마커 생성
        googleMap.addMarker(MarkerOptions()
                .position(latLng)
                .title(place.name))
    }
}