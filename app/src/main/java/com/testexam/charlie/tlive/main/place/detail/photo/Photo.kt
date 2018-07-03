package com.testexam.charlie.tlive.main.place.detail.photo

import android.os.Parcel
import android.os.Parcelable

/**
 * 맛집 상세보기에서 리뷰나 사진 미리보기에서 사용하는 사진 데이터 클래스
 */
data class Photo(val url : String   // 사진의 경로
                ) : Parcelable{
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }
    }

}