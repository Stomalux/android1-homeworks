package ru.netology.nmedia.repository


import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {

    val data: Flow<List<Post>>

    //val data: LiveData<List<Post>>

    fun getNewerCount(firstId: Long): Flow<Int>
    suspend  fun getAllAsync()
    suspend  fun save(post: Post)
    suspend  fun removeById(id: Long)
    suspend fun likeByIdAsync(post: Post)
}