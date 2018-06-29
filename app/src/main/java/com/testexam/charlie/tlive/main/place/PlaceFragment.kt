package com.testexam.charlie.tlive.main.place

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.*
import com.testexam.charlie.tlive.main.place.detail.PlaceDetailActivity
import com.testexam.charlie.tlive.main.place.map.MapsActivity
import com.testexam.charlie.tlive.main.place.map.SelectSearchRange
import kotlinx.android.synthetic.main.fragment_place.*
import org.json.JSONArray
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

/**
 * 맛집 리스트를 보여주는 Fragment
 * !! 추가 정보 기입해야함
 *
 * Created by charlie on 2018. 5. 24
 */
class PlaceFragment : Fragment() , View.OnClickListener{
    private val permissionsLocation : Array<String> = Array(1, init = { android.Manifest.permission.ACCESS_FINE_LOCATION })
    private val permissionsCoarseLocation : Array<String> = Array(1, init = { android.Manifest.permission.ACCESS_COARSE_LOCATION })
    private val permissionCode = 1000 // ACCESS_FINE_LOCATION 에 대한 요청 코드
    private val permissionCoarseCode = 1001 // ACCESS_COARSE_LOCATION 에 대한 요청 코드

    private var isPermission = false // 현재 위치를 확인하는 권한이 있는지 없는지 판단하는 변수
    private var isAccessFineLocation = false // ACCESS_FINE_LOCATION 가 승인 되었는지 판단하는 변수
    private var isAccessCoarseLocation = false // ACCESS_COARSE_LOCATION 가 승인 되었는지 판단하는 변수

    private var gps : GPSInfo? = null // 현재 위치 정보를 가져오는 서비스인 GPSInfo

    private var area = "" // 현재 위치의 행정 구역
    private var lat : Double = 0.0 // 현재 위치의 위도
    private var lon  : Double = 0.0 // 현재 위치의 경도
    private var limitDistance : Float = 500.0f // 맛집 검색시 최대 거리

    private var placeList : ArrayList<Place> = ArrayList() // 맛집 리스트들을 담는 ArrayList
    private var placeAdapter : PlaceAdapter? = null // 맛집 RecyclerView 를 설정하는 어댑터


    companion object {
        fun newInstance(): PlaceFragment = PlaceFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_place,container,false)
        //return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        setClickListeners()
        setPlaceRecyclerView() // 맛집 RecyclerView 설정
        checkSelfPermission() // 권한 확인

        if(!isPermission){ // 위치에 관한 권한이 승인 되었는지
            callPermission() // 권한이 승인 되지 않았다면 권한을 요청한다.
        }else{ // 권한이 승인 되었다면 위치를 가져온다.
            gps = GPSInfo(context!!)
            if(gps!!.isGetLocation){
                lat = gps!!.lat // gps 에서 위도를 가져온다.
                lon = gps!!.lon // gps 에서 경도를 가져온다.

                val gCoder = Geocoder(context,Locale.KOREA) // 위경도 변환할 Geo coder 를 생성한다. 지역은 한국으로 국한한다.

                try{
                    val addr = gCoder.getFromLocation(lat,lon,1) // lat , lon 으로 가져온 위치를 geo coder 를 이용하여 주소로 만든다.
                    val address : Address = addr[0] // 첫 번째 주소를 가져온다.
                    for(i in 0 .. address.maxAddressLineIndex){  // 주소의 최대 길이까지 주소를 가져온다.
                        val addressSlice = address.getAddressLine(i).split(" ") // 주소를 공백을 기준으로 배열로 만든다.
                        for(j in 0 until addressSlice.size){
                            if(addressSlice[j].endsWith("구")) { // 주소 배열에서 끝 글자가 '구' 로 끝나는 배열을 가져와 area 변수에 저장한다.
                                area = addressSlice[j] // 어느 행정 구역에 있는지 area 에 저장한다.
                                break
                            }
                        }
                    }
                    placeAreaTv.text = area // 행정 구역을 TextView 에 표시한다.
                    getPlaceList()  // 현재 위치를 기준으로 서버에서 맛집 리스트를 가져온다.
                }catch(e : IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v){
            placeMapIv->{
                val mapIntent = Intent(context, MapsActivity::class.java)
                mapIntent.putParcelableArrayListExtra("placeList",placeList)
                mapIntent.putExtra("area",area)
                mapIntent.putExtra("lat",lat)
                mapIntent.putExtra("lon",lon)
                mapIntent.putExtra("limit",limitDistance)
                startActivity(mapIntent)
            }
            placeLocationLo->{
                val selectSearchRange = SelectSearchRange.newInstance()
                selectSearchRange.setData(limitDistance.toDouble())
                selectSearchRange.setRangeListener(RangeListener { range, rangeIndex ->
                    val rangeLimit = arrayOf(100.0,300.0,500.0,1000.0,3000.0)
                    placeLimitTv.text = range
                    limitDistance = rangeLimit[rangeIndex].toFloat()

                    // 리스트 삭제
                    placeList.clear()

                    // 새로운 맛집 리스트 요청
                    getPlaceList()
                })
                selectSearchRange.show(fragmentManager,"rangeSheet")
            }
        }
    }

    /*
     * 위치 정보 접근 전에 현재 권한을 확인하는 메소드
     */
    private fun checkSelfPermission(){
        isPermission = (ContextCompat.checkSelfPermission(context!!,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context!!,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    /*
     * 위치 정보에 접근하기 위한 권한을 요청하는 메소드
     */
    private fun callPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(context!!,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissionsLocation,permissionCode)
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(context!!,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissionsCoarseLocation,permissionCoarseCode)
        }else{
            isPermission = true
        }
    }

    /*
     * 위치 정보 권한 요청에 대한 처리 결과
     * 요청이 수락되었다면 isPermission 를 true 로 설정한다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // ACCESS_FINE_LOCATION 로 요청한 퍼미션이 수락되었을 때
        if(requestCode == permissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isAccessFineLocation = true
        }else if(requestCode == permissionCoarseCode && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isAccessCoarseLocation = true
        }

        if(isAccessFineLocation && isAccessCoarseLocation){
            isPermission = true
        }
    }

    // 클릭 리스너들 설정
    private fun setClickListeners(){
        placeMapIv.setOnClickListener(this)
        placeLocationLo.setOnClickListener(this)
    }

    /*
     * 맛집 Recycler View 를 설정하는 메소드
     */
    private fun setPlaceRecyclerView(){
        placeList = ArrayList()
        placeAdapter = PlaceAdapter(placeList, context!!)

        val layoutManager = GridLayoutManager(context,2)
        placeRv.adapter = placeAdapter
        placeRv.layoutManager = layoutManager

        // 맛집 RecyclerView 에 클릭 리스너 추가.
        // 맛집 아이템을 클릭하면 PlaceDetailActivity 로 이동하여 추가 정보를 볼 수 있다.
        placeRv.addOnItemTouchListener(RecyclerItemClickListener(
                context, placeRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                val place = placeList[position]
                val intent = Intent(context, PlaceDetailActivity::class.java)
                intent.putExtra("place",place)
                intent.putExtra("lat",lat)
                intent.putExtra("lon",lon)
                context!!.startActivity(intent)
            }

            override fun onLongItemClick(view: View?, position: Int) {
            }})
        )
    }

    /*
     * 현재 위치를 기반으로 맛집 정보를 서버에서 가져온다.
     *
     * 서버에 보내는 파라미터는 lat, lon, limitDistance 가 있다.
     * 응답받은
     * @Return : JSONArray
     * @Return : JSONObject : no , name, nearStation , starScore, category, viewNum, reviewNum, distance, previewSrc
     */
    private fun getPlaceList(){
        Thread{
            try{
                activity!!.runOnUiThread({
                    placePb.visibility = View.VISIBLE  // Progress bar 를 보여준다.
                })

                val params : ArrayList<Params> = ArrayList() // 서버에 전송할 파라미터를 설정한다.
                params.add(Params("lat",lat.toString())) // 파라미터에 lat, 경도 를 설정한다.
                params.add(Params("lon",lon.toString())) // 파라미터에 lon, 위도 를 설정한다.
                params.add(Params("limitDistance",limitDistance.toString())) // 파라미터에 limitDistance, 최대 검색 거리 를 설정한다.

                val result = HttpTask("getPlaceList.php",params).execute().get() // 서버에 getPlaceList.php 로 요청을 전송한다. 결과는 result 변수에 저장한다.
                if(result != "[]"){ // 서버의 처리 결과가 null 이 아닐 경우
                    val placeArray = JSONArray(result) // 처리 결과를 JSONArray 형식으로 변환한다.
                    placeList.clear() // 기존의 placeList 를 초기화한다.

                    for(i in 0 .. (placeArray.length()-1)){
                        val place = placeArray.getJSONObject(i)
                        placeList.add(Place(
                                (i+1),                                          // no
                                place.getInt("placeNo"),                // placeNo
                                place.getString("name"),                // name
                                place.getDouble("lat"),                 // lat
                                place.getDouble("lon"),                 // lon
                                place.getString("nearStation"),         // nearStation
                                place.getDouble("starScore").toFloat(), // starScore
                                place.getString("category"),            // category
                                place.getInt("viewNum"),                // viewNum
                                place.getInt("reviewNum"),              // reviewNum
                                place.getInt("distance"),               // distance
                                place.getString("previewSrc")           // previewSrc
                        ))
                    }
                    activity!!.runOnUiThread({
                        //val controller = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_fall_down)
                        //placeRv.layoutAnimation = controller
                        placeAdapter!!.setData(placeList)
                        //placeRv.scheduleLayoutAnimation()
                    })

                }
            }catch (e : Exception ){
                e.printStackTrace()
            }finally {
                activity!!.runOnUiThread({
                    placePb.visibility = View.GONE
                })
            }
        }.start()
    }


    override fun onPause() {
        if (gps != null){
            gps!!.stopUsingGPS()
        }
        super.onPause()
    }
}

