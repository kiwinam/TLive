package com.testexam.charlie.tlive.main.place.detail.pathFinder

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.maps.model.LatLng
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.detail.ar.ARNavigationActivity


class SelectPathFinder : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var selectPathMap : LinearLayout
    private lateinit var selectPathAr : LinearLayout

    private lateinit var startLatLng: LatLng
    private lateinit var endLatLng: LatLng
    lateinit var intent : Intent

    companion object {
        fun newInstance(): SelectPathFinder = SelectPathFinder()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.bs_select_path_finder, container, false)
        selectPathMap = v.findViewById(R.id.selectPathMap)
        selectPathAr = v.findViewById(R.id.selectPathAr)

        selectPathMap.setOnClickListener(this)
        selectPathAr.setOnClickListener(this)

        return v
    }

    /*
     *
     */
    override fun onClick(v: View?) {
        when(v){

            selectPathMap->{
                val tMapIntent = Intent(context, TMapPathFinderActivity::class.java)
                tMapIntent.putExtra("startLatLng",startLatLng)
                tMapIntent.putExtra("endLatLng",endLatLng)
                startActivity(tMapIntent)
            }
            selectPathAr-> {
                val arIntent = Intent(context, ARNavigationActivity::class.java)
                arIntent.putExtra("startLat", startLatLng.latitude)
                arIntent.putExtra("startLng", startLatLng.longitude)
                arIntent.putExtra("endLat", endLatLng.latitude)
                arIntent.putExtra("endLng", endLatLng.longitude)
                startActivity(arIntent)
            }
        }
        dismiss()
    }

    /*
     *
     */
    fun setLatLng(myLatLng: LatLng, placeLatLng: LatLng){
        startLatLng = myLatLng
        endLatLng = placeLatLng
    }
}