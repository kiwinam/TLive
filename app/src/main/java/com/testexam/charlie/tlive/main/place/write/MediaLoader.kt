package com.testexam.charlie.tlive.main.place.write

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yanzhenjie.album.AlbumFile
import com.yanzhenjie.album.AlbumLoader

/**
 * yanzhenjie:album 라이브러리를 사용하기 위해 사진을 로드하는 MediaLoader 개클래스
 */
class MediaLoader : AlbumLoader {
    /* 전달된 사진 경로가 디바이스의 앨범 파일이라면 바로 로드한다. */
    override fun load(imageView: ImageView?, albumFile: AlbumFile?) {
        load(imageView,albumFile!!.path)
    }

    /* 전달된 사진 경로가 외부 url 일 경우 Glide 를 사용하여 load 한다. */
    override fun load(imageView: ImageView?, url: String?) {
        Glide.with(imageView!!.context)
                .load(url)
                .into(imageView)
    }
}