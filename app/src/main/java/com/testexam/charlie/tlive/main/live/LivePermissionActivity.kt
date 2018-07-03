package com.testexam.charlie.tlive.main.live

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity
import kotlinx.android.synthetic.main.activity_live_permission.*

@Suppress("PrivatePropertyName")
/**
 * 라이브 방송 전 필요한 권한을 확인하는 Activity
 * Created by charlie on 2018. 5. 25
 */
class LivePermissionActivity : BaseActivity() , View.OnClickListener {
    private val CAMERA_REQUEST_CODE = 1000  // 카메라 권한 요청 코드
    private val MIC_REQUEST_CODE = 2000     // 마이크 권한 요청 코드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_permission) // Activity - Layout Connection

        setupPermissionBtn() // 현재 허용된 권한에 따라 버튼을 숨김
        setOnClickListeners() // 클릭 리스너 설정
    }

    override fun onClick(v: View?) {
        when(v){
            livePerCloseIv -> onBackPressed()   // X 를 눌렀을 때 현재 Activity 를 종료한다.

            // 카메라 권한 허용 버튼을 누르면 카메라 권한을 요청한다.
            livePerCameraBtn->{ ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),CAMERA_REQUEST_CODE) }
            // 마이크 권한 허용 버튼을 누르면 마이크 권한을 요청한다.
            livePerMicBtn->{ ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),MIC_REQUEST_CODE) }

            // 방송 시작 버튼을 누르면 BroadCasterActivity 로 이동한다.
            livePerStartBtn->{
                startActivity(Intent(applicationContext, BroadCasterActivity::class.java))
                finish()
            }
        }
    }

    /* 뷰들의 클릭 리스너를 OnClickListener 에서 오버라이딩한 onClick 에 연결한다. */
    private fun setOnClickListeners(){
        livePerCloseIv.setOnClickListener(this)
        livePerCameraBtn.setOnClickListener(this)
        livePerMicBtn.setOnClickListener(this)
        livePerStartBtn.setOnClickListener(this)
    }

    /*
     * 권한 요청의 결과가 넘어오는 메소드
     *
     * 방송을 시작하는데 필요한 권한이 충족하는지 확인한다.
     * 카메라 권한이 승인 되었으면 카메라 버튼을 없앤다.
     * 마이크 권한이 승인 되었으면 마이크 버튼을 없앤다.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if( !grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){   // 권한 요청이 승인되었다면
            when(requestCode){
                CAMERA_REQUEST_CODE->{  // 승인된 권한이 카메라라면
                    livePerCameraBtn.visibility = View.INVISIBLE    // 카메라 권한 요청 버튼을 숨긴다.
                    livePerOKCameraTv.visibility = View.VISIBLE     // 카메라 권한이 있다는 텍스트를 표시한다.
                }
                MIC_REQUEST_CODE->{ // 승인된 권한이 마이크라면
                    livePerMicBtn.visibility = View.INVISIBLE       // 마이크 권한 요청 버튼을 숨긴다.
                    livePerOKMicTv.visibility = View.VISIBLE        // 마이크 권한이 있다는 텍스트를 표시한다.
                }
            }
            checkPermissionStatus() // 방송에 필요한 권한이 모두 승인 되었는지 확인하는 메소드를 호출한다.
        }
    }

    /*
     * Activity 가 시작될 때 어플리케이션에서 허용된 권한을 확인하고, 이미 승인된 권한의 버튼은 숨김
     */
    private fun setupPermissionBtn(){
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)   // 카메라 퍼미션
        val micPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)    // 마이크 퍼미션

        // 카메라 권한이 승인 되었다면
        if(cameraPermission == PackageManager.PERMISSION_GRANTED){
            livePerCameraBtn.visibility = View.INVISIBLE    // 카메라 권한 요청 버튼을 숨긴다.
            livePerOKCameraTv.visibility = View.VISIBLE     // 카메라 권한이 있다는 텍스트를 표시한다.
        }

        // 마이크 권한이 승인 되었다면
        if(micPermission == PackageManager.PERMISSION_GRANTED){
            livePerMicBtn.visibility = View.INVISIBLE       // 마이크 권한 요청 버튼을 숨긴다.
            livePerOKMicTv.visibility = View.VISIBLE        // 마이크 권한이 있다는 텍스트를 표시한다.
        }

        // 모든 권한이 승인되었다면
        if(micPermission == PackageManager.PERMISSION_GRANTED && micPermission == PackageManager.PERMISSION_GRANTED){
            livePerMicBtn.visibility = View.INVISIBLE       // 카메라 권한 요청 버튼을 숨긴다.
            livePerOKMicTv.visibility = View.INVISIBLE      // 카메라 권한이 있다는 텍스트를 표시한다.
            livePerOKCameraTv.visibility = View.INVISIBLE   // 마이크 권한 요청 버튼을 숨긴다.
            livePerOKMicTv.visibility = View.INVISIBLE      // 마이크 권한이 있다는 텍스트를 표시한다.
        }
    }

    /*
     * 방송에 필요한 권한이 모두 승인 되었는지 확인하는 메소드
     *
     * 방송에 필요한 권한인 카메라와 마이크 권한이 승인되었는지 확인한다.
     * 필요한 권한이 모두 승인되었다면 방송 시작 버튼을 보여준다.
     */
    private fun checkPermissionStatus(){
        val micPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)    // 카메라 권한을 가져온다.
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)       // 마이크 권한을 가져온다.
        // 카메라와 마이크 권한이 모두 승인 되었다면
        if(micPermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED){
            livePerMicBtn.visibility = View.GONE    // 마이크 버튼을 숨긴다
            livePerCameraBtn.visibility = View.GONE // 카메라 버튼을 숨긴다
            livePerOKCameraTv.visibility = View.INVISIBLE   // 카메라 권한이 있다는 텍스트를 숨긴다.
            livePerOKMicTv.visibility = View.INVISIBLE      // 마이크 권한이 있다는 텍스트를 숨긴다.

            livePerInfoTv.text = getString(R.string.ready_broadcast_ko) // 권한 상태를 표시하는 텍스트뷰에 권한이 준비 되었다는 메시지를 표시한다.

            livePerStartBtn.visibility = View.VISIBLE   // 방송 시작 버튼을 보여준다.
        }
    }
}