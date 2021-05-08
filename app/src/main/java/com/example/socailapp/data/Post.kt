package com.example.socailapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Post(
    var id: String = "",
    val text: String = "",
    /*val creator: User = User(),*/
    val creatorId: String = "",
    val createdAt: String = "",
    val likedBy: ArrayList<String> = ArrayList(),
    val image: String? = "",

)