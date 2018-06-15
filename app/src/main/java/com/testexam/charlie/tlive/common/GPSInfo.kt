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

@SuppressLint("Registered")
class GPSInfo(val context: Context) : Service(), LocationListener{
    var isGPSEnabled = false // 현재 GPS 사용 유무

    var isNetworkEnabled = false // 네트워크 사용 유무

    var isGetLocation = false // GPS 상태값

    var location : Location? = null

    var lat : Double = 0.0

    var lon : Double = 0.0

    private final val updateMinDistance : Float = 10.0f // 최소 GPS 정보 업데이트 거리 10미터
    private final val updateMinTime : Long = 1000 * 60 * 1 // 최소 GPS 정보 업데이트 시간 (1분)

    protected var locationManager : LocationManager? = null


    /*
     * GPSInfo Service 를 생성하면 현재 위치를 가져오는 getLocation 메서드를 호출한다.
     */
    init{
        getCurrentLocation()
    }

    /*
     * 현재 사용자의 위치를 가져오는 메소드
     */
    @TargetApi(23)
    private fun getCurrentLocation(): Location? {
        // 빌드 버전이 23 이상이고, ACCESS_FINE_LOCATION , ACCESS_COARSE_LOCATION 의 권한이 부여되지 않은 경우
        if(Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        try{
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager? // Location Manager 설정
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) // GPS 정보 가져오기
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER) // 현재 네트워크 상태 값 알아오기

            if(!isGPSEnabled && !isNetworkEnabled){
                // GPS 와 네트워크 사용이 불가능할 때 알림
            }else{
                isGetLocation = true

                // 네트워크 정보로 부터 위치 값 가져오기
                if(isNetworkEnabled){
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            updateMinTime,
                            updateMinDistance,this
                    )
                    if(locationManager != null){
                        location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if(location != null){
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