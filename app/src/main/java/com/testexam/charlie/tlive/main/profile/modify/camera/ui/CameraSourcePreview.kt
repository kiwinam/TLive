package com.testexam.charlie.tlive.main.profile.modify.camera.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.support.v4.app.ActivityCompat
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.google.android.gms.vision.CameraSource
import java.io.IOException

/**
 * 모바일 비전에서 디바이스의 카메라를 받아온 카메라 데이터를 SurfaceView 에 보여준다.
 */
class CameraSourcePreview(private val mContext : Context, attrs : AttributeSet) : ViewGroup(mContext,attrs) {
    private var mSurfaceView : SurfaceView      // 카메라 데이터를 뿌릴 SurfaceView
    private var mStartRequested = false         // 시작 요청이 왔는지 확인하는 변수
    private var mSurfaceAvailable = false       // 서페이스 뷰가 사용가능한지 확인하는 변수
    private var mCameraSource : CameraSource? = null        // 카메라에서 데이터를 받아오는 소스 객체
    private var mOverlay : GraphicOverlay? = null       // 얼굴 인식 후 마스크를 띄울 오버레이 객체
    private val isPortraitMode : Boolean        // 디바이스의 각도가 세로인지 가로인지 확인하는 변수
        get(){
            val orientation = mContext.resources.configuration.orientation      // 현재 디바이스의 방향을 가져온다.
            if(orientation == Configuration.ORIENTATION_LANDSCAPE){ // 방향이 가로라면 false 를 리턴한다.
                return false
            }
            if(orientation == Configuration.ORIENTATION_PORTRAIT){  // 방향이 세로라면 true 를 리턴한다.
                return true
            }
            return false
        }

        // 카메라 프리뷰 초기화
        init{
            mStartRequested = false
            mSurfaceAvailable = false
            mSurfaceView = SurfaceView(mContext)
            mSurfaceView.holder.addCallback(SurfaceCallback())
            addView(mSurfaceView)       // 서페이스 뷰에 카메라 뷰를 추가한다.
        }

    /*
     * 카메라 프리뷰를 시작한다.
     *
     * 카메라에서 데이터를 받아와 SurfaceView 에 뿌려준다.
     */
    @Throws(IOException::class)
    fun start(cameraSource : CameraSource?){
        if(cameraSource == null){
            stop()
        }
        mCameraSource = cameraSource
        if(mCameraSource != null){
            mStartRequested = true
            startIfReady()
        }
    }

    /*
     * 오버레이를 할 경우 시작하는 메소드
     */
    @Throws(IOException::class)
    fun start(cameraSource: CameraSource, overlay: GraphicOverlay){
        mOverlay = overlay
        start(cameraSource)
    }

    /*
     * 카메라 소스 받아오는 것을 중지한다.
     */
    fun stop(){
        if(mCameraSource != null)
            mCameraSource!!.stop()
    }

    /*
     * 카메라 소스를 풀어주는 메소드
     */
    fun release(){
        if(mCameraSource != null){
            mCameraSource!!.release()
            mCameraSource = null
        }
    }

    /*
     * 서페이스 뷰가 준비 되었다면 카메라 프리뷰를 실행한다.
     */
    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun startIfReady(){
        if(mStartRequested && mSurfaceAvailable){   // 시작 요청이 있었고 서페이스 뷰가 사용 가능하면
            // 권한 요청이 승인 되지 않았다면 메소드를 종료한다.
            if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                return
            }
            mCameraSource!!.start(mSurfaceView.holder)  // 카메라 소스를 받아온다.
            if(mOverlay != null){
                val size = mCameraSource!!.previewSize
                val min = Math.min(size.width, size.height)
                val max = Math.max(size.width, size.height)
                if(isPortraitMode){     // 세로 모드일 경우
                    mOverlay!!.setCameraInfo(min, max, mCameraSource!!.cameraFacing)
                }else{      // 가로 모드일 경우
                    mOverlay!!.setCameraInfo(max, min, mCameraSource!!.cameraFacing)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    /*
     * 서페이스 콜백 클래스
     *
     * 서페이스가 정상적으로 생성 되었으면 mSurfaceAvailable 를 true 로 변경한다.
     * 서페이스 뷰가 사라지면 mSurfaceAvailable 를 false 로 변경한다.
     */
    private inner class SurfaceCallback : SurfaceHolder.Callback{
        @SuppressLint("LogNotTimber")
        override fun surfaceCreated(holder: SurfaceHolder?) {
            mSurfaceAvailable = true
            try{
                startIfReady()
            }catch (e: IOException){
                Log.e("SurfaceCallback","create fail ${e.printStackTrace()}")
            }
        }
        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mSurfaceAvailable = false
        }
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
    }

    @SuppressLint("LogNotTimber")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var width = 320
        var height = 240
        if(mCameraSource != null){
            val size = mCameraSource!!.previewSize
            if(size != null){
                width = size.width
                height = size.height
            }
        }

        if(isPortraitMode){
            val tmp = width
            width = height
            height = tmp
        }

        val layoutWidth = right - left
        val layoutHeight = bottom - top
        var childWidth = layoutWidth
        var childHeight = (layoutWidth.toFloat() / width.toFloat() * height).toInt()

        if(childHeight > layoutHeight){
            // childHeight = layoutHeight
            childHeight = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
        }

        for(i in 0 until childCount){
            getChildAt(i).layout(0,0 , childWidth, childHeight)
        }

        try{
            startIfReady()
        }catch (e:IOException){
            Log.e("onLayout","Camera source fail ${e.printStackTrace()}")
        }
    }
}