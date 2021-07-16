package com.appknot.sample.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
@JsonClass(generateAdapter = true)
data class PassengerInfo(
    val totalPassengers: Int,
    val totalPages: Int,
    val data: List<Passenger>
): Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class Passenger(
        val _id: String,
        val name: String,
        val trips: Int?,
//        val airline: Airline
    ): Parcelable {

        @Parcelize
        @JsonClass(generateAdapter = true)
        data class Airline(
            val id: Int,
            val name: String,
            val country: String,
            val logo: String,
            val slogan: String,
            @Json(name = "head_quaters")
            val headQuaters: String,
            val website: String,
            val established: String
        ): Parcelable
    }
}