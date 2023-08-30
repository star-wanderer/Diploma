package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.*
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class JobRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val jobDao: JobDao,
    private val jobRemoteKeyDao: JobRemoteKeyDao,
) : RemoteMediator<Int, JobEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, JobEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    service.getJobs()
                }
                LoadType.PREPEND -> {
                   state.firstItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getJobs()
                }
                LoadType.APPEND -> {
                   state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getJobs()
                }
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

        db.withTransaction {
            when(loadType){
                LoadType.REFRESH-> {
                    jobRemoteKeyDao.removeAll()
                    jobRemoteKeyDao.insert(
                        listOf(
                            JobRemoteKeyEntity(
                                type = JobRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id
                            ),
                            JobRemoteKeyEntity(
                                type = JobRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id
                            ),
                        )
                    )
                    jobDao.removeAll()
                }
                LoadType.PREPEND->{
                    jobRemoteKeyDao.insert(
                        JobRemoteKeyEntity(
                            type = JobRemoteKeyEntity.KeyType.AFTER,
                            id = body.first().id
                        )
                    )
                }
                LoadType.APPEND->{
                    jobRemoteKeyDao.insert(
                        JobRemoteKeyEntity(
                            type = JobRemoteKeyEntity.KeyType.BEFORE,
                            id = body.last().id
                        )
                    )
                }
            }
        }
            jobDao.insert(body.toEntity())
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())

        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}