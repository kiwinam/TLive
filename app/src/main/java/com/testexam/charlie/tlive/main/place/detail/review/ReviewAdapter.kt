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

/**
 * 리뷰를 표시하는 어댑터
 *
 * 맛집 상세보기에서 가장 최근 올린 리뷰와 리뷰 전체보기에서 사용된다.
 */
class ReviewAdapter (private var reviewList : ArrayList<Review>, val context : Context, private val placeNo : Int) : RecyclerView.Adapter<ReviewAdapter.ReviewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_review,parent,false)
        return ReviewHolder(v)
    }
    /* 리뷰 객체에 있는 정보를 ViewHolder 에 표시한다. */
    override fun onBindViewHolder(holder: ReviewHolder, position: Int) {
        val review = reviewList[position]   // reviewList 에서 포지션에 맞게 리뷰 객체를 가져온다.

        holder.reviewNameTv.text = review.userName  // 리뷰 남긴 유저의 이름
        holder.reviewCountTv.text = review.userReviewCount.toString() // 유저가 그동안 남긴 리뷰의 수

        // 리뷰 점수에 따른 emotion 설정
        when(review.reviewPoint){
            5->{    // 5점일 경우 맛있다
                holder.reviewPointTv.text = "맛있다!"
                holder.reviewPointIv.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_emotion_good_orange))
            }       // 3점일 경우 괜찮다
            3->{
                holder.reviewPointTv.text = "괜찮다"
                holder.reviewPointIv.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_emotion_normal_orange))
            }       // 1점일 경우 별로
            1->{
                holder.reviewPointTv.text = "별로"
                holder.reviewPointIv.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_emotion_bad_orange))
            }
        }

        holder.reviewTextTv.text = review.reviewText // 리뷰
        holder.reviewDateTv.text = review.uploadTime // 올린 날짜와 시간

        if(review.photoArray != "null"){    // 리뷰에 사진이 있다면
            holder.reviewPhotoRv.visibility = View.VISIBLE  // 리뷰 사진 RecyclerView 를 보이게한다.
            val photoList : ArrayList<Photo> = ArrayList()  // 사진 ArrayList 를 초기화한다.
            try{
                val photoJSONArray = JSONArray(review.photoArray)   // JSONArray 형식으로 변환한다.
                for(i in 0 until photoJSONArray.length()){
                    photoList.add(Photo(photoJSONArray.getJSONObject(i).getString("src")))  // 사진 리스트에 추가한다.
                }
                val horizontalLayoutManager = LinearLayoutManager(context)  // 레이아웃 매니저를 초기화한다.
                val photoAdapter = PhotoAdapter(photoList, context, placeNo)    // 사진 어댑터를 초기화한다.
                horizontalLayoutManager.orientation = LinearLayoutManager.HORIZONTAL    // 레이아웃 매니저의 방향을 가로로 설정한다.
                holder.reviewPhotoRv.layoutManager = horizontalLayoutManager    // reviewPhotoRv 에 레이아웃 매니저를 설정한다.
                holder.reviewPhotoRv.adapter = photoAdapter         // reviewPhotoRv 에 어댑터를 설정한다.
            }catch (e : Exception){
                e.printStackTrace()
            }
        }else{
            holder.reviewPhotoRv.visibility = View.GONE // 리뷰 사진 RecyclerView 를 보이지 않게한다.
        }
        
    }
    /* 리뷰의 길이를 리턴한다. */
    override fun getItemCount(): Int { return reviewList.size }

    /*
     * 매개 변수로 전달된 리뷰 리스트를 reviewList 에 넣고 데이터 변경을 알린다.
     */
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