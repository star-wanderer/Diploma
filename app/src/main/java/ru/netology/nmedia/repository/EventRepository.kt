package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.model.AuthModel
import java.io.File

interface EventRepository {
    val authData: LiveData<AuthModel>
    val data: Flow<PagingData<FeedItem>>
    suspend fun getInitial()
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(event: Event)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun update()
    suspend fun saveWithAttachment(file: File, event: Event)
}

