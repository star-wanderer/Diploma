package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun update()
    suspend fun authenticate(login: String, password: String)
    suspend fun saveWithAttachment(file: File, post: Post)
}

