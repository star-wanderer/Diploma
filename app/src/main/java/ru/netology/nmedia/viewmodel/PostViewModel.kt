package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import ru.netology.nmedia.MediaLifecycleObserver
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.*
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

    private val mediaObserver = MediaLifecycleObserver()

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (id, _) ->
            repository.data.map { posts ->
                posts.map { item ->
                    if (item !is Post) item else { item.copy(
                            ownedByMe = item.authorId == id
                        )
                    }
                }
            }
        }

    private val _trackData = MutableLiveData<Track>()
    val trackData: LiveData<Track>
        get() = _trackData

    private val _audioState = MutableLiveData<AudioModel?>()
    val audioState: LiveData<AudioModel?>
        get() = _audioState

    private val _videoState = MutableLiveData<VideoModel?>()
    val videoState: LiveData<VideoModel?>
        get() = _videoState

    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private var _focusPost = MutableLiveData(empty)
    val focusPost: LiveData<Post>
        get() = _focusPost

    init {
        loadPosts()

        _trackData.value = Track()
        mediaObserver.apply {
            player?.setOnCompletionListener {
                player?.reset()
                _trackData.value = Track()
            }
        }
    }

    fun useAudioAttachment(post: Post) {
        mediaObserver.apply {
            _trackData.value.let{ track ->
                if (track?.itemId != post.id)
                    post.attachment?.let { it ->
                        player?.reset()
                        _trackData.value  = track?.copy(
                            isPlaying = true,
                            url = it.url,
                            itemId = post.id,
                        )
                        this.play(it.url)
                    }
                else {
                    if (!player?.isPlaying!!) {
                        this.resume()
                    } else {
                        this.pause()
                    }
                }
            }
        }
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
        _focusPost.value?.let { post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    _photoState.value?.let { photoModel ->
                        repository.saveWithAttachment(photoModel.file,
                            post.copy(attachment = Attachment(type = AttachmentType.IMAGE)))
                    } ?:
                    _audioState.value?.let { audioModel ->
                        repository.saveWithAttachment(audioModel.file,
                            post.copy(attachment = Attachment(type = AttachmentType.AUDIO)))
                    } ?:
                    _videoState.value?.let { videoModel ->
                        repository.saveWithAttachment(videoModel.file,
                            post.copy(attachment = Attachment(type = AttachmentType.VIDEO)))
                    } ?:
                    repository.save(post)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _photoState.value = null
        _audioState.value = null
        _videoState.value = null
        clear()
    }

    fun clear() {
        _focusPost.value = empty
    }

    fun getById(id: Long){
        viewModelScope.launch {
            try {
                _focusPost.value = repository.getById(id)
                println("GetById:${_focusPost.value}")
            }
            catch (_: Exception) {
            }
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_focusPost.value?.content == text) {
            return
        }
        _focusPost.value = _focusPost.value?.copy(content = text)
    }

    fun changeAudio(audioModel: AudioModel?) {
        _videoState.value = null
        _photoState.value = null
        if (audioModel == null){
            _focusPost.value = _focusPost.value?.copy(attachment = null)
        }
        _audioState.value = audioModel
    }

    fun changeVideo(videoModel: VideoModel?) {
        _audioState.value = null
        _photoState.value = null
        if (videoModel == null){
            _focusPost.value = _focusPost.value?.copy(attachment = null)
        }
        _videoState.value = videoModel
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _videoState.value = null
        _photoState.value = null
        if (photoModel == null){
            _focusPost.value = _focusPost.value?.copy(attachment = null)
        }
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
