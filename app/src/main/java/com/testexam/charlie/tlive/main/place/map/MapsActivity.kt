package com.testexam.charlie.tlive.main.place.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.SnapLayoutManager
import com.testexam.charlie.tlive.main.place.Place
import kotlinx.android.synthetic.main.activity_maps.*


/**
 * .. recyclerView 돌아갈 때 마다 맵 중앙 변경, 선택되는 효과
 * .. 마커 클릭될 때마다 선택되는 효과, recyclerView 포지션 이동
 * .. recyclerView 클릭되면 맛집 디테일 Activity 로 이동
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback
        ,ClusterManager.OnClusterClickListener<PositionItem>, ClusterManager.OnClusterInfoWindowClickListener<PositionItem>,
        ClusterManager.OnClusterItemClickListener<PositionItem>,
        GoogleMap.OnMarkerClickListener{


    private lateinit var mMap: GoogleMap                                // 구글 맵 객체
    private lateinit var clusterManager : ClusterManager<PositionItem>  // 클러스터 매니저

    private lateinit var mapPlaceAdapter: MapPlaceAdapter               // MapActivity 에서 맛집 리스트를 가로 RecyclerView 로 보여줄 때 사용하는 Adapter

    private var placeList : ArrayList<Place> = ArrayList()              // 인텐트로 넘어오는 맛집 리스트를 저장하는 ArrayList
    private var clusterList : ArrayList<PositionItem> = ArrayList()     // 클러스터링을 사용할 때 맵에 표시할 마커를 저장하는 ArrayList
    private var markerList : ArrayList<Marker> = ArrayList()            // 마커를 저장하는 ArrayList


    private var lat : Double = 0.0                                      // 현재 나의 위도를 저장하는 변수
    private var lon : Double = 0.0                                      // 현재 나의 경도를 저장하는 변수

    private var limitDistance = 0.0                                     // 맛집 검색의 제한 거리를 저장하는 변수

    private var lastListPosition = 0                                    // 맛집을 스크롤링 할 때 마지막 위치를 저장한다.

    private var lastMarkerPosition = 0

    private var markGap = 0
    /*
     * PlaceList 를 번호로 정렬하는 클래스
     *
     * PlaceList 가 Intent 로 넘어오면서 순서가 섞이는 문제 발생.
     * 이 문제를 해결하기 위해 Comparator 를 Implement 하는 SortByNo 클래스로 리스트를 번호 순으로 정렬한다.
     */
    /*class SortByNo : Comparator<Place>{
        override fun compare(left: Place?, right: Place?): Int {
            return left!!.no.compareTo(right!!.no)
        }
    }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        placeList  = intent.getParcelableArrayListExtra("placeList")
        val area = intent.getStringExtra("area")
        lat = intent.getDoubleExtra("lat",0.0)
        lon = intent.getDoubleExtra("lon",0.0)
        limitDistance = intent.getFloatExtra("limit",0.0f).toDouble()

        //sort(placeList,SortByNo()) // PlaceList 정렬

        mapAreaTv.text = area
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val myLatLng = LatLng(lat,lon) // 현재 위도, 경도로 LatLng 객체를 생성한다.

        var zoomLevel = 15.0f

        when(limitDistance){
            500.0-> zoomLevel = 15.0f
            1000.0 -> zoomLevel = 12.0f
            1500.0 -> zoomLevel = 10.0f
            2000.0 -> zoomLevel = 10.0f
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,zoomLevel))

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = false
        mMap.uiSettings.isRotateGesturesEnabled = false

        // 검색 제한 거리 까지 원을 그린다.
        // 원의 중심부는 현재 나의 위치이고,
        // 원의 크기는 검색 제한 거리이다.
        mMap.addCircle(CircleOptions()
                .center(LatLng(lat,lon))
                .radius(limitDistance)
                .strokeColor(ContextCompat.getColor(applicationContext,R.color.colorCircleLine))
                .fillColor(ContextCompat.getColor(applicationContext,R.color.colorCircleFill)))

        if(limitDistance >= 2000.0){    // 검색 제한 거리가 2 Km 가 넘을 경우 클러스터링을 사용하고
            setCluster()
        }else{  // 검색 제한 거리가 2 Km 이하를 경우 클러스터링을 사용하지 않는다.
            // placeList 에 있는 맛집 정보를 구글 맵에 marker 로 추가한다.
            for(i in 0 until placeList.size){
                val place = placeList[i]
                if(i==0){
                    addMarker(place,true)
                }else{
                    addMarker(place,false)
                }

            }
            mMap.setOnMarkerClickListener(this)
        }
        setPlaceRecyclerView()
    }

    /*
     *
     */
    override fun onMarkerClick(marker : Marker?): Boolean {
        val cameraUpdateFactory = CameraUpdateFactory.newLatLng(marker!!.position) // 마커의 위치 값 (Lat,Lon) 으로 LanLng 객체를 생성하고 CameraUpdateFactory 에 담는다.
        mMap.animateCamera(cameraUpdateFactory) // 마커의 위치로 카메라의 중심을 이동한다.

        val markerIndex = markerList.indexOf(marker)
        // 마커의 색상 변경
        Log.d("onMarkerClick", "markerList index = $markerIndex")
        changeMarker(lastMarkerPosition, markerIndex)

        // 마커가 클릭될 때, 클릭된 마커의 RecyclerView 아이템을 보여준다.
        /*android.os.Handler().postDelayed({
            mapPlaceRv.smoothScrollToPosition(markerIndex)
        },200)*/
        mapPlaceRv.smoothScrollToPosition(markerIndex)

        return true
    }

    /*
     *
     */
    override fun onClusterClick(p0: Cluster<PositionItem>?): Boolean {

        return true
    }

    /*
     *
     */
    override fun onClusterInfoWindowClick(p0: Cluster<PositionItem>?) {

    }

    /*
     * 클러스터링 된 마커를 클릭 했을 때 이벤트를 처리하는 메소드
     */
    override fun onClusterItemClick(item: PositionItem?): Boolean {
        val cameraUpdate = CameraUpdateFactory.newLatLng(item!!.position)
        mMap.animateCamera(cameraUpdate)
        return true
    }


    private fun setPlaceRecyclerView(){
        for(i in 0 until placeList.size){
            clusterList.add(PositionItem(placeList[i].no,placeList[i].name,placeList[i].lat,placeList[i].lon))
        }

        mapPlaceAdapter = MapPlaceAdapter(placeList,applicationContext)

        val snapLayoutManager = SnapLayoutManager(applicationContext)
        snapLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        val snapHelper = LinearSnapHelper() // 맛집 리스트 RecyclerView 가 스크롤링 될 때 아이템이 전체적으로 보이도록 Snap 효과를 준다.
        snapHelper.attachToRecyclerView(mapPlaceRv)  // SnapHelper 를 mapPlaceRv 에 부착한다.

        mapPlaceRv.layoutManager = snapLayoutManager
        mapPlaceRv.adapter = mapPlaceAdapter
        mapPlaceRv.isNestedScrollingEnabled = false
        mapPlaceRv.addOnScrollListener(object : RecyclerView.OnScrollListener(){ // mapPlaceRv 에 스크롤 리스너를 추가한다.
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val lastItemPosition = (recyclerView!!.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() // 마지막으로 보고 있는 아이템의 위치 값

                // 현재 위치가 이전에 보고 있던 리스트의 위치가 아니라면
                // 활성화된 마커와 비활성화 된 마커를 변경한다.
                if(lastListPosition != lastItemPosition){
                    // 마커를 클릭해서 스크롤할 때 onScrolled 메서드가 호출되어서 타겟 position 까지 가는데 모든 마커들을 활성화했다 비활성화 하는 작업이 생김
                    // 이 문제를 해결하기 위해 서로 이웃하는 (현재 활성화된 마커의 위치와 타겟 마커의 위치 차이가 절대값으로 1이라면) 타겟을 제외하고
                    // 나머지 타겟 포지션에 대한 이동을 markGap 변수에 저장한다.
                    val gap = Math.abs(lastMarkerPosition - lastItemPosition) // 현재 활성화된 마커와 타겟 마커간의 position 값 차이를 구한다.
                    if(gap > 1) { // 차이값이 1보다 크면 (이웃하지 않는다면)
                        markGap = gap // markGap 에 차이값을 넣는다.
                    }

                    // markGap 값이 0보다 크면 , markGap 값을 -1 한다.
                    if(markGap > 0){
                        markGap--
                        Log.d("markGap","$markGap ..")

                    // markGap 값이 0 이라면 모든 아이템들이 스크롤 되었다 판단한다.
                    // 마지막으로 스크롤 될 때 타겟 포지션의 마커만 활성화한다.
                    }else{

                        val place : Place = placeList[lastItemPosition] // 현재 선택된 index 로 place 객체를 가져옴
                        val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng(place.lat,place.lon)) // place 가 가지고 있는 위치 값 (위경도) 으로 카메라의 중심점을 생성한다.
                        mMap.animateCamera(cameraUpdate) // 카메라 위치 변경

                        // 선택된 마커 색상 변경
                        changeMarker(lastMarkerPosition,lastItemPosition)
                    }
                    lastListPosition = lastItemPosition // 현재 위치를 마지막 위치로 저장함.

                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    /*
     *
     */
    @SuppressLint("InflateParams")
    private fun addMarker (place : Place, isSelected : Boolean) : Marker {
        val marker: View = (this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker_selected,null)
        val numTv : TextView = marker.findViewById(R.id.markerNumTv)
        val markerIv = marker.findViewById<ImageView>(R.id.markerIv)
        numTv.text = place.no.toString()
        if(isSelected){
            numTv.setTextColor(ContextCompat.getColor(applicationContext,R.color.colorPrimary))
            markerIv.setImageDrawable(getDrawable(R.drawable.ic_location_orange))
        } else {
            numTv.setTextColor(ContextCompat.getColor(applicationContext,R.color.colorGray))
            markerIv.setImageDrawable(getDrawable(R.drawable.ic_location_gray))
        }
        val markers = mMap.addMarker(
                MarkerOptions()
                        .position(LatLng(place.lat,place.lon))
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(marker)))
                        .title(place.name))
        markerList.add(markers)
        return markers
    }

    /*
     * 마커가 선택 될 때마다 마커의 색상을 변경한다.
     * 1. 기존의 선택된 마커의 색상을 원래대로 돌린다.
     * 2. 새로 선택된 마커의 색상을 primary 로 변경한다.
     *
     */
    @SuppressLint("InflateParams")
    private fun changeMarker(oldPosition : Int, newPosition : Int){
        // 기존의 선택된 마커의 색상을 원래대로 돌린다.

        if(oldPosition != -1){
            val oldPlace = placeList[oldPosition]
            val oldMarker = markerList[oldPosition]
            val marker = (this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker_selected,null)
            val numTv : TextView = marker.findViewById(R.id.markerNumTv)
            val markerIv = marker.findViewById<ImageView>(R.id.markerIv)

            numTv.text = oldPlace.no.toString()
            numTv.setTextColor(ContextCompat.getColor(applicationContext,R.color.colorGray))
            markerIv.setImageDrawable(getDrawable(R.drawable.ic_location_gray))

            val markers = mMap.addMarker(
                    MarkerOptions()
                            .position(oldMarker.position)
                            .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(marker)))
                            .title(oldPlace.name))
            oldMarker.remove()
            markerList.add(oldPosition,markers)
            markerList.remove(oldMarker)

        }

        // 새로 선택된 마커의 색상을 primary 로 변경한다.
        if(newPosition != -1){
            val newPlace = placeList[newPosition]
            val newMarker = markerList[newPosition]
            val marker =  (this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker_selected,null)
            val numTv : TextView = marker.findViewById(R.id.markerNumTv)
            val markerIv = marker.findViewById<ImageView>(R.id.markerIv)

            numTv.text = newPlace.no.toString()
            numTv.setTextColor(ContextCompat.getColor(applicationContext,R.color.colorPrimary))
            markerIv.setImageDrawable(getDrawable(R.drawable.ic_location_orange))

            val markers = mMap.addMarker(
                    MarkerOptions()
                            .position(newMarker.position)
                            .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(marker)))
                            .title(newPlace.name))

            newMarker.remove()
            markerList.add(newPosition,markers)
            markerList.remove(newMarker)
        }
        lastMarkerPosition = newPosition
    }

    private fun createDrawableFromView(view : View) : Bitmap{
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        //view.layoutParams = ViewGroup.LayoutParams(36, 36)
        view.measure(displayMetrics.widthPixels,displayMetrics.heightPixels)
        view.layout(0,0,displayMetrics.widthPixels,displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.measuredWidth,view.measuredHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private fun setCluster(){
        clusterManager = ClusterManager(this,mMap)
        clusterManager.renderer = CustomIconRenderer(this,this,mMap, clusterManager)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        mMap.setOnInfoWindowClickListener(clusterManager)
        clusterManager.setOnClusterClickListener(this)
        clusterManager.setOnClusterInfoWindowClickListener(this)
        clusterManager.setOnClusterItemClickListener(this)
        //clusterManager.setOnClusterItemInfoWindowClickListener(this)

        for(i in 0 until clusterList.size){
            clusterManager.addItem(clusterList[i])
        }
        clusterManager.cluster()
    }



    /**
     * 클러스터링을 사용하기 위해 DefaultClusterRenderer 를 오버라이딩하는 클래스
     */
    class CustomIconRenderer(val activity : MapsActivity,val context: Context?,val map: GoogleMap?, clusterManager: ClusterManager<PositionItem>?) : DefaultClusterRenderer<PositionItem>(context, map, clusterManager) {

        @SuppressLint("InflateParams")
        override fun onBeforeClusterItemRendered(item: PositionItem?, markerOptions: MarkerOptions?) {
            val marker: View = (context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker_unselected,null)
            val numTv = marker.findViewById<TextView>(R.id.unMarkerNumTv)
            numTv.text = item!!.getNo().toString()
            /*map!!.addMarker(markerOptions!!
                    .position(item.position)
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(marker))))*/
            Log.d("customIconRenderer", item.getName())
            markerOptions!!
                    .position(item.position)
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(marker)))
        }

        override fun shouldRenderAsCluster(cluster: Cluster<PositionItem>?): Boolean {
            return cluster!!.size > 1
        }

        private fun createDrawableFromView(view : View) : Bitmap{
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            //view.layoutParams = ViewGroup.LayoutParams(36, 36)
            view.measure(displayMetrics.widthPixels,displayMetrics.heightPixels)
            view.layout(0,0,displayMetrics.widthPixels,displayMetrics.heightPixels)
            view.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(view.measuredWidth,view.measuredHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(bitmap)
            view.draw(canvas)

            return bitmap
        }
    }
}
