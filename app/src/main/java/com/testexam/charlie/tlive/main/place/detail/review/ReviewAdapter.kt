package com.testexam.charlie.tlive.main.place.detail.review

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.detail.photo.Photo
import com.testexam.charlie.tlive.main.place.detail.photo.PhotoAdapter
import org.json.JSONArray

class ReviewAdapter (private var reviewList : ArrayList<Review>, val context : Context, val placeNo : Int) : RecyclerView.Adapter<ReviewAdapter.ReviewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review,parent,false)
        return ReviewHolder(v)
    }
    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        val review = reviewList[position]

        holder.reviewNameTv.text = review.userName  // 리뷰 남긴 유저의 이름
        holder.reviewCountTv.text = review.userReviewCount.toString() // 유저가 그동안 남긴 리뷰의 수

        // 리뷰 점수에 따른 emotion 설정
        when(review.reviewPoint){
            5->{
                holder.reviewPointTv.text = "맛있다!"
                holder.reviewPointIv.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_emotion_good_orange))
            }
            3->{
                holder.reviewPointTv.text = "괜찮다"
                holder.reviewPointIv.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_emotion_normal_orange))
            }
            1->{
                holder.reviewPointTv.text = "별로"
                holder.reviewPointIv.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_emotion_bad_orange))
            }
        }

        holder.reviewTextTv.text = review.reviewText // 리뷰
        holder.reviewDateTv.text = review.uploadTime // 올린 날짜와 시간

        if(review.photoArray != "null"){
            holder.reviewPhotoRv.visibility = View.VISIBLE
            val photoList : ArrayList<Photo> = ArrayList()
            try{
                val photoJSONArray = JSONArray(review.photoArray)
                for(i in 0 until photoJSONArray.length()){
                    val src =photoJSONArray.getJSONObject(i).getString("src")
                    photoList.add(Photo(photoJSONArray.getJSONObject(i).getString("src")))
                }
                val horizontalLayoutManager = LinearLayoutManager(context)
                val photoAdapter = PhotoAdapter(photoList, context, placeNo)
                horizontalLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                holder.reviewPhotoRv.layoutManager = horizontalLayoutManager
                holder.reviewPhotoRv.adapter = photoAdapter
            }catch (e : Exception){
                e.printStackTrace()
            }
        }else{
            holder.reviewPhotoRv.visibility = View.GONE
        }
        
    }
    override fun getItemCount(): Int {
        return reviewList.size
    }

    fun setData(list : ArrayList<Review>){
        reviewList = list
        notifyDataSetChanged()
    }

    class ReviewHolder(holder : View) : RecyclerView.ViewHolder(holder){
        val reviewProfileIv = holder.findViewById<ImageView>(R.id.reviewProfileIv)!!
        val reviewPointIv = holder.findViewById<ImageView>(R.id.reviewPointIv)!!

        val reviewNameTv = holder.findViewById<TextView>(R.id.reviewNameTv)!!
        val reviewCountTv = holder.findViewById<TextView>(R.id.reviewCountTv)!!
        val reviewPointTv = holder.findViewById<TextView>(R.id.reviewPointTv)!!
        val reviewTextTv = holder.findViewById<TextView>(R.id.reviewTextTv)!!
        val reviewDateTv = holder.findViewById<TextView>(R.id.reviewDateTv)!!

        val reviewPhotoRv = holder.findViewById<RecyclerView>(R.id.reviewPhotoRv)!!
    }
}