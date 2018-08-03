package com.testexam.charlie.tlive.main.profile.modify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import com.testexam.charlie.tlive.main.profile.modify.camera.GraphicFaceTrackerFactory
import com.testexam.charlie.tlive.main.profile.modify.camera.mask.Mask
import com.testexam.charlie.tlive.main.profile.modify.camera.mask.MaskAdapter
import com.testexam.charlie.tlive.main.profile.modify.camera.ui.CameraSourcePreview
import com.testexam.charlie.tlive.main.profile.modify.camera.ui.GraphicOverlay
import kotlinx.android.synthetic.main.activity_profile_camera.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 프로필 사진을 촬영하기 위한 Activity
 *
 */
@Suppress("NAME_SHADOWING")
class ProfileCameraActivity : AppCompatActivity(), View.OnClickListener{
    companion object {
        private val TAG = "ProfileCameraActivity"
        private val GMS_CODE = 9001
        private const val CAMERA_CODE = 2
    }

    private var mCameraSource : CameraSource? = null
    private lateinit var mPreview : CameraSourcePreview
    private lateinit var mGraphicOverlay: GraphicOverlay

    private var mIsFrontFacing = true

    private lateinit var maskAdapter: MaskAdapter
    private lateinit var maskList : ArrayList<Mask>
    private lateinit var detector: FaceDetector
    private var maskPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_camera)

        mPreview = findViewById<View>(R.id.cameraPreview) as CameraSourcePreview
        mGraphicOverlay = findViewById<View>(R.id.faceOverlay) as GraphicOverlay

        setOnClickListeners()
        setMaskRecyclerView()
        val cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val writePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(cameraPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED){
            createCameraSource()
        }else{
            requestCameraPermission()
        }
    }

    /*
     * 마스크 리사이클러뷰 설정
     */
    private fun setMaskRecyclerView(){
        maskList = ArrayList()
        setMaskItems()  // 마스크 아이템 초기화
        maskAdapter = MaskAdapter(maskList, applicationContext) // 마스크 어댑터 초기화
        val layoutManager = LinearLayoutManager(applicationContext) // 레이아웃 매니저 생성
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL  // 레이아웃 매니저 방향을 가로로 설정한다.

        cameraMaskRv.layoutManager = layoutManager      // Rv 에 레이아웃 매니저 설정
        cameraMaskRv.adapter = maskAdapter              // Rv 에 마스크 어댑터 설정

        cameraMaskRv.addOnItemTouchListener(RecyclerItemClickListener(
                applicationContext, cameraMaskRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                runOnUiThread{
                    maskAdapter.setSelect(position) // 마스크 아이템의 선택 상태를 변경한다.
                }
                maskPosition = position
                changeMask()
            }
            override fun onLongItemClick(view: View?, position: Int) {} }
        ))
    }

    /*
     * 마스크 아이템 초기화
     */
    private fun setMaskItems(){
        maskList.add(Mask("none",false,true))
        maskList.add(Mask("dog",true,false))
        maskList.add(Mask("cat",true,false))
        maskList.add(Mask("iron",true,false))
        maskList.add(Mask("spider",true,false))
        maskList.add(Mask("batman",true,false))
        maskList.add(Mask("annony",true,false))
        maskList.add(Mask("submarine",true,false))
    }

    /*
     * 클릭 리스너 설정
     */
    private fun setOnClickListeners(){
        cameraCloseIv.setOnClickListener(this)
        cameraCaptureIv.setOnClickListener(this)
        cameraSwitchIv.setOnClickListener(this)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onClick(v: View?) {
        when(v){
            cameraCloseIv->onBackPressed()  // 닫기 버튼
            cameraSwitchIv->{   // 카메라 전환 버튼
                mIsFrontFacing = !mIsFrontFacing    // 카메라 방향을 저장한다.
                if(mCameraSource != null){
                    mCameraSource!!.release()       // 카메라 리소스를 해제한다.
                    mCameraSource = null
                }

                // 전환된 방향으로 새로운 카메라 리소스를 가져온다.
                createCameraSource()
                startCameraSource()
            }

            cameraCaptureIv->{  // 카메라 촬영 버튼
                val saveImageDialog = SaveImageDialog(this,"wait")   // 이미지 저장 다이얼로그 생성
                saveImageDialog.show()      // 이미지 저장 다이얼로그 보여주기
                mCameraSource!!.takePicture(null, CameraSource.PictureCallback {    // 카메라 캡쳐
                    val frameBitmap = BitmapFactory.decodeByteArray(it,0, it.size)  // 카메라 소스에서 가져온 바이트 배열을 비트맵으로 변경한다.
                    mGraphicOverlay.isDrawingCacheEnabled = true    // 그래픽 오버레이의 드로잉 캐시를 가능하게 한다.
                    mGraphicOverlay.buildDrawingCache(false)    // 그래픽 오버레이 오토 스케일을 끈다.
                    val overlayBitmap = mGraphicOverlay.getDrawingCache(true)   // 그래픽 오버레이을 비트맵으로 변경한다.

                    capturePreviewIv.setImageBitmap(frameBitmap)        // 캡쳐할 프리뷰에 이미지를 넣는다.
                    captureOverlayIv.setImageBitmap(overlayBitmap)      // 캡쳐할 오버레이뷰에 이미지를 넣는다.

                    captureFrame.isDrawingCacheEnabled = true       // 캡쳐할 프레임 레이아웃의 드로잉 캐시를 가능하게 한다.
                    captureFrame.buildDrawingCache(true)    // 캡쳐할 프레임의 오토 스케일을 킨다.
                    val captureBitmap = Bitmap.createBitmap(captureFrame.drawingCache)  // 캡쳐할 프레임에서 비트맵을 가져온다.

                    var out : FileOutputStream? = null   // 이미지 파일을 작성할 아웃풋 스트림
                    var imageUrl = ""       // 결과 인텐트로 전달할 저장된 이미지 파일의 절대 경로
                    try{
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())  // 이미지 파일의 유니크한 이름을 위해 현재 시간을 가져온다.
                        val imageFileName = "tlive_$timeStamp.png"  // 이미지 파일 이름

                        val file = File(Environment.getExternalStorageDirectory().absoluteFile,imageFileName)   // 파일 저장 경로
                        out = FileOutputStream(file)
                        imageUrl = file.absolutePath
                        captureBitmap.compress(Bitmap.CompressFormat.PNG, 50, out)     // 파일을 저장한다.
                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally {
                        try{
                            if(out != null){
                                out.close()
                            }
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    saveImageDialog.dismiss()   // 이미지 저장 다이얼로그 종료

                    val resultIntent = Intent()     // ModifyProfileActivity 로 결과 값을 전달해줄 인텐트
                    resultIntent.putExtra("imageUrl",imageUrl)  // 이미지 파일의 경로를 인텐트에 담는다.

                    setResult(Activity.RESULT_OK, resultIntent)       // 결과 코드와 결과 인텐트를 설정한다.
                    finish()    // 현재 액티비티 종료
                })

            }
        }
    }

    /*
     * 카메라 권한을 요청한다.
     */
    private fun requestCameraPermission(){
        val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)
                && !ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(this, permission, CAMERA_CODE)
            return
        }

        val thisActivity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(thisActivity, permission, CAMERA_CODE)
        }
        Snackbar.make(mGraphicOverlay, "camera permission",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("ok", listener)
                .show()
    }

    /*
     * 마스크를 변경하는 메소드.
     *
     * 디텍터 객체에 새로운 마스크 위치를 정의한다.
     */
    private fun changeMask(){
        detector.setProcessor(
                MultiProcessor.Builder<Face>(mGraphicOverlay.let { GraphicFaceTrackerFactory(it,maskPosition,applicationContext,mIsFrontFacing) })
                        .build())
    }

    /*
     * 카메라 리소스를 생성하는 메소드
     */
    private fun createCameraSource(){
        val context = applicationContext
        detector = crateFaceDetector(applicationContext)

        var facing = CameraSource.CAMERA_FACING_FRONT
        if(!mIsFrontFacing){
            facing = CameraSource.CAMERA_FACING_BACK
        }

        mCameraSource = CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640,480)
                .setFacing(facing)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build()
    }

    /*
     * 얼굴 디텍터를 생성하는 메소드
     */
    private fun crateFaceDetector(context : Context) : FaceDetector{
        val detector = FaceDetector.Builder(context)
                //.setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(mIsFrontFacing)
                .setMinFaceSize(if(mIsFrontFacing){
                    0.35f
                } else{
                    0.15f
                })
                .build()
        detector.setProcessor(
                MultiProcessor.Builder<Face>(mGraphicOverlay.let { GraphicFaceTrackerFactory(it,maskPosition,context,mIsFrontFacing) })
                        .build())
        if (!detector.isOperational) {
            Log.w(TAG, "Face detector dependencies are not yet available.")

            // Check the device's storage.  If there's little available storage, the native
            // face detection library will not be downloaded, and the app won't work,
            // so notify the user.
            val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowStorageFilter) != null

            if (hasLowStorage) {
                val listener = DialogInterface.OnClickListener { _, _ -> finish() }
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle(R.string.app_name)
                        .setMessage("error")
                        .setPositiveButton("ok", listener)
                        .show()
            }
        }
        return detector
    }

    private fun startCameraSource(){
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)
        if(code != ConnectionResult.SUCCESS){
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, GMS_CODE)
            dlg.show()
        }

        if(mCameraSource != null){
            try{
                mGraphicOverlay.let { mPreview.start(mCameraSource!!, it) }
            }catch (e: IOException){
                Log.e(TAG,"Unable to start camera source $e")
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode != CAMERA_CODE){
            Log.d(TAG, "unexpected permission result : $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            createCameraSource()
            return
        }

        Log.e(TAG, "Permission not granted ")

        val listener = DialogInterface.OnClickListener{ dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("얼굴 인식")
                .setMessage("카메라 권한이 거절되었습니다.")
                .setPositiveButton("확인",listener)
                .show()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        mPreview.stop()
        super.onPause()
    }

    override fun onDestroy() {
        if(mCameraSource != null){
            mCameraSource!!.release()
        }
        super.onDestroy()
    }
}