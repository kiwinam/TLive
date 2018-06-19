package com.testexam.charlie.tlive.main.place.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class PositionItem(no : Int,name : String,lat : Double, lon : Double) : ClusterItem{

    private final var position : LatLng? = null

    private var number : Int = 0
    private var placeName : String = ""
    init {
        position = LatLng(lat,lon)
        number = no
        placeName = name
    }

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return position!!
    }

    fun getNo() : Int{
        return number
    }

    fun getName() : String {
        return placeName
    }
}