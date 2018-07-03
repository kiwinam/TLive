package com.testexam.charlie.tlive.main.place.detail.photo

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R

/**
 * 맛집 상세보기에서 맨 위 사진 미리 보기와 리뷰에서 리뷰 사진 리스트에서 사용하는 어댑터
 *
 * 맛집 미리 보기 사진의 경우 마지막 사진에 "전체 사진 보기" 글자를 보여준다.
 * "전체 사진 보기" 글자가 보이는 사진을 누르면 해당 맛집의 사진을 전부 보여주는 Activity 로 이동한다.
 */
class PhotoAdapter(private var photoList : ArrayList<Photo>, val context : Context, private val placeNo : Int) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address
    private var listSize = 0 // 전체 리스트의 크기를 지정한다.
    private var isAllPhoto = false  //

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place_photo,parent,false)
        return PhotoViewHolder(v)
    }
    /* 사진 데이터를 ViewHolder 에 보이게한다. */
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val placePhoto = photoList[position]

        // 이미지 사진 로드
        if(placePhoto.url != ""){
            Glide.with(context)
                    .load(reviewImgUrl+placePhoto.url)
                    .into(holder.photoIv)
        }

        // 마지막 사진이라면 "전체 사진 보기" 글자를 보여주고 클릭 시 전체 사진을 보여주는 Activity 로 이동한다.
        if((listSize - 1) == position && !isAllPhoto) {
            holder.showMoreLo.visibility = View.VISIBLE
            holder.showMoreLo.setOnClickListener({ // 마지막 사진을 클릭하면 전체 사진을 보여주는 Activity 로 이동.
                val photoIntent = Intent(context, PlacePhotoActivity::class.java)
                photoIntent.putExtra("placeNo",placeNo) // 맛집의 번호를 인텐트에 담는다.
                context.startActivity(photoIntent)  // 전체 사진 보기 액티비티로 이동한다.
            })
        }else{
            holder.showMoreLo.visibility = View.GONE    // 전체사진 보기 레이아웃을 보이지 않게 한다.
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    fun setAllPhoto(all : Boolean){
        isAllPhoto = all
    }

    fun setData(list : ArrayList<Photo>){
        photoList = list // 새로운 데이터를 기존 리스트에 담는다.
        listSize = photoList.size // 리스트의 크기를 저장한다.
        notifyDataSetChanged()
    }

    class PhotoViewHolder (photoView : View): RecyclerView.ViewHolder(photoView){
        val photoIv : ImageView = photoView.findViewById(R.id.photoIv)!!
        val showMoreLo : RelativeLayout = photoView.findViewById(R.id.showMoreLo)!!
    }
}