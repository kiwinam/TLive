package com.testexam.charlie.tlive.main.place


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

class PlaceAdapter (private var placeList : ArrayList<Place>, val context : Context) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>(){
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place,parent,false)
        return PlaceHolder(v)
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val place = placeList[position]

        if(place.previewSrc != null){
            Glide.with(context)
                    .load(reviewImgUrl+place.previewSrc)
                    .into(holder.placePreviewIv)
        }
        holder.placeNameTv.text = place.name
        holder.placeStationTv.text = place.nearStation+" - "
        holder.placeDistanceTv.text = place.distance.toString()+"m"
        holder.placeViewerTv.text = place.viewNum.toString()
        holder.placeReviewTv.text = place.reviewNum.toString()
        holder.placeStarScoreTv.text = place.starScore.toString()
    }

    fun setData(list : ArrayList<Place>){
        placeList = list
        notifyDataSetChanged()
    }

    class PlaceHolder (placeView : View) : RecyclerView.ViewHolder(placeView){

        val placePreviewIv = placeView.findViewById<ImageView>(R.id.placePreviewIv)!!

        val placeNameTv = placeView.findViewById<TextView>(R.id.placeNameTv)!!
        val placeStationTv = placeView.findViewById<TextView>(R.id.placeStationTv)!!
        val placeDistanceTv = placeView.findViewById<TextView>(R.id.placeDistanceTv)!!
        val placeViewerTv = placeView.findViewById<TextView>(R.id.placeViewerTv)!!
        val placeReviewTv = placeView.findViewById<TextView>(R.id.placeReviewTv)!!
        val placeStarScoreTv = placeView.findViewById<TextView>(R.id.placeStarScoreTv)!!
    }
}