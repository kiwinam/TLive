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
import com.testexam.charlie.tlive.main.place.detail.ar.util.ArFragmentSupport;
import com.testexam.charlie.tlive.main.place.detail.ar.util.LocationCalc;


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
    private String TAG = "ARNavigationActivity";

    private Step steps[]; // 경로의 Step 들을 저장하는 배열
    private World world; // AR 을 그릴 world (ar 세계)

    private Location mLastLocation;
    private LocationManager locationManager;
    private FusedLocationProviderClient mLastLocationProvider;

    private GoogleApiClient mGoogleApiClient; // 구글 API 클라이언트

    private DirectionsRoute currentRoute; // Map Box 에서 경로를 가져와 저장하는 변수

    private Double myLocationLat;
    private Double myLocationLng;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_navigation);
        Mapbox.getInstance(this, getString(R.string.access_token)); // MapBox 에 access token 을 설정한다.
        //mLastLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        //startLocationUpdates();
        //getIntents();
        setGoogleApiClient(); // 구글 API 클라이언트 설정
    }

    private void setGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    /**
     * AR 설정하는 메소드
     */

    private void configureAR() {
        List<List<Point>> polylineLatLngList = new ArrayList<>();

        world = new World(getApplicationContext());


        world.setGeoPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        //world.setGeoPosition(myLocationLat, myLocationLng);
        //Timber.tag(TAG).d("Configure_AR: LOCATION" + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude());
        Log.d(TAG,"Configure_AR: LOCATION" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        //Log.d(TAG,"Configure_AR: LOCATION" + myLocationLat + "," + myLocationLng);
        world.setDefaultImage(R.drawable.ar_sphere_default);

        ArFragmentSupport arFragmentSupport = (ArFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.ar_cam_fragment);

        //GeoObject signObjects[]=new GeoObject[steps.length];

        //Timber.tag(TAG).d("Configure_AR: STEP.LENGTH:%s", steps.length);
        Log.d(TAG,"Configure_AR: STEP.LENGTH:"+ steps.length);
        //TODO The given below is for rendering MAJOR STEPS LOCATIONS

        /*
         * Steps 에 있는 주요 위치를 polyLine 으로 만든다.
         */
        for(int i=0;i<steps.length;i++){
            Step step = steps[i];
            //polylineLatLngList.add(i,PolyUtil.decode((step.getGeometry())));
            polylineLatLngList.add(i, PolylineUtils.decode(step.getGeometry(), Constants.PRECISION_6));

            Log.d("steps["+i+"] ",PolyUtil.decode((step.getGeometry())).toString());

            String modifier = step.getModifier();
            // AR 시작 지점
            if(i == 0){
                GeoObject startObject = new GeoObject(10000+i);
                startObject.setImageResource(R.drawable.start);
                startObject.setGeoPosition(step.getStartLocation().getLat(),step.getStartLocation().getLng());
                world.addBeyondarObject(startObject);
                //Timber.tag("AR Make").e("Configure_AR: START SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                Log.e("AR Make","Configure_AR: START SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");

            }else if(i==steps.length-1){ // AR 마지막 지점
                GeoObject lastObject = new GeoObject(10000+i);
                lastObject.setImageResource(R.drawable.stop);

                LatLng latLng = SphericalUtil.computeOffset(
                        new LatLng(step.getStartLocation().getLat(),step.getStartLocation().getLng()),4f,SphericalUtil.computeHeading(
                                new LatLng(step.getStartLocation().getLat(),step.getStartLocation().getLng()),
                                new LatLng(step.getStartLocation().getLat(),step.getStartLocation().getLng())
                        )
                );
                lastObject.setGeoPosition(latLng.latitude,latLng.longitude);
                world.addBeyondarObject(lastObject);
                //Timber.tag("AR Make").e("Configure_AR: LAST SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                Log.e("AR Make","Configure_AR: LAST SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
            }else{
                //
                if(modifier!=null){
                    if(modifier.equals("left")){
                        GeoObject leftObject = new GeoObject(10000+i);
                        leftObject.setImageResource(R.drawable.turn_left);
                        leftObject.setGeoPosition(step.getStartLocation().getLat(),step.getStartLocation().getLng());

                        world.addBeyondarObject(leftObject);
                        //Timber.tag("AR Make").e("Configure_AR: LEFT SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                        Log.e("AR Make","Configure_AR: LEFT SIGN:" + i + "(" + step.getStartLocation().getLat() + "," + step.getStartLocation().getLng() + ")");
                    }else if(modifier.equals("right")){
                        GeoObject rightObject = new GeoObject(10000+i);
                        rightObject.setImageResource(R.drawable.turn_right);
                        rightObject.setGeoPosition(step.getStartLocation().getLat(),step.getStartLocation().getLng());

                        world.addBeyondarObject(rightObject);
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
         * 시작, 끝 사이에 폴리라인을 따라 원을 그려준다.
         */
        int tempPolyCount = 0;
        int tempInterPolyCount = 0;

        for(int j = 0; j < polylineLatLngList.size(); j++){
            for(int k = 0; k < polylineLatLngList.get(j).size(); k++){
                Point polyPoint = polylineLatLngList.get(j).get(k);
                List<Point> polyList = polylineLatLngList.get(j);
                GeoObject polyGeoObject = new GeoObject(1000+tempPolyCount++);

                polyGeoObject.setGeoPosition(polyPoint.latitude(),polyPoint.longitude());
                polyGeoObject.setImageResource(R.drawable.ar_sphere_150x);
                polyGeoObject.setName("arObj"+j+k);

                try{
                    if(polyList.size() > k+1){
                        double dist = LocationCalc.haversine(
                                polyPoint.latitude(), polyPoint.longitude(),
                                polyList.get(k+1).latitude(), polyList.get(k+1).longitude() ) * 1000;

                        // polyObject 간의 거리 차이가 6이 넘는다면
                        if(dist > 6){
                            int arObjectCount = ((int) dist/3 ) - 1;

                            double heading = SphericalUtil.computeHeading(
                                    new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                    new LatLng(polyList.get(k+1).latitude(),polyList.get(k+1).longitude()));

                            LatLng tempLatLng = SphericalUtil.computeOffset(
                                    new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                    3f,
                                    heading );

                            // The distance to be incremented
                            double incrementDist = 3f;

                            for(int i = 0; i < arObjectCount; i++){
                                GeoObject interPolyGeoObject = new GeoObject(5000+tempInterPolyCount++);

                                if( i > 0 && k < polylineLatLngList.size()){
                                    incrementDist += 3f;

                                    tempLatLng = SphericalUtil.computeOffset(
                                            new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                            incrementDist,
                                            SphericalUtil.computeHeading(
                                                    new LatLng(polyPoint.latitude(), polyPoint.longitude()),
                                                    new LatLng(polyList.get(k+1).latitude(), polyList.get(k+1).longitude())
                                            )
                                    );
                                }

                                // 이미지 이름 설정
                                interPolyGeoObject.setGeoPosition(tempLatLng.latitude, tempLatLng.longitude);
                                interPolyGeoObject.setImageResource(R.drawable.ar_sphere_default_125x);
                                interPolyGeoObject.setName("inter_arObj"+j+k+i);

                                world.addBeyondarObject(interPolyGeoObject);
                                //Timber.tag("AR Make").e("Configure_AR: INTER SIGN:" + i + "(" + interPolyGeoObject.getLatitude() + "," + interPolyGeoObject.getLongitude() + ")");
                                Log.e("AR Make","Configure_AR: INTER SIGN:" + i + "(" + interPolyGeoObject.getLatitude() + "," + interPolyGeoObject.getLongitude() + ")");
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                world.addBeyondarObject(polyGeoObject);
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

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(startPoint)
                .destination(endPoint)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Timber.tag(TAG).d("Response code: %s", response.code());
                        if (response.body() == null) {
                            Timber.tag(TAG).e("No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Timber.tag(TAG).e("No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        //System.out.println(currentRoute.toString());

                        //Timber.tag("duration").e(currentRoute.legs().get(0).duration().toString());
                        Log.e("duration",currentRoute.legs().get(0).duration().toString());
                        //Timber.tag("distance").e(currentRoute.legs().get(0).distance().toString());
                        Log.e("distance",currentRoute.legs().get(0).distance().toString());
                        //Timber.tag("steps size").e(String.valueOf(currentRoute.legs().get(0).steps().size()));
                        Log.e("steps size",String.valueOf(currentRoute.legs().get(0).steps().size()));

                        int stepSize = currentRoute.legs().get(0).steps().size();
                        steps=new Step[stepSize];

                        for(int i=0;i<stepSize;i++) {
                            LegStep step = currentRoute.legs().get(0).steps().get(i);
                            System.out.println(step.toString());

                            StartLocation startLocation = new StartLocation(step.maneuver().location().latitude(),step.maneuver().location().longitude());
                            EndLocation endLocation = new EndLocation(step.intersections().get(0).location().latitude(),step.intersections().get(0).location().longitude());
                            String geometry = step.geometry();
                            String type = step.maneuver().type();
                            String modifier = step.maneuver().modifier();


                            steps[i] = new Step(startLocation,endLocation,geometry,type,modifier);

                            /*Log.d(TAG,"onResponse: STEP " + i + ": " + steps[i].getStartLocation().getLat() + "/" + steps[i].getStartLocation().getLng() + ">>"
                                    + steps[i].getEndLocation().getLat() + "/" + steps[i].getEndLocation().getLng());
                            Log.d("geometry",i+">"+geometry+"..");*/
                        }
                        configureAR();
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.tag(TAG).e("Error: %s", throwable.getMessage());
                    }
                });


    }


    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

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
    public void onConnected(@android.support.annotation.Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            String locationProvider = LocationManager.NETWORK_PROVIDER;
            //mLastLocationProvider = locationManager.getLastKnownLocation(locationProvider);

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient); // Deprecated
            //mLastLocationProvider = LocationServices.getFusedLocationProviderClient(this);

            try {
                getIntents(); //Fetch Intent Values
            }catch (Exception e){
                e.printStackTrace();
                Timber.tag(TAG).d("onCreate: Intent Error");
            }
        }
        startLocationUpdates();
    }

    private void getIntents() {
        myLocationLat = getIntent().getDoubleExtra("startLat",0.0);
        myLocationLng = getIntent().getDoubleExtra("startLng",0.0);
        com.mapbox.mapboxsdk.geometry.LatLng startLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(myLocationLat, myLocationLng);
        com.mapbox.mapboxsdk.geometry.LatLng endLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(getIntent().getDoubleExtra("endLat",0.0),getIntent().getDoubleExtra("endLng",0.0));


        //Point startPoint = Point.fromLngLat(126.970727,37.484987);
        //Point endPoint = Point.fromLngLat(126.971575,37.482927 );

        Point startPoint = Point.fromLngLat(startLatLng.getLongitude(),startLatLng.getLatitude());
        Point endPoint = Point.fromLngLat(endLatLng.getLongitude(),endLatLng.getLatitude());


        getRoute(startPoint, endPoint);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("location Change",location.getLatitude()+","+location.getLongitude());
        if(world!=null) {
            world.setGeoPosition(location.getLatitude(), location.getLongitude());
            Log.d("world location",world.getLatitude()+","+world.getLongitude());
        }
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);
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

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }
}
