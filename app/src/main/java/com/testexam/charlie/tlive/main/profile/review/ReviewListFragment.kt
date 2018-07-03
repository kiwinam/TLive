package com.testexam.charlie.tlive.main.profile.review

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R

/**
 * 내가 올린 리뷰를 보여주는 Fragment
 */
class ReviewListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_review_list, null)
        return view
    }
}