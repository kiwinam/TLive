package com.testexam.charlie.tlive.main.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity

/**
 *
 */
class ProfileActivity : BaseActivity() , View.OnClickListener{

    private var userName = ""
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)

        userName = sp.getString("name", "")
        userEmail = sp.getString("email","")


    }

    override fun onClick(v: View?) {

    }
}