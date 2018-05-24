package com.testexam.charlie.tlive.common

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * 모든 액티비티에서 상속받는 BaseActivity
 * 공통적으로 사용하는 기능을 모아놓음.
 *
 * Created by charlie on 2018. 5. 22
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}