package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = "",
    likedByMe = false,
    ownedByMe = false,
    mentionedMe = false,
    attachment = null,
    coords = null,
    users = null,
)

@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

//    private val cached = repository
//        .data
//        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (id, _) ->
            repository.data.map { posts ->
                posts.map { item ->
                    if (item !is Post) item else item.copy(ownedByMe = item.authorId == id)
                }
            }
        }

    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private var _activeData = MutableLiveData(empty)
    val activeData: LiveData<Post>
        get() = _activeData

    init {
        loadPosts()
    }

    private fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getInitial()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        _activeData.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    _photoState.value?.let { photoModel ->
                        repository.saveWithAttachment(photoModel.file, it)
                    } ?: repository.save(it)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _photoState.value = null
        clear()
    }

    fun clear() {
        _activeData.value = empty
    }

    fun edit(post: Post) {
        _activeData.value = post
    }

    fun getById(id: Long) {
        viewModelScope.launch {
            try {
                _activeData.value = repository.getById(id)
            } catch (_: Exception) {
            }
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_activeData.value?.content == text) {
            return
        }
        _activeData.value = _activeData.value?.copy(content = text)
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (_: Exception) {
            }
        }
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
            } catch (_: Exception) {
            }
        }
    }

    fun disLikeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.disLikeById(id)
            } catch (_: Exception) {
            }
        }
    }
}
