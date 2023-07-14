package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.entity.toEntityInitial
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import java.io.File
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PostRepositoryImpl @Inject constructor(
    val db: AppDb,
    private val dao: PostDao,
    val postRemoteKeyDao: PostRemoteKeyDao,
    private val apiService: ApiService,
) : PostRepository {

    private val pageSize = 5

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = PostRemoteMediator(apiService,db,dao,postRemoteKeyDao),
        pagingSourceFactory = dao::pagingSource,
    ).flow.map { pagingData->
        pagingData.map(PostEntity::toDto)
            .insertSeparators(
            generator = { before , after ->
                val beforeTimeDiff = System.currentTimeMillis()/1000 - before?.published?.toLong()!!
                val afterTimeDiff = System.currentTimeMillis()/1000 - after?.published?.toLong()!!
                if ((afterTimeDiff in 1..86399998) && (beforeTimeDiff in 86399999 ..172800000))
                    TextSeparator(
                        Random.nextLong(),
                        "TODAY")
                if ((afterTimeDiff in 86399999 ..172800000) && (beforeTimeDiff >= 172800000))
                    TextSeparator(
                        Random.nextLong(),
                        "YESTERDAY")
                if   (afterTimeDiff >= 172800001)
                    TextSeparator(
                    Random.nextLong(),
                    "LAST WEEK"
                    )
                else null
            }
        )
    }

    private val _authData = MutableLiveData<AuthModel>()
    override val authData: LiveData<AuthModel>
        get() = _authData

    override suspend fun authenticate(login: String, password: String) {
        try {
            val response = apiService.authenticate(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            _authData.value = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getInitial() {
        try {
            val response = apiService.getLatest(pageSize)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            if (dao.isEmpty()) {
                dao.insert(body.toEntityInitial())
            } else {
                dao.insert(body.toEntity())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(file: File, post: Post) {
        try {
            val media = upload(file)
            val response = apiService.save(
                post.copy(
                    attachment = Attachment(
                        url = media.id,
                        AttachmentType.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
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

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getNewer(id + dao.getIsNewCount())
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
            emit(dao.getIsNewCount())
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun update() {
        dao.update()
    }

    override suspend fun removeById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun likeById(id: Long) {
        TODO("Not yet implemented")
    }


}
