package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.*
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.EventEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.entity.toEntityInitial
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    val db: AppDb,
    private val userDao: UserDao,
    private val eventDao: EventDao,
    private val eventUserDao: EventUserDao,
    private val eventSpeakerDao: EventSpeakerDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val apiService: ApiService,
) : EventRepository {

    private val pageSize = 5

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = EventRemoteMediator(apiService,db,eventDao,eventUserDao,eventSpeakerDao,eventRemoteKeyDao),
        pagingSourceFactory = eventDao::pagingSource,
    ).flow.map { pagingData->
        pagingData.map(EventEntity::toDto)
            .map { event ->
            val myUsers = mutableMapOf<String,UserPreview>()
            eventUserDao.getById(event.id).forEach{ eventUser ->
                myUsers[eventUser.userId] = UserPreview(eventUser.name,eventUser.avatar)
            }
                val speakers = mutableMapOf<Long,String>()
                val speakerIds = ArrayList<Long>()
                eventSpeakerDao.getById(event.id).forEach { eventSpeaker ->
                speakerIds.add(eventSpeaker.speakerId)
                speakers[eventSpeaker.speakerId] = userDao.getById(eventSpeaker.speakerId).authorAvatar.toString()
                }
            event.copy(users = myUsers, speakerIds = speakerIds, speakers = speakers)
        }
    }

    private val _authData = MutableLiveData<AuthModel>()
    override val authData: LiveData<AuthModel>
        get() = _authData

    override suspend fun getInitial() {
        try {
            val response = apiService.getLatestEvents(pageSize)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            if (eventDao.isEmpty()) {
                eventDao.insert(body.toEntityInitial())
            } else {
                eventDao.insert(body.toEntity())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(file: File, event: Event) {
        try {
            val media = upload(file)
            val response = apiService.saveEvents(
                event.copy(
                    attachment = Attachment(
                        url = media.url,
                        type = event.attachment?.type
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun upload(file: File): Media {
        return apiService.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )
            .let { requireNotNull(it.body()) }
    }

    override suspend fun save(event: Event) {
        try {
            val response = apiService.saveEvents(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            eventDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.likeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun disLikeById(id: Long) {
        try {
            val response = apiService.dislikeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun checkInById(id: Long) {
        try {
            val response = apiService.checkInEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun checkOutById(id: Long) {
        try {
            val response = apiService.checkOutEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long) : Event {
        val myMap = mutableMapOf<String,UserPreview>()
        eventUserDao.getById(id).forEach{ eventUser ->
            myMap[eventUser.userId] = UserPreview(eventUser.name,eventUser.avatar)
        }
        return eventDao.getById(id).let(EventEntity::toDto).copy(users = myMap)
    }

//    override fun getNewerCount(id: Long): Flow<Int> = flow {
//        while (true) {
//            delay(10_000L)
//            val response = apiService.getNewerEvents(id + eventDao.getIsNewCount())
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            eventDao.insert(body.toEntity())
//            emit(eventDao.getIsNewCount())
//        }
//    }
//        .catch { e -> throw AppError.from(e) }
//        .flowOn(Dispatchers.Default)
//
//    override suspend fun update() {
//        eventDao.update()
//    }
}
