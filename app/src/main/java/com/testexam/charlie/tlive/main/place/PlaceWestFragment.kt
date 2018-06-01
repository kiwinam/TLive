package com.testexam.charlie.tlive.main.place

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R

/**
 * Created by charlie on 2018. 5. 24..
 */
class PlaceWestFragment : Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_west,container,false)
        //return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        fun newInstance(): PlaceWestFragment = PlaceWestFragment()
    }
}