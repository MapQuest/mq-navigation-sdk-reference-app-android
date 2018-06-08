package com.mapquest.navigation.sampleapp.routesettings

import android.os.Parcel
import android.os.Parcelable
import com.mapquest.navigation.model.location.Coordinate


data class RouteStop(val displayText: String, val coordinate: Coordinate, val mqId: String?): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(Coordinate::class.java.classLoader),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(displayText)
        parcel.writeParcelable(coordinate, flags)
            parcel.writeString(mqId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteStop> {
        override fun createFromParcel(parcel: Parcel): RouteStop {
            return RouteStop(parcel)
        }

        override fun newArray(size: Int): Array<RouteStop?> {
            return arrayOfNulls(size)
        }
    }

}