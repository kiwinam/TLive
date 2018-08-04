package com.testexam.charlie.tlive.main.profile.modify

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.testexam.charlie.tlive.R

/**
 * 마스크 씌운 프로필 이미지를 저장하는 동안 프로그레스 바를 보여주는 다이얼로그
 */
class SaveImageDialog(context: Context?, name : String) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_wait)

    }
}