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
class UserRepositoryImpl @Inject constructor(
    val db: AppDb,
    private val userDao: UserDao,
    private val userRemoteKeyDao: UserRemoteKeyDao,
    private val apiService: ApiService,
) : UserRepository {

    private val pageSize = 5

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = UserRemoteMediator(apiService,db,userDao,userRemoteKeyDao),
        pagingSourceFactory = userDao::pagingSource,
    ).flow.map { pagingData->
        pagingData.map(UserEntity::toDto)
    }

    private val _authData = MutableLiveData<AuthModel>()
    override val authData: LiveData<AuthModel>
        get() = _authData

    override suspend fun getInitial() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            if (userDao.isEmpty()) {
                userDao.insert(body.toEntityInitial())

            } else {
                userDao.insert(body.toEntity())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}

