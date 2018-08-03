package com.testexam.charlie.tlive.main.profile.modify

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.testexam.charlie.tlive.R

class SaveImageDialog(context: Context?, name : String) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_wait)

    }
}