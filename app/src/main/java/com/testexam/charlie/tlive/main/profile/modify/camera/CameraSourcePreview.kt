package com.testexam.charlie.tlive.main.profile.modify.camera

import android.Manifest
import android.annotation.SuppressLint
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

class CameraSourcePreview(private val mContext : Context, attrs : AttributeSet) : ViewGroup(mContext,attrs) {
    private lateinit var mSurfaceView : SurfaceView
    private var mStartRequested = false
    private var mSurfaceAvailable = false
    private var mCameraSource : CameraSource? = null
    private var mOverlay : GraphicOverlay? = null

    private val isPortraitMode : Boolean
        get(){
            val orientation = mContext.resources.configuration.orientation
            if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                return false
            }
            if(orientation == Configuration.ORIENTATION_PORTRAIT){
                return true
            }
            Log.d("Camera preview","return default")
            return false
        }

        init{
            mStartRequested = false
            mSurfaceAvailable = false
            mSurfaceView = SurfaceView(mContext)
            mSurfaceView.holder.addCallback(SurfaceCallback())
            addView(mSurfaceView)
        }

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

    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun startIfReady(){
        if(mStartRequested && mSurfaceAvailable){   // 시작 요청이 있었고 서페이스 뷰가 사용 가능하면
            if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                return
            }
            mCameraSource!!.start(mSurfaceView.holder)
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

    private inner class SurfaceCallback : SurfaceHolder.Callback{
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
            childHeight = layoutHeight
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