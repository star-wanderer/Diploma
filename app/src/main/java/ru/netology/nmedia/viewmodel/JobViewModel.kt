package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.JobRepository
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.SingleLiveEvent
import java.text.SimpleDateFormat
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = "",
    finish = "",
    link = "",
)

@HiltViewModel
@ExperimentalCoroutinesApi
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    appAuth: AppAuth
) : ViewModel() {

    val data: Flow<PagingData<FeedItem>> = repository.data

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    private var _focusJob = MutableLiveData(empty)
    val focusJob: LiveData<Job>
        get() = _focusJob

    private var _jobStarted = MutableLiveData<Long>()
    val jobStarted: LiveData<Long>
        get() = _jobStarted

    private var _jobFinished = MutableLiveData<Long>()
    val jobFinished: LiveData<Long>
        get() = _jobFinished

    private var _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() = _name

    private var _position = MutableLiveData<String>()
    val position: LiveData<String>
        get() = _position

    private var _link = MutableLiveData<String?>()
    val link: LiveData<String?>
        get() = _link

    init {
        loadJobs()
    }

    private fun loadJobs() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getInitial()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        _focusJob.value?.let { job ->
            _jobCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.save(job.copy(
                        start = AndroidUtils.dateFormatter(_jobStarted.value),
                        finish = AndroidUtils.dateFormatter(_jobFinished.value),
                        name = _name.value.toString(),
                        position = _position.value.toString(),
                        link = _link.value.toString())
                    )
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        clear()
    }

    fun clear() {
        _focusJob.value = empty
    }

    fun getById(id: Long) {
        viewModelScope.launch {
            try {
                _focusJob.value = repository.getById(id)
                _jobStarted.value = AndroidUtils.dateFormatter(_focusJob.value!!.start)
                _jobFinished.value = _focusJob.value!!.finish?.let { AndroidUtils.dateFormatter(it) }
                _link.value = _focusJob.value!!.link
            } catch (_: Exception) {
            }
        }
    }

    fun changeStartDate(datetime: Long){
        _jobStarted.value = datetime
    }

    fun changeFinishDate(datetime: Long){
        _jobFinished.value = datetime
    }

    fun changeLink(link: String){
        val text = link.trim()
        if (_focusJob.value?.link == text) {
            return
        }
        _link.value = text
    }

    fun changeName(content: String) {
        val text = content.trim()
        if (_focusJob.value?.name == text) {
            return
        }
        _name.value = text
    }

    fun changePosition(position: String) {
        val text = position.trim()
        if (_focusJob.value?.position == text) {
            return
        }
       _position.value = text
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (_: Exception) {
            }
        }
    }
}
