package com.example.socailapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String? = "",
    val lowercaseName: String? = "",
    val imageURL: String = "",
    val description: String = "",
    val connectionRequests: ArrayList<String> = ArrayList(),
    val incomingRequest: ArrayList<String> = ArrayList(),
    val mutualConnections: ArrayList<String> = ArrayList()
) : Parcelable