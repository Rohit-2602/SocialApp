package com.example.socailapp.data

data class Comment(
    var id: String = "",
    val creatorId: String = "",
    val text: String = "",
    val creationTime: Long = 0L
)