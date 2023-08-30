package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.MediaLifecycleObserver
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.EventRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    datetime = "",
    published = "",
    likedByMe = false,
    attachment = null,
    participatedByMe = false,
    ownedByMe = false,
    coords = null,
    type = Type.OFFLINE,
    users =  null,
    speakerIds = null,
    speakers = null,
)

@HiltViewModel
@ExperimentalCoroutinesApi
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val mediaObserver = MediaLifecycleObserver()

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { (id, _) ->
            repository.data.map { events ->
                events.map { item ->
                    if (item !is Event ) item else item.copy(
                        ownedByMe = item.authorId == id)
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

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private var _focusEvent = MutableLiveData(empty)
    val focusEvent: LiveData<Event>
        get() = _focusEvent

    private var _plannedEvent = MutableLiveData<String?>()
    val plannedEvent: LiveData<String?>
        get() = _plannedEvent

    private var eventType: Type = Type.OFFLINE

    private var _speakerList = MutableLiveData<ArrayList<Long>>()
    val speakerList: LiveData<ArrayList<Long>>
        get() = _speakerList

    init {
        loadEvents()
        _plannedEvent.value = dateFormatter(Calendar.getInstance().timeInMillis)
        _speakerList.value = arrayListOf()
    }

    fun changeSpeakers(id: Long){
        if (_speakerList.value?.contains(id) == true)
            _speakerList.value?.remove(id)
        else _speakerList.value.let { it?.add(id) }
    }

    fun useAudioAttachment(event: Event) {
        mediaObserver.apply {
            _trackData.value.let{ track ->
                if (track?.itemId != event.id)
                    event.attachment?.let { it ->
                        player?.reset()
                        _trackData.value  = track?.copy(
                            isPlaying = true,
                            url = it.url,
                            itemId = event.id,
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

    private fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getInitial()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        _focusEvent.value = _plannedEvent.value?.let {
            _focusEvent.value?.copy(datetime = it, type = eventType, speakerIds = speakerList.value )
        }
        _focusEvent.value?.let { event ->
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    _photoState.value?.let { photoModel ->
                        repository.saveWithAttachment(
                            photoModel.file,
                            event.copy(attachment = Attachment(type = AttachmentType.IMAGE))
                        )
                    } ?: _audioState.value?.let { audioModel ->
                        repository.saveWithAttachment(
                            audioModel.file,
                            event.copy(attachment = Attachment(type = AttachmentType.AUDIO))
                        )
                    } ?:
                    _videoState.value?.let { videoModel ->
                        repository.saveWithAttachment(
                            videoModel.file,
                            event.copy(attachment = Attachment(type = AttachmentType.VIDEO))
                        )
                    } ?: repository.save(event)
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _photoState.value = null
        _audioState.value = null
        _videoState.value = null
        _plannedEvent.value = null
        clear()
    }

    fun clear() {
        _focusEvent.value = empty
    }

    fun getById(id: Long) {
        viewModelScope.launch {
            try {
                _focusEvent.value = repository.getById(id)
            } catch (_: Exception) {
            }
        }
    }

    fun changeEventType(type: Type) {
        eventType = type
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_focusEvent.value?.content == text) {
            return
        }
        _focusEvent.value = _focusEvent.value?.copy(content = text)
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateFormatter(time: Long): String{
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return formatter.format(time)
    }

    @SuppressLint("SimpleDateFormat")
    fun changeEventDate(datetime: Long){
       _plannedEvent.value = dateFormatter(datetime)
    }

    fun changeAudio(audioModel: AudioModel?) {
        _audioState.value = audioModel
        _videoState.value = null
        _photoState.value = null
    }

    fun changeVideo(videoModel: VideoModel?) {
        _videoState.value = videoModel
        _audioState.value = null
        _photoState.value = null
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
        _videoState.value = null
        _photoState.value = null
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

    fun checkInById(id: Long) {
        viewModelScope.launch {
            try {
                repository.checkInById(id)
            } catch (_: Exception) {
            }
        }
    }

    fun checkOutById(id: Long) {
        viewModelScope.launch {
            try {
                repository.checkOutById(id)
            }
            catch (_: Exception) {
            }
        }
    }
}
