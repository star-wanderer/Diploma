package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.AuthModel
import java.io.File

interface PostRepository {
    val authData: LiveData<AuthModel>
    val data: Flow<PagingData<FeedItem>>
    suspend fun getInitial()
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun disLikeById(id: Long)
    suspend fun getById(id: Long) : Post
    suspend fun saveWithAttachment(file: File, post: Post)
//    fun getNewerCount(id: Long): Flow<Int>
//    suspend fun update()
}

