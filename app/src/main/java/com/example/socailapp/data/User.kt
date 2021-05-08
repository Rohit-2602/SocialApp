package com.example.socailapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    var id: String = "",
    var name: String? = "",
    var lowercaseName: String? = "",
    var imageURL: String = "",
    var description: String = "",
    @Ignore
    val connectionRequests: ArrayList<String> = ArrayList(),
    @Ignore
    val incomingRequest: ArrayList<String> = ArrayList(),
    @Ignore
    val connections: ArrayList<String> = ArrayList(),
    var backgroundImage: String = "https://firebasestorage.googleapis.com/v0/b/social-app-f333c.appspot.com/o/diamond_background.jpg?alt=media&token=0ba0bd82-7ea1-4425-bd80-874edeb98461"
) : Parcelable