package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.CredsModel
import java.io.File

interface UserAuthRepository {
    val authData: LiveData<AuthModel>
    suspend fun authenticate(login: String, password: String)
    fun unAuthenticate()
    suspend fun register(name: String, login: String, password: String, file: File?)
}

