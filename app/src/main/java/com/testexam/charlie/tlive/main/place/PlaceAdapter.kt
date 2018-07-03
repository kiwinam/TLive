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

/**
 * 맛집 리스트의 어댑터
 */
class PlaceAdapter (private var placeList : ArrayList<Place>, val context : Context) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>(){
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address
    private var lastPosition = -1   // 마지막 위치
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place,parent,false)
        return PlaceHolder(v)
    }

    /* Place 객체의 정보를 ViewHolder 에 표시한다. */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val place = placeList[position]     // placeList 에서 포지션에 맞게 Place 객체를 가져온다.

        if(place.previewSrc != ""){ // preview 가 존재한다면
            Glide.with(context) // placePreviewIv 에 표시한다.
                    .load(reviewImgUrl+place.previewSrc)
                    .into(holder.placePreviewIv)
        }
        holder.placeNameTv.text = (place.no).toString()+". "+place.name // 맛집의 이름을 표시한다 . 평점 순서로 매겨진 번호를 앞에 붙인다.
        holder.placeStationTv.text = place.nearStation+" - "    // 근처에 있는 역을 표시한다.
        holder.placeDistanceTv.text = place.distance.toString()+"m" // 현재 사용자와의 직선 거리를 표시한다.
        holder.placeViewerTv.text = place.viewNum.toString()    // 맛집을 본 사람들의 숫자를 표시한다.
        holder.placeReviewTv.text = place.reviewNum.toString()  // 리뷰의 숫자를 표시한다
        holder.placeStarScoreTv.text = place.starScore.toString()   // 평점을 표시한다.
        //setAnimation(holder.itemView, position)
    }
    /* 맛집 리스트의 개수를 리턴하는 메소드 */
    override fun getItemCount(): Int {
        return placeList.size
    }
    /* 애니메이션을 뷰에서 떼어내는 메소드 */
    override fun onViewDetachedFromWindow(holder: PlaceHolder) {
        holder.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    /* 매개 변수로 전달된 list 를 placeList 에 넣고 데이터 세트가 변경된 것을 알린다. */
    fun setPlaceList(list : ArrayList<Place>){
        placeList = list
        notifyDataSetChanged()
    }

    /* 애니메이션을 설정한다. */
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