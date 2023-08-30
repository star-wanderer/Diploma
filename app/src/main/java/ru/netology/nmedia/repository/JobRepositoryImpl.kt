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
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.entity.toEntityInitial
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    val db: AppDb,
    private val jobDao: JobDao,
    private val jobRemoteKeyDao: JobRemoteKeyDao,
    private val apiService: ApiService,
) : JobRepository {

    private val pageSize = 5

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = JobRemoteMediator(apiService,db,jobDao,jobRemoteKeyDao),
        pagingSourceFactory = jobDao::pagingSource,
    ).flow.map { pagingData->
        pagingData.map(JobEntity::toDto)
    }

    private val _authData = MutableLiveData<AuthModel>()
    override val authData: LiveData<AuthModel>
        get() = _authData

    override suspend fun getInitial() {
        try {
            val response = apiService.getJobs()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            if (jobDao.isEmpty()) {
                jobDao.insert(body.toEntityInitial())

            } else {
                jobDao.insert(body.toEntity())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(job: Job) {
        try {
            val response = apiService.saveJob(job)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            jobDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun getById(id: Long) : Job {
        return jobDao.getById(id).let(JobEntity::toDto)
    }

//    override suspend fun update() {
//        jobDao.update()
//    }
}

