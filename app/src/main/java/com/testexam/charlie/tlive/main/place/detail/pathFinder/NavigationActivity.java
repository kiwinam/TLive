package com.testexam.charlie.tlive.main.place.detail.pathFinder;
import java.util.List;
import java.util.Objects;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.api.directions.v5.models.LegStep;

import com.mapbox.core.constants.Constants;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

// classes needed to add location layer
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import android.location.Location;

import com.mapbox.mapboxsdk.geometry.LatLng;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

// 맵 박스 폴리라인 디코딩
import com.mapbox.geojson.utils.PolylineUtils;

// classes needed to add a marker

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.util.Log;

// classes needed to launch navigation UI
import android.widget.Button;

import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;


// classes needed to add location layer


// classes needed to add a marker


// classes to calculate a route


// classes needed to launch navigation UI

import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.main.place.detail.ar.model.EndLocation;
import com.testexam.charlie.tlive.main.place.detail.ar.model.StartLocation;
import com.testexam.charlie.tlive.main.place.detail.ar.model.Step;

/**
 * Map Box 를 이용하여 자동차 네비게이션을 구현하는 액티비티
 *
 * 액티비티를 시작하면 맵에 경로를 표시해준다.
 * 네비게이션 시작 버튼을 누르게되면 자동차 네비게이션을 시작한다.
 */
@SuppressWarnings("ConstantConditions")
public class NavigationActivity extends AppCompatActivity implements LocationEngineListener, PermissionsListener {
    private static final String TAG = "DirectionsActivity"; // 로그를 위한 태그
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private Step steps[];   // 경로의 상세 정보를 표시하는 스텝 배열

    private MapView mapView;    // 지도를 표시할 맵 뷰

    // variables for adding location layer
    private MapboxMap map;      // 맵 박스 객체
    private PermissionsManager permissionsManager;  // 퍼미션 매니저
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;


    // variables for adding a marker
    //private Marker destinationMarker;
    private LatLng originCoord;
    //private LatLng destinationCoord;


    // variables for calculating and drawing a route
    //private Point originPosition;
    //private Point destinationPosition;
    // 경로를 그리기 위한 변수들
    private DirectionsRoute currentRoute;   // 현재 길찾기 경로를 저장하고 있는 변수
    private NavigationMapRoute navigationMapRoute;  // 네비게이션 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_navigations);
        mapView = findViewById(R.id.mapView);   // 맵을 그릴 mapView
        mapView.onCreate(savedInstanceState);
        Button startButton = findViewById(R.id.startButton);    // 네비게이션 시작 버튼
        startButton.setOnClickListener(v -> {   // 네비게이션 시작 버튼을 누를 때를 감지하는 리스너
            Timber.tag("startButton").d("press");
            boolean simulateRoute = true;   // 자동차가 자동으로 움직이게해주는 변수, false 를 하면 내가 직접 움직여야한다.
            NavigationLauncherOptions options = NavigationLauncherOptions.builder() // 네비게이션 런쳐를 초기화한다.
                    .directionsRoute(currentRoute)  // 네비게이션 경로 설정
                    .shouldSimulateRoute(simulateRoute) // 시뮬레이션 설정
                    .build();
            NavigationLauncher.startNavigation(NavigationActivity.this, options);   // 네비게이션 액티비티를 시작한다.
        });

        // 인텐트에서 출발 지점의 위경도를 가져온다.
        LatLng startLatLng = new LatLng(getIntent().getDoubleExtra("startLat",0.0),getIntent().getDoubleExtra("startLng",0.0));
        // 인텐트에서 도착 지점의 위경도를 가져온다.
        LatLng endLatLng = new LatLng(getIntent().getDoubleExtra("endLat",0.0),getIntent().getDoubleExtra("endLng",0.0));

        // 맵 박스 Point 로 출발 지점과 도착 지점의 포인트를 만든다.
        Point startPoint = Point.fromLngLat(startLatLng.getLongitude(),startLatLng.getLatitude());
        Point endPoint = Point.fromLngLat(endLatLng.getLongitude(),endLatLng.getLatitude());

        getRoute(startPoint, endPoint); // 맵박스 api 에  길찾기 요청을한다.

        mapView.getMapAsync(mapboxMap -> {
            map = mapboxMap;
            enableLocationPlugin();
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng,15.0f)); // 현재 위치로 카메라를 이동한다.
            originCoord = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
        });
    }

    /*
     * Map Box API 에 경로를 요청하는 메소드
     */
    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin) // 출발지를 설정한다.
                .destination(destination)   // 도착지를 설정한다.
                .build()
                .getRoute(new Callback<DirectionsResponse>() {  // 경로 찾기를 요청한다.
                    @Override
                    public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
                        Timber.tag(TAG).d("Response code: %s", response.code());
                        if (response.body() == null) {
                            Timber.tag(TAG).e("경로를 찾을수 없다. 엑세스 토큰 확인이 필요함");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Timber.tag(TAG).e("경로가 없다.");
                            return;
                        }

                        currentRoute = response.body().routes().get(0); // 결과 경로를 저장한다.
                        //System.out.println(currentRoute.toString());
                        int stepSize = currentRoute.legs().get(0).steps().size();   // 경로의 스텝의 개수
                        steps=new Step[stepSize];   // steps 를 초기화한다.

                        for(int i=0;i<stepSize;i++) {
                            LegStep step = Objects.requireNonNull(Objects.requireNonNull(currentRoute.legs()).get(0).steps()).get(i);
                            StartLocation startLocation = new StartLocation(step.maneuver().location().latitude(),step.maneuver().location().longitude());
                            EndLocation endLocation = new EndLocation(step.intersections().get(0).location().latitude(),step.intersections().get(0).location().longitude());
                            String geometry = step.geometry();
                            String type = step.maneuver().type();
                            String modifier = step.maneuver().modifier();
                            // 스텝 객체를 초기화한다.
                            steps[i] = new Step(startLocation,endLocation,geometry,type,modifier);
                        }

                        // 경로를 맵에 그린다.
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();   // 이미 경로가 있다면 지운다.
                        } else {
                            // 경로 객체를 초기화한다.
                            navigationMapRoute = new NavigationMapRoute(null, mapView, map, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);  // 경로를 추가한다.
                    }

                    @Override
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
                        Timber.tag(TAG).e("Error: %s", throwable.getMessage());
                    }
                });
    }

    /*
     * 맵박스 api 에서 위치 요청을 승인하는 메소드
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
            locationPlugin.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /*
     * 맵박스 위치 엔진을 초기화한다.
     */
    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    /* 카메라의 위치를 이동한다. */
    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }

    /* 권한 요청 결과 */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) { }

    /* 위치 권한의 결과 */
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {  // 승인 되었다면
            enableLocationPlugin(); // 위치 플러그인을 사용한다.
        } else {
            finish();   // 액티비티를 종료한다.
        }
    }

    /* 맵에 연결되면 위치 갱신을 요청한다. */
    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    /* 위치가 변경된다면 카메라를 이동한다. */
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener(this);
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();    // 위치 업데이트를 요청한다.
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();   // 위치 플러그인을 실행한다.
        }
        mapView.onStart();  // 맵뷰를 시작한다.
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();     // 위치 업데이트를 중지한다.
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();    // 위치 플러그인을 중지한다.
        }
        mapView.onStop();   // 맵뷰를 중지한다.
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();    // 맵뷰를 릴리스한다.
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
