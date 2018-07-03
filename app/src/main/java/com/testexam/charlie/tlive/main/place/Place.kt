package com.testexam.charlie.tlive.main.place

import android.os.Parcel
import android.os.Parcelable

/**
 * 맛집의 정보를 담고 있는 데이터 클래스
 */
@Suppress("MemberVisibilityCanBePrivate")
data class Place(val no : Int,              // 평점 순서로 매겨진 번호
                 val placeNo : Int,         // 데이터 베이스 상의 맛집 번호
                 val name : String,         // 맛집의 이름
                 val lat : Double,          // 위도
                 val lon : Double,          // 경도
                 val nearStation : String,  // 가까이에 위치한 지하철 역 이름
                 val starScore : Float,     // 평점
                 val category : String,     // 맛집의 카테고리
                 val viewNum : Int,         // 맛집을 본 사람의 숫자
                 val reviewNum : Int,       // 리뷰의 숫자
                 val distance : Int,        // 현재 사용자와의 직선 거리
                 val previewSrc : String)   // 맛집의 미리보기 경로
    : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readFloat(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(no)
        parcel.writeInt(placeNo)
        parcel.writeString(name)
        parcel.writeDouble(lat)
        parcel.writeDouble(lon)
        parcel.writeString(nearStation)
        parcel.writeFloat(starScore)
        parcel.writeString(category)
        parcel.writeInt(viewNum)
        parcel.writeInt(reviewNum)
        parcel.writeInt(distance)
        parcel.writeString(previewSrc)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Place> {
        override fun createFromParcel(parcel: Parcel): Place {
            return Place(parcel)
        }

        override fun newArray(size: Int): Array<Place?> {
            return arrayOfNulls(size)
        }
    }
}