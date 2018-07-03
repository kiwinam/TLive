package com.testexam.charlie.tlive.main.place.detail.ar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.Mapbox;
import com.google.android.gms.maps.model.LatLng;

import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.common.BaseActivity;
import com.testexam.charlie.tlive.main.place.detail.ar.model.EndLocation;
import com.testexam.charlie.tlive.main.place.detail.ar.model.StartLocation;
import com.testexam.charlie.tlive.main.place.detail.ar.model.Step;
import com.testexam.charlie.tlive.main.place.detail.ar.model.util.ArFragmentSupport;
import com.testexam.charlie.tlive.main.place.detail.ar.model.util.LocationCalc;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * AR 로 맛집까지의 길을 찾는 Activity
 *
 */
@SuppressLint("LogNotTimber")
public class ARNavigationActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private String TAG = "ARNavigationActivity";    // 로그용 태그

    private Step steps[]; // 경로의 Step 들을 저장하는 배열
    private World world; // AR 을 그릴 world (ar 세계)

    private Location mLastLocation; // 마지막 위치를 저장하는 변수
    //private FusedLocationProviderClient mLastLocationProvider;

    private GoogleApiClient mGoogleApiClient; // 구글 API 클라이언트

    private DirectionsRoute currentRoute; // Map Box 에서 경로를 가져와 저장하는 변수


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_navigation);
        Mapbox.getInstance(this, getString(R.string.access_token)); // MapBox 에 access token 을 설정한다.

        setGoogleApiClient(); // 구글 API 클라이언트 설정
    }

    /* 구글 API 클라이언트를 설정한다. */
    private void setGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /*
     * AR 설정하는 메소드
     */
    @SuppressWarnings("ConstantConditions")
    private void configureAR() {
        List<List<Point>> polylineLatLngList = new ArrayList<>();   // 폴리라인 리스트를 초기화한다.

        world = new World(getApplicationContext()); // AR 객체들을 그릴 World 객체를 초기화한다.

        world.setGeoPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude());    // 월드에 위치를 현재 위경도로 설정한다.
        Log.d(TAG,"Configure_AR: LOCATION" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());

        world.setDefaultImage(R.drawable.ar_sphere_default);    // 월드 오브젝트의 기본 이미지를 원으로(ar_sphere_default) 설정한다.

        // AR 를 그릴 프레그먼트를 뷰에서 찾아온다.
        ArFragmentSupport arFragmentSupport = (ArFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.ar_cam_fragment);

        Log.d(TAG,"Configure_AR: STEP.LENGTH:"+ steps.length);

        /*
         * Steps 에 있는 주요 위치를 polyLine 으로 만든다.
         */
        for(int i=0;i<steps.length;i++){
            Step step = steps[i];
            //polylineLatLngList.add(i,PolyUtil.decode((step.getGeometry())));

            // step 객체가 가지고 있는 geometry 를 map box util 로 디코딩 한다.
            // 디코딩된 geometry 는 폴리 라인 위치 리스트를 추가해준다.
            polylineLatLngList.add(i, PolylineUtils.decode(Objects.requireNonNull(step.getGeometry()), Constants.PRECISION_6)); // 폴리라인 디코딩
            Log.d("steps["+i+"] ",PolyUtil.decode((step.getGeometry())).toString());

            String modifier = step.getModifier();   // 좌회전, 우회전을 표시하는 modifier 를 가져온다.

            // AR 시작 지점
            if(i == 0){ // 첫 번째 폴리라인이라면 시작 지점이다.
                GeoObject startObject = new GeoObject(10000+i); // 새로운 GeoObject 를 만든다.
                startObject.setImageResource(R.drawable.start); // 시작 지점의 이미지를 start 로 설정한다.
                startObject.setGeoPosition(Objects.requireNonNull(step.getStartLocation()).getLat(),step.getStartLocation().getLng());  // GeoObject 의 위치를 step 객체에서 가져온다.
                world.addBeyondarObject(startObject);   // 월드에 AR 오브젝트를 추가한다.
                Log.e("AR Make","Configure_AR: START SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");

            }else if(i==steps.length-1){ // AR 마지막 지점
                GeoObject lastObject = new GeoObject(10000+i);  // 새로운 GeoObject 를 만든다.
                lastObject.setImageResource(R.drawable.stop);   // 마지막 지점의 이미지를 stop 로 설정한다

                LatLng latLng = SphericalUtil.computeOffset(    // step 에서 가져온 위치를 위경도 객체로 변경한다.
                        new LatLng(step.getStartLocation().getLat(),step.getStartLocation().getLng()),4f,SphericalUtil.computeHeading(
                                new LatLng(step.getStartLocation().getLat(),step.getStartLocation().getLng()),
                                new LatLng(step.getStartLocation().getLat(),step.getStartLocation().getLng())
                        )
                );
                lastObject.setGeoPosition(latLng.latitude,latLng.longitude);    // lastObject 의 위치를 설정한다.
                world.addBeyondarObject(lastObject);    // 월드에 AR 오브젝트를 추가한다.
                Log.e("AR Make","Configure_AR: LAST SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
            }else{
                // 시작과 끝 지점을 제외한 주요 오브젝트를 월드에 추가한다.
                // 시작과 끝을 제외한 주요 오브젝트는 좌회전과 우회전 오브젝트이다.
                if(modifier!=null){
                    if(modifier.equals("left")){    // 좌회전일 경우
                        GeoObject leftObject = new GeoObject(10000+i);  // 새로운 GeoObject 를 만든다.
                        leftObject.setImageResource(R.drawable.turn_left);  // 좌회전 이미지를 turn_left 로 설정한다.
                        leftObject.setGeoPosition(step.getStartLocation().getLat(),step.getStartLocation().getLng());   // step 에서 가져온 위치를 설정한다.

                        world.addBeyondarObject(leftObject);    // 월드에 AR 오브젝트를 추가한다.
                        //Timber.tag("AR Make").e("Configure_AR: LEFT SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                        Log.e("AR Make","Configure_AR: LEFT SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                    }else if(modifier.equals("right")){ // 우회전일 경우
                        GeoObject rightObject = new GeoObject(10000+i); // 새로운 GeoObject 를 만든다.
                        rightObject.setImageResource(R.drawable.turn_right); // 우회전 이미지를 turn_right 로 설정한다.
                        rightObject.setGeoPosition(step.getStartLocation().getLat(),step.getStartLocation().getLng());  // step 에서 가져온 위치를 설정한다.

                        world.addBeyondarObject(rightObject);    // 월드에 AR 오브젝트를 추가한다.
                        //Timber.tag("AR Make").e("Configure_AR: RIGHT SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                        Log.e("AR Make","Configure_AR: RIGHT SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                    }
                }else{
                    //Timber.tag("AR Make").e("modifier is null");
                    Log.e("AR Make","modifier is null");
                }
            }
        }


        /*
         * 폴리라인의 중간 지점에 촘촘하게 원을 그린다.
         * 시작, 끝 사이에 폴리라인을 따라 원을 그려준다.
         */
        int tempPolyCount = 0;      // 임시 폴리라인 카운트
        int tempInterPolyCount = 0; // 임시 내부 폴리라인 카운트

        for(int j = 0; j < polylineLatLngList.size(); j++){ // 폴리라인의 사이즈만큼
            for(int k = 0; k < polylineLatLngList.get(j).size(); k++){  // 폴리라인에서 찍힌 중간 지점 만큼
                Point polyPoint = polylineLatLngList.get(j).get(k); // 교차로 포인트 객체를 가져온다.
                List<Point> polyList = polylineLatLngList.get(j);
                GeoObject polyGeoObject = new GeoObject(1000+tempPolyCount++);  // 새로운 GeoObject 를 만든다.

                polyGeoObject.setGeoPosition(polyPoint.latitude(),polyPoint.longitude());   // 포인트 객체에서 위경도를 가져온다.
                polyGeoObject.setImageResource(R.drawable.ar_sphere_150x);  // 붉은 색 원으로 이미지를 설정한다.
                polyGeoObject.setName("arObj"+j+k); // 오브젝트에 일므을 설정한다.

                try{
                    if(polyList.size() > k+1){
                        double dist = LocationCalc.haversine(   // 다음 교차로 포인트와 거리를 구한다.
                                polyPoint.latitude(), polyPoint.longitude(),
                                polyList.get(k+1).latitude(), polyList.get(k+1).longitude() ) * 1000;

                        // polyObject 간의 거리 차이가 6이 넘는다면
                        if(dist > 6){
                            int arObjectCount = ((int) dist/3 ) - 1;

                            double heading = SphericalUtil.computeHeading(  // 다음 교차로의 위치를 구한다.
                                    new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                    new LatLng(polyList.get(k+1).latitude(),polyList.get(k+1).longitude()));

                            LatLng tempLatLng = SphericalUtil.computeOffset(    // 증가할 임시 위치를 구한다.
                                    new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                    3f,
                                    heading );


                            double incrementDist = 3f;      // 하나의 하얀색 원이 증가할 거리

                            for(int i = 0; i < arObjectCount; i++){
                                GeoObject interPolyGeoObject = new GeoObject(5000+tempInterPolyCount++);    // 새로운 중간 오브젝트를 만든다.

                                if( i > 0 && k < polylineLatLngList.size()){
                                    incrementDist += 3f;    // 거리를 증가시킨다.

                                    tempLatLng = SphericalUtil.computeOffset(   // 교차로 포인트와 다음 교차로 포인트에 중간 오브젝트의 위경도를 구한다.
                                            new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                            incrementDist,
                                            SphericalUtil.computeHeading(
                                                    new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                                    new LatLng(polyList.get(k+1).latitude(), polyList.get(k+1).longitude())
                                            )
                                    );
                                }

                                // 이미지 이름 설정
                                interPolyGeoObject.setGeoPosition(tempLatLng.latitude, tempLatLng.longitude);   // 중간 오브젝트의 위경도를 설정한다.
                                interPolyGeoObject.setImageResource(R.drawable.ar_sphere_default_125x); // 중간 오브젝트의 이미지를 하얀색 원으로 설정한다.
                                interPolyGeoObject.setName("inter_arObj"+j+k+i);    // 이름을 설정한다.

                                world.addBeyondarObject(interPolyGeoObject);    // 월드에 중간 오브젝트를 추가한다.
                                //Timber.tag("AR Make").e("Configure_AR: INTER SIGN:" + i + "(" + interPolyGeoObject.getLatitude() + "," + interPolyGeoObject.getLongitude() + ")");
                                Log.e("AR Make","Configure_AR: INTER SIGN:" + i + "(" + interPolyGeoObject.getLatitude() + "," + interPolyGeoObject.getLongitude() + ")");
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                world.addBeyondarObject(polyGeoObject); // 월드에 교차로 포인트를 가지고 있는 오브젝트를 추가한다.
                //Timber.tag("AR Make").e("Configure_AR: POLY SIGN:" + "(" + polyGeoObject.getLatitude() + "," + polyGeoObject.getLongitude() + ")");
                Log.e("AR Make","Configure_AR: POLY SIGN:  (" + polyGeoObject.getLatitude() + "," + polyGeoObject.getLongitude() + ")");
            }
        }

        arFragmentSupport.setWorld(world); // Fragment 에 world 를 보여준다.
        //Timber.tag("AR Make").e("%s objects are added", world.getBeyondarObjectLists().size());
        Log.e("AR Make",world.getBeyondarObjectLists().size()+"objects are added");
    }

    /**
     * MapBox API 로 Directions 을 요청하고 길 안내에 사용할 경로를 받는다.
     *
     * 1.
     *
     * @param startPoint - 시작 위치의 Point 값
     * @param endPoint - 끝 위치의 Point 값
     */
    private void getRoute(Point startPoint, Point endPoint) {

        NavigationRoute.builder(this)   // 네비게이션 경로를 만드는  MapBox Api 를 생성한다.
                .accessToken(Mapbox.getAccessToken())   // 토큰을 설정한다.
                .origin(startPoint) // 출발 지점을 설정한다.
                .destination(endPoint)  // 도착 지점을 설정한다.
                .build()
                .getRoute(new Callback<DirectionsResponse>() {  // 경로를 가져온다.
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        Timber.tag(TAG).d("Response code: %s", response.code());
                        if (response.body() == null) {
                            Timber.tag(TAG).e("경로를 가져오지 못했습니다. 엑세스 토큰을 확인해주세요");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Timber.tag(TAG).e("경로가 없습니다.");
                            return;
                        }

                        currentRoute = response.body().routes().get(0); // http response 로 가져온 경로를 저장한다.

                        //Timber.tag("duration").e(currentRoute.legs().get(0).duration().toString());
                        Log.e("duration",currentRoute.legs().get(0).duration().toString());
                        //Timber.tag("distance").e(currentRoute.legs().get(0).distance().toString());
                        Log.e("distance",currentRoute.legs().get(0).distance().toString());
                        //Timber.tag("steps size").e(String.valueOf(currentRoute.legs().get(0).steps().size()));
                        Log.e("steps size",String.valueOf(currentRoute.legs().get(0).steps().size()));

                        int stepSize = currentRoute.legs().get(0).steps().size();   // 경로의 총 스텝 사이즈를 구한다.
                        steps=new Step[stepSize];

                        for(int i=0;i<stepSize;i++) {   // 경로의 수만큼
                            LegStep step = currentRoute.legs().get(0).steps().get(i);
                            System.out.println(step.toString());

                            StartLocation startLocation = new StartLocation(step.maneuver().location().latitude(),step.maneuver().location().longitude());  // 스텝의 시작 위치를 가져온다.
                            EndLocation endLocation = new EndLocation(step.intersections().get(0).location().latitude(),step.intersections().get(0).location().longitude()); // 스텝의 도착 위치를 가져온다.
                            String geometry = step.geometry();  // 스텝의 geometry 값을 가져온다.
                            String type = step.maneuver().type();   // 스텝의 type 값을 가져온다.
                            String modifier = step.maneuver().modifier();   // 스텝의 구분자를 가져온다.

                            steps[i] = new Step(startLocation,endLocation,geometry,type,modifier);  // 가져온 정보로 새로운 Step 객체를 만든다.

                            /*Log.d(TAG,"onResponse: STEP " + i + ": " + steps[i].getStartLocation().getLat() + "/" + steps[i].getStartLocation().getLng() + ">>"
                                    + steps[i].getEndLocation().getLat() + "/" + steps[i].getEndLocation().getLng());
                            Log.d("geometry",i+">"+geometry+"..");*/
                        }
                        configureAR();  // 경로 정보를 불러온 다음 AR 설정을 진행한다.
                    }

                    @Override
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
                        Timber.tag(TAG).e("Error: %s", throwable.getMessage());
                    }
                });


    }

    /*
     * 구글 api 와 연결 되었을 때 위치 권한을 검사한다.
     */
    @Override
    public void onConnected(@android.support.annotation.Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) { // 권한이 승인되지 않았을 때
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);  // 권한 요청을 한다.
        } else {
            //LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            //String locationProvider = LocationManager.NETWORK_PROVIDER;
            //mLastLocationProvider = locationManager.getLastKnownLocation(locationProvider);

            //noinspection deprecation
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient); // Deprecated
            //mLastLocationProvider = LocationServices.getFusedLocationProviderClient(this);

            try {
                getIntents(); // 인텐트에서 정보를 가져온다.
            }catch (Exception e){
                e.printStackTrace();
                Timber.tag(TAG).d("onCreate: Intent Error");
            }
        }
        startLocationUpdates(); // 위치 정보를 업데이트한다.
    }

    /* 인텐트로 데이터를 전달받는다. */
    private void getIntents() {
        Double myLocationLat = getIntent().getDoubleExtra("startLat", 0.0); // 출발 위치의 위경도
        Double myLocationLng = getIntent().getDoubleExtra("startLng", 0.0); // 도착 위치의 위경도
        // 위경도를 map box 형태로 변환
        com.mapbox.mapboxsdk.geometry.LatLng startLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(myLocationLat, myLocationLng);
        com.mapbox.mapboxsdk.geometry.LatLng endLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(getIntent().getDoubleExtra("endLat",0.0),getIntent().getDoubleExtra("endLng",0.0));

        Point startPoint = Point.fromLngLat(startLatLng.getLongitude(),startLatLng.getLatitude());  // 시작 위치
        Point endPoint = Point.fromLngLat(endLatLng.getLongitude(),endLatLng.getLatitude());    // 도착 위치

        getRoute(startPoint, endPoint); // 시작 위치와 도착 위치를 매개 변수로 전달하여 경로를 찾는다.
    }

    /*
     * 위치 정보를 갱신한다.
     */
    protected void startLocationUpdates() {
        try {
            //noinspection deprecation
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);   // 위치 갱신을 요청한다.
            /*mLastLocationProvider.getLastLocation().addOnSuccessListener(this, location -> {
                if(location != null){
                    mLastLocation = location;
                    Timber.d("location Update %l %l",location.getLatitude(), location.getLongitude());
                }
            });*/

        }catch (SecurityException e){
            Toast.makeText(this, "Location Permission not granted . Please Grant the permissions",
                    Toast.LENGTH_LONG).show();
        }
    }

    /*
     * 내 위치가 변경될 때마다 AR 월드에서 나의 위치를 갱신하는 메소드
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d("location Change",location.getLatitude()+","+location.getLongitude());
        if(world!=null) {
            world.setGeoPosition(location.getLatitude(), location.getLongitude());  // AR 월드에서 나의 위치를 갱신한다.
            Log.d("world location",world.getLatitude()+","+world.getLongitude());
        }
    }
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /* Activity 가 시작될 때 구글 api 를 연결한다. */
    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    /* Activity 가 stop 될 때 구글 api 의 연결을 끊는다. */
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BeyondarLocationManager.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BeyondarLocationManager.enable();
    }
    @Override
    public void onConnectionSuspended(int i) { }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
}
