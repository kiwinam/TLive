package com.testexam.charlie.tlive.main.place.map

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.Place

class MapPlaceAdapter(private var placeList : ArrayList<Place>, val context : Context) : RecyclerView.Adapter<MapPlaceAdapter.MapPlaceViewHolder>(){
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPlaceViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_map_place,parent,false)
        return MapPlaceViewHolder(v)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MapPlaceViewHolder, position: Int) {
        val place = placeList[position]


        if(place.previewSrc != ""){
            Glide.with(context)
                    .load(reviewImgUrl+place.previewSrc)
                    .into(holder.mapPlacePreviewIv)
        }
        holder.mapPlaceNameTv.text = (place.no).toString()+". "+place.name
        holder.mapPlaceStationTv.text = place.nearStation
        holder.mapPlaceViewNumTv.text = place.viewNum.toString()
        holder.mapPlaceReviewNumTv.text = place.reviewNum.toString()
        holder.mapPlaceStarScoreTv.text = place.starScore.toString()
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    fun setData(list : ArrayList<Place>){
        placeList = list
        notifyDataSetChanged()
    }

    class MapPlaceViewHolder (holder : View) : RecyclerView.ViewHolder(holder){
        val mapPlacePreviewIv = holder.findViewById<ImageView>(R.id.mapPlacePreviewIv)!!

        val mapPlaceNameTv = holder.findViewById<TextView>(R.id.mapPlaceNameTv)!!
        val mapPlaceStationTv = holder.findViewById<TextView>(R.id.mapPlaceStationTv)!!
        val mapPlaceViewNumTv = holder.findViewById<TextView>(R.id.mapPlaceViewNumTv)!!
        val mapPlaceReviewNumTv = holder.findViewById<TextView>(R.id.mapPlaceReviewNumTv)!!
        val mapPlaceStarScoreTv = holder.findViewById<TextView>(R.id.mapPlaceStarScoreTv)!!
    }
}