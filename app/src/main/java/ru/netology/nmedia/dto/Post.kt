package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean = true,
    val likes: Int = 7,
    val attachment: Attachment? = null,
    val viewed: Boolean = false
)

