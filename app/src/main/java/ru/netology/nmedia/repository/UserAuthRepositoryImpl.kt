package ru.netology.nmedia.repository

import androidx.lifecycle.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.*
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : UserAuthRepository {

    private val _authData = MutableLiveData<AuthModel>()
    override val authData: LiveData<AuthModel>
        get() = _authData

    override fun unAuthenticate() {
        _authData.value = AuthModel()
    }

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
    override suspend fun register(name: String, login: String, password: String , file: File?) {
        try {
            val response = if (file == null){
                 apiService.register(
                    login,
                    password,
                    name,)
            } else{
                apiService.register(
                    login.toRequestBody("text/plain".toMediaType()),
                    password.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody()
                    ))
            }

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
}
