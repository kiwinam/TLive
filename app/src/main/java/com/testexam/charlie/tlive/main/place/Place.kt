package com.testexam.charlie.tlive.main.place

import android.os.Parcel
import android.os.Parcelable

data class Place(val no : Int,
                 val placeNo : Int,
                 val name : String,
                 val lat : Double,
                 val lon : Double,
                 val nearStation : String,
                 val starScore : Float,
                 val category : String,
                 val viewNum : Int,
                 val reviewNum : Int,
                 val distance : Int,
                 val previewSrc : String) : Parcelable {
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