package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

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
        AppDb.getInstance(application).postDao()
    )

    // private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel> = repository.data.map (::FeedModel )
        .asLiveData(Dispatchers.Default)
    //  get() = _data

    private val _state = MutableLiveData<FeedModelState>(FeedModelState.Idle)
    val state: LiveData<FeedModelState>
        get() = _state

    val newerCount: LiveData<Int> = data.switchMap {                            //следим за таблицей и как только изменется
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)          //пересоздаем подписку на новые посты
            .asLiveData(Dispatchers.Default)
    }


    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

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

             //   println("555555555555555555555555555555555555555555555555555555555")
                ///////////////////////////////////////////////////////////////////////////////////////////////
                repository.likeByIdAsync(post)
                _state.value = FeedModelState.Idle
                _postCreated.postValue(Unit)
                loadPosts()
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
                loadPosts()
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
            _state.value = FeedModelState.Loading
            repository.getNewPosts()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }
    }