package com.testexam.charlie.tlive.main.profile.modify

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.retrofit.ConnectionList
import com.testexam.charlie.tlive.retrofit.RetrofitConn
import com.testexam.charlie.tlive.retrofit.SimpleResponse
import kotlinx.android.synthetic.main.activity_modify_profile.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

/**
 * 현재 로그인한 사용자의 개인 정보를 변경 할 수 있는 액티비티
 */
class ModifyProfileActivity : AppCompatActivity() , View.OnClickListener{
    companion object {
        private const val CAMERA_REQUEST = 5005
    }

    private var email = ""      // 유저의 이메일
    private var name = ""       // 유저의 이름
    private var profileImagePath = ""   // 이미지 파일 경로

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_profile)

        getIntentData()
        setClickListeners() // 클릭 리스너 설정
    }

    private fun getIntentData(){
        email  = intent.getStringExtra("email")
        name = intent.getStringExtra("name")
        modifyNameEt.setText(name)
        modifyEmailTv.text = email
    }

    private fun setClickListeners(){
        modifyCloseIv.setOnClickListener(this)
        modifyConfirmIv.setOnClickListener(this)
        modifyProfileIv.setOnClickListener(this)
    }



    override fun onClick(v: View?) {
        when(v){
            modifyCloseIv->onBackPressed()  // 닫기 버튼을 누르면 액티비티를 종료한다.
            modifyProfileIv->{  // 프로필 사진을 누르면 프로필 사진을 변경할 수 있게한다.
                val profileIntent = Intent(applicationContext, ProfileCameraActivity::class.java)

                startActivityForResult(profileIntent, CAMERA_REQUEST)
            }
            modifyConfirmIv->{  // 확인 버튼을 누르면 변경된 사항을 저장한다.
                updateProfile()
            }
        }
    }

    /*
     * 서버에 프로필 정보 변경을 요청하는 메소드
     */
    private fun updateProfile(){
        val paramMap = HashMap<String, RequestBody>()   // 파라미터를 담을 해쉬맵 생성

        paramMap["email"] = RequestBody.create(MediaType.parse("multipart/form-data"),email)    // 파라미터에 이메일 추가
        paramMap["name"] = RequestBody.create(MediaType.parse("multipart/form-data"),modifyNameEt.text.toString())    // 파라미터에 이름 추가

        try{
            val file = File(profileImagePath)
            val img = RequestBody.create(MediaType.parse("image/*"),file)
            paramMap["img\"; filename=\"profile_$email.png\""] = img // 파라미터에 이미지 파일 추가
        }catch (e:Exception){
            e.printStackTrace()
        }

        val conn = RetrofitConn.getRetrofit().create(ConnectionList::class.java) // 레트로핏 커넥션 초기화
        val call = conn.updateProfile(paramMap)

        call.enqueue(object : retrofit2.Callback<SimpleResponse>{
            override fun onResponse(call: Call<SimpleResponse>?, response: Response<SimpleResponse>?) {

            }

            override fun onFailure(call: Call<SimpleResponse>?, t: Throwable?) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                profileImagePath = data!!.getStringExtra("imageUrl")
                val imageFile = File(profileImagePath)
                if(imageFile.exists()){
                    val imageBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    modifyProfileIv.setImageBitmap(imageBitmap)
                }
            }
        }
    }
}