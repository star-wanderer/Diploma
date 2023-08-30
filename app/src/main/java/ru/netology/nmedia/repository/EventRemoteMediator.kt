package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.EventDao
import ru.netology.nmedia.dao.EventRemoteKeyDao
import ru.netology.nmedia.dao.EventSpeakerDao
import ru.netology.nmedia.dao.EventUserDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val service: ApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventUserDao: EventUserDao,
    private val eventSpeakerDao: EventSpeakerDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
) : RemoteMediator<Int, EventEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    service.getLatestEvents(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    val item = state.firstItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getAfterEvents(item.id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val item = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBeforeEvents(item.id,state.config.pageSize)
                }
            }
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

        db.withTransaction {
            when(loadType){
                LoadType.REFRESH-> {
                    eventRemoteKeyDao.removeAll()
                    eventRemoteKeyDao.insert(
                        listOf(
                            EventRemoteKeyEntity(
                                type = EventRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id
                            ),
                            EventRemoteKeyEntity(
                                type = EventRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id
                            ),
                        )
                    )
                    eventDao.removeAllEvents()
                    eventUserDao.removeAll()
                    eventSpeakerDao.removeAll()
                }
                LoadType.PREPEND->{
                    eventRemoteKeyDao.insert(
                        EventRemoteKeyEntity(
                            type = EventRemoteKeyEntity.KeyType.AFTER,
                            id = body.first().id
                        )
                    )
                }
                LoadType.APPEND->{
                    eventRemoteKeyDao.insert(
                        EventRemoteKeyEntity(
                            type = EventRemoteKeyEntity.KeyType.BEFORE,
                            id = body.last().id
                        )
                    )
                }
            }
        }
            eventDao.insert(body.toEntity())
            eventUserDao.insert(body.toUserEntity())
            eventSpeakerDao.insert(body.toSpeakerEntity())
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())

        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}