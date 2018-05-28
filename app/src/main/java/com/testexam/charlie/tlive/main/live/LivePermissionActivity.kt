package com.testexam.charlie.tlive.main.live

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import kotlinx.android.synthetic.main.activity_live_permission.*
import kotlinx.android.synthetic.main.activity_live_permission.view.*

/**
 * 라이브 방송 전 필요한 권한을 확인하는 Activity
 * Created by charlie on 2018. 5. 25
 */
class LivePermissionActivity : BaseActivity() , View.OnClickListener {
    private val CAMERA_REQUEST_CODE = 1000
    private val MIC_REQUEST_CODE = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_permission) // Activity - Layout Connection

        setupPermissionBtn() // 현재 허용된 권한에 따라 버튼을 숨김

        setOnClickListeners() // 클릭 리스너 설정
    }

    override fun onClick(v: View?) {
        when(v){
            // X 를 눌렀을 때 현재 Activity 를 종료한다.
            livePerCloseIv -> onBackPressed()

            livePerCameraBtn->{
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),CAMERA_REQUEST_CODE)
            }

            livePerMicBtn->{
                ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),MIC_REQUEST_CODE)
            }
        }
    }

    private fun setOnClickListeners(){
        livePerCloseIv.setOnClickListener(this)
        livePerCameraBtn.setOnClickListener(this)
        livePerMicBtn.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if( !grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            when(requestCode){
                CAMERA_REQUEST_CODE->{
                    livePerCameraBtn.visibility = View.INVISIBLE
                    livePerOKCameraTv.visibility = View.VISIBLE
                }
                MIC_REQUEST_CODE->{
                    livePerMicBtn.visibility = View.INVISIBLE
                    livePerOKMicTv.visibility = View.VISIBLE
                }
            }
            checkPermissionStatus()
        }
    }

    /**
     * Activity 가 시작될 때 어플리케이션에서 허용된 권한을 확인하고, 이미 승인된 권한의 버튼은 숨김
     */
    private fun setupPermissionBtn(){
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        val micPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)

        if(cameraPermission == PackageManager.PERMISSION_GRANTED){
            livePerCameraBtn.visibility = View.INVISIBLE
            livePerOKCameraTv.visibility = View.VISIBLE
        }
        if(micPermission == PackageManager.PERMISSION_GRANTED){
            livePerMicBtn.visibility = View.INVISIBLE
            livePerOKMicTv.visibility = View.VISIBLE
        }

    }

    private fun checkPermissionStatus(){
        val micPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
        val cameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
        if(micPermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED){
            livePerMicBtn.visibility = View.GONE
            livePerCameraBtn.visibility = View.GONE
            livePerStartBtn.visibility = View.VISIBLE
            livePerInfoTv.text = getString(R.string.ready_broadcast_ko)
        }
    }

}