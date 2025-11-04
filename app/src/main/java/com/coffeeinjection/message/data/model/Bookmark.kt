package com.coffeeinjection.message.data.model

data class Bookmark(
    val id : Long,
    val title : String,
    val content : String,
    val userProfileImg : Int,
    val shareIcon : Int,
    val bookmarkIcon : Int,
)
