package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {

    val data: Float<List<Post>>

    //val data: LiveData<List<Post>>
    suspend  fun getAllAsync()
    suspend  fun save(post: Post)
    suspend  fun removeById(id: Long)
    suspend fun likeByIdAsync(post: Post)
}