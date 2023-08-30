package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.*
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val userDao: UserDao,
    private val userRemoteKeyDao: UserRemoteKeyDao,
) : RemoteMediator<Int, UserEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    service.getUsers()
                }
                LoadType.PREPEND -> {
                    service.getUsers()
                }
                LoadType.APPEND -> {
                    service.getUsers()
                }
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

        db.withTransaction {
            when(loadType){
                LoadType.REFRESH-> {
                    userRemoteKeyDao.removeAll()
                    userRemoteKeyDao.insert(
                        listOf(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id
                            ),
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id
                            ),
                        )
                    )
                   userDao.removeAll()
                }
                LoadType.PREPEND->{
                    userRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            type = PostRemoteKeyEntity.KeyType.AFTER,
                            id = body.first().id
                        )
                    )
                }
                LoadType.APPEND->{
                    userRemoteKeyDao.insert(
                        PostRemoteKeyEntity(
                            type = PostRemoteKeyEntity.KeyType.BEFORE,
                            id = body.last().id
                        )
                    )
                }
            }
        }
            userDao.insert(body.toEntity())
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())

        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}