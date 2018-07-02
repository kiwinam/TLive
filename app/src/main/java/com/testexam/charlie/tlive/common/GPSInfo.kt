package com.testexam.charlie.tlive.common

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.util.Log


/**
 * 디바이스의 GPS 를 이용하여 현재 나의 위도와 경도를 찾는 클래스
 */
@SuppressLint("Registered")
class GPSInfo(val context: Context) : Service(), LocationListener{
    private var isGPSEnabled = false // 현재 GPS 사용 유무
    private var isNetworkEnabled = false // 네트워크 사용 유무
    var isGetLocation = false // 위치를 가져올 수 있는지 저장하는 변수

    private var location : Location? = null

    var lat : Double = 0.0  // 위도
    var lon : Double = 0.0  // 경도

    private val updateMinDistance : Float = 10.0f // 최소 GPS 정보 업데이트 거리 10미터
    private val updateMinTime : Long = 1000 * 60 * 1 // 최소 GPS 정보 업데이트 시간 (1분)

    private var locationManager : LocationManager? = null


    /*
     * GPSInfo Service 를 생성하면 현재 위치를 가져오는 getLocation 메서드를 호출한다.
     */
    init{
        getCurrentLocation()
    }

    /*
     * 현재 사용자의 위치를 가져오는 메소드
     */
    @SuppressLint("LogNotTimber")
    @TargetApi(23)
    private fun getCurrentLocation(): Location? {
        // 빌드 버전이 23 이상이고, ACCESS_FINE_LOCATION , ACCESS_COARSE_LOCATION 의 권한이 부여되지 않은 경우
        if(Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null // 위치 가져오는걸 종료한다.
        }
        try{
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager? // Location Manager 설정
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) // GPS 정보 가져오기
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER) // 현재 네트워크 상태 값 알아오기

            if(!isGPSEnabled && !isNetworkEnabled){
                // GPS 와 네트워크 사용이 불가능할 때 알림
            }else{
                isGetLocation = true    // 위치 가져올 수 있는지 여부를 저장하는 변수에 true 값을 저장한다.

                // 네트워크 정보로 부터 위치 값 가져오기
                if(isNetworkEnabled){   // 네트워크를 사용할 수 있다면
                    locationManager!!.requestLocationUpdates(   // 네트워크를 사용하여서 위치 갱신을 요청한다.
                            LocationManager.NETWORK_PROVIDER,
                            updateMinTime,
                            updateMinDistance,this
                    )
                    if(locationManager != null){    // locationManager 가 초기화 되어 있다면
                        location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) // locationManager 에서 네트워크를 사용하여 현재 위치를 가져온다.
                        if(location != null){   // 가져온 위치가 null 이 아니라면
                            // 가져온 위치에서 위도와 경도를 lat, lon 변수에 저장한다.
                            lat = location!!.latitude
                            lon = location!!.longitude

                            Log.e("location", "lat : $lat, lon : $lon")
                        }
                    }
                }

                // GPS 로부터 위치 값 가져오기
                if(isGPSEnabled){
                    if(location == null){ // 네트워크 정보로부터 위치 값을 받아오지 못한 경우에만 실행한다.
                        // GPS 에 현재 위치를 갱신한다.
                        // 갱신 기간은 10분,
                        // 갱신 최소 거리는 10미터이다.
                        locationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                updateMinTime,
                                updateMinDistance,this)

                        // locationManager 가 정상적으로 생성되었을 때
                        if(locationManager != null){
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER) // locationManager 로부터 현재 위치를 가져온다.

                            if( location != null){     // 현재 위치 정보가 정상적으로 가져와졌을 때
                                lat = location!!.latitude  // 위도를 가져온다.
                                lon = location!!.longitude // 경도를 가져온다.

                                Log.e("location", "lat : $lat, lon : $lon")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
        return location
    }

    // GPS 사용 종료
    fun stopUsingGPS(){
        if(locationManager != null){
            locationManager!!.removeUpdates(this)
        }
    }
    override fun onLocationChanged(location: Location?) {

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}