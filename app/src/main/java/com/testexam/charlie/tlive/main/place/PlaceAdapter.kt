package com.testexam.charlie.tlive.main.place


import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R

class PlaceAdapter (private var placeList : ArrayList<Place>, val context : Context) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>(){
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address
    private var lastPosition = -1
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

        if(place.previewSrc != ""){
            Glide.with(context)
                    .load(reviewImgUrl+place.previewSrc)
                    .into(holder.placePreviewIv)
        }
        holder.placeNameTv.text = (place.no).toString()+". "+place.name
        holder.placeStationTv.text = place.nearStation+" - "
        holder.placeDistanceTv.text = place.distance.toString()+"m"
        holder.placeViewerTv.text = place.viewNum.toString()
        holder.placeReviewTv.text = place.reviewNum.toString()
        holder.placeStarScoreTv.text = place.starScore.toString()
        //setAnimation(holder.itemView, position)
    }

    override fun onViewDetachedFromWindow(holder: PlaceHolder) {
        holder.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    fun setData(list : ArrayList<Place>){
        placeList = list
        notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate : View, position : Int){
        if(position>lastPosition){
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }



    class PlaceHolder (placeView : View) : RecyclerView.ViewHolder(placeView){

        val placePreviewIv = placeView.findViewById<ImageView>(R.id.placePreviewIv)!!

        val placeNameTv = placeView.findViewById<TextView>(R.id.placeNameTv)!!
        val placeStationTv = placeView.findViewById<TextView>(R.id.placeStationTv)!!
        val placeDistanceTv = placeView.findViewById<TextView>(R.id.placeDistanceTv)!!
        val placeViewerTv = placeView.findViewById<TextView>(R.id.placeViewerTv)!!
        val placeReviewTv = placeView.findViewById<TextView>(R.id.placeReviewTv)!!
        val placeStarScoreTv = placeView.findViewById<TextView>(R.id.placeStarScoreTv)!!

        fun clearAnimation(){

        }
    }
}