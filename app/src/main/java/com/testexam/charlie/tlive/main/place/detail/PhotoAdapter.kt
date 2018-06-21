package com.testexam.charlie.tlive.main.place.detail

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R

/**
 *
 */
class PhotoAdapter(private var photoList : ArrayList<PlacePhoto>, val context : Context) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
    private val reviewImgUrl = "http://13.125.64.135/review/" // AWS 의 Elastic IP address
    private var listSize = 0 // 전체 리스트의 크기를 지정한다.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place_photo,parent,false)
        return PhotoViewHolder(v)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val placePhoto = photoList[position]

        // 이미지 사진 로드
        if(placePhoto.url != ""){
            Glide.with(context)
                    .load(reviewImgUrl+placePhoto.url)
                    .into(holder.photoIv)
        }

        // 마지막 사진이라면 "전체 사진 보기" 글자를 보여주고 클릭 시 전체 사진을 보여주는 Activity 로 이동한다.
        if((listSize - 1) == position){
            holder.showMoreLo.visibility = View.VISIBLE
            holder.showMoreLo.setOnClickListener({ // 마지막 사진을 클릭하면 전체 사진을 보여주는 Activity 로 이동.
                //val intent = Intent(context,)
            })
        }else{
            holder.showMoreLo.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return photoList.size
    }

    fun setData(list : ArrayList<PlacePhoto>){
        photoList = list // 새로운 데이터를 기존 리스트에 담는다.
        listSize = photoList.size // 리스트의 크기를 저장한다.
    }

    class PhotoViewHolder (photoView : View): RecyclerView.ViewHolder(photoView){
        val photoIv : ImageView = photoView.findViewById(R.id.photoIv)!!
        val showMoreLo : RelativeLayout = photoView.findViewById(R.id.showMoreLo)!!
    }
}