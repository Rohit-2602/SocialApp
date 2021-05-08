package com.example.socailapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Post(
    var id: String = "",
    val text: String = "",
    val creatorId: String = "",
    val createdAt: Long = 0L,
    val likedBy: @RawValue ArrayList<String> = ArrayList(),
    val image: String? = "",
    val commentedBy: @RawValue ArrayList<String> = ArrayList()
) : Parcelable