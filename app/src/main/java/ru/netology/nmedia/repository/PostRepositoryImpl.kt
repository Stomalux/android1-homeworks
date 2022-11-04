package ru.netology.nmedia.repository



import android.content.ContentResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.PostApiServiceHolder
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException

class PostRepositoryImpl(private val postDao: PostDao, private val contentResolver: ContentResolver,) : PostRepository {



    override val data =  postDao.getAll().map { it.toDto() }

         .map {  it.filter {it.viewed }}


        .flowOn(Dispatchers.Default)
    override fun getNewerCount(firstId: Long): Flow<Int> = flow {
        try {
            while (true) {
                delay(10_000L)
/////////////////////////////////////////////////////////////
                println(firstId)
                val response = PostApiServiceHolder.service.getNewer(firstId)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(body.toEntity().map { it.copy(viewed = false) })

                println("bodi size")
                 println(body.size)
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                emit(body.size)

            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }

    }

//    override fun getNewerLoad(firstId: Long): Flow<Int> = flow {
//        try {
//            while (true) {
//                val response = PostApiServiceHolder.service.getNewer(firstId)
//                if (!response.isSuccessful) {
//                    throw ApiError(response.code(), response.message())
//                }
//                val body = response.body() ?: throw ApiError(response.code(), response.message())
//                postDao.insert(body.toEntity())
//                emit(body.size)
//                delay(10_000L)
//            }
//        } catch (e: CancellationException) {
//            throw e
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//
//    }
    override suspend fun getAllAsync() {
        try {
            val response = PostApiServiceHolder.service.getAllAsync()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity()
                .map { it.copy(viewed = true) })

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = PostApiServiceHolder.service.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body).copy(viewed = true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postDao.removeById(id)
            val response = PostApiServiceHolder.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun getNewPosts() {
        try {
            println("getNewPosts11111111111111111111111111111111111111")
            postDao.viewedPosts()
            println("viewedPosts111111111111111111111")
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
    override suspend fun likeByIdAsync(post: Post) {
        val id = post.id
        if (!post.likedByMe) {
            try {
                postDao.likeById(id)
                val response = PostApiServiceHolder.service.likeByIdAsync(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(PostEntity.fromDto(body).copy(viewed = true))
           //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        } else {
            try {
                postDao.likeById(id)
                val response = PostApiServiceHolder.service.delitLikeByIdAsync(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(PostEntity.fromDto(body).copy(viewed = true))
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

    private suspend fun upload(upload: PhotoModel): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file",
                "file",
                withContext(Dispatchers.Default) {
                    requireNotNull(contentResolver.openInputStream(upload.uri))
                        .readBytes()
                        .toRequestBody()
                }
            )

            val response = PostApiServiceHolder.service.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }



}
