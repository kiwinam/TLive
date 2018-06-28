package com.testexam.charlie.tlive.main.place.detail.imageSlide

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.place.detail.photo.Photo


class ImageSlideAdapter(val context: Context, private val layoutInflater: LayoutInflater, private val photoList : ArrayList<Photo>) : PagerAdapter(){
    private val url = "http://13.125.64.135/review/"

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.item_image_slide, null)
        val iv = view.findViewById<ImageView>(R.id.slideIv)
        Glide.with(context)
                .load(url+photoList[position].url)
                .into(iv)
        container.addView(view)
        return view
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return photoList.size
    }
}