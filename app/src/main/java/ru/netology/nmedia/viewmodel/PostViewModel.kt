package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState

import ru.netology.nmedia.model.PhotoModel

import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
//


private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).postDao(),
        application.contentResolver,
    )
    var firstId: Long = 0

    // private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> = repository.data.map(::FeedModel)


        .asLiveData(Dispatchers.Default)
    //  get() = _data

    private val _state = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val state: LiveData<FeedModelState>
        get() = _state

    val newerCount: LiveData<Int> = data.switchMap { //следим за таблицей и как только изменется
        firstId = it.posts.firstOrNull()?.id ?: 0L
        repository.getNewerCount(firstId) //пересоздаем подписку на новые посты
            .asLiveData(Dispatchers.Default)
    }


    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState.Loading
            try {
                repository.getAllAsync()
                _state.value = FeedModelState.Idle
            } catch (e: Exception) {
                _state.value = FeedModelState.Error
            }
        }
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState.Loading

                repository.likeByIdAsync(post)
                _state.value = FeedModelState.Idle
                _postCreated.postValue(Unit)
                //  loadPosts()
            } catch (e: Exception) {
                _state.value = FeedModelState.Error
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            edited.value?.let {

                //         println("do do")
                repository.save(it)
                _postCreated.postValue(Unit)
            }
            edited.value = empty
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            repository.removeById(id)
            // loadPosts()
//            override fun onSuccess(posts: Unit) {
//
//            }
//
//            override fun onError(e: Exception) {
//                _data.value = (FeedModel(error = true))
//            }
//        })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }


    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState.Refreshing
            try {
                repository.getAllAsync()
                _state.value = FeedModelState.Idle
            } catch (e: Exception) {
                _state.value = FeedModelState.Error

            }
        }
    }

    fun loadNewPosts() = viewModelScope.launch {
        try {
            println("loadNewPosts1111111111111111111111111111111111111")
            _state.value = FeedModelState.Loading
            repository.getNewPosts()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = if (uri != null) {
            PhotoModel(uri)
        } else {
            null
        }
    }
}