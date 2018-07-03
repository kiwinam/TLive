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

/**
 * 티 맵 하단에서 맛집 리스트를 보여주는 어댑터
 */
class MapPlaceAdapter(private var placeList : ArrayList<Place>, val context : Context) : RecyclerView.Adapter<MapPlaceAdapter.MapPlaceViewHolder>(){
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPlaceViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_map_place,parent,false)
        return MapPlaceViewHolder(v)
    }

    /* Place 객체의 정보를 ViewHolder 에 표시한다. */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MapPlaceViewHolder, position: Int) {
        val place = placeList[position]     // placeList 에서 포지션에 맞는 Place 객체를 가져온다.

        if(place.previewSrc != ""){ // 맛집의 미리보기 사진이 있는 경우
            Glide.with(context)     // 사진을 표시한다.
                    .load(reviewImgUrl+place.previewSrc)
                    .into(holder.mapPlacePreviewIv)
        }
        holder.mapPlaceNameTv.text = (place.no).toString()+". "+place.name  // 맛집의 이름을 설정한다. 평점 순서대로 1 부터 숫자가 매겨진다.
        holder.mapPlaceStationTv.text = place.nearStation   // 가까이에 있는 지하철 역을 표시한다.
        holder.mapPlaceViewNumTv.text = place.viewNum.toString()    // 맛집 상세보기를 본 사람의 수를 표시한다.
        holder.mapPlaceReviewNumTv.text = place.reviewNum.toString()    // 리뷰의 개수를 표시한다.
        holder.mapPlaceStarScoreTv.text = place.starScore.toString()    // 평점을 표시한다.
    }

    /* 맛집 리스트의 개수를 리턴하는 메소드 */
    override fun getItemCount(): Int { return placeList.size }

    /*
     * 맛집 리스트를 갱신하는 메소드
     */
    fun setData(list : ArrayList<Place>){
        placeList = list    // 매개변수로 전달받은 list 를 placeList 에 넣는다.
        notifyDataSetChanged()  // 데이터 세트가 변경됨을 알려준다.
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