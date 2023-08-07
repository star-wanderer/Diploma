package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.UserAuthRepository
import javax.inject.Inject

@HiltViewModel
class UserAuthViewModel @Inject constructor(
    private val repository: UserAuthRepository,
) : ViewModel() {

    private var authCreds = AuthCredsModel()
    private var regCreds = RegCredsModel()

    private val _authDataState = MutableLiveData<AuthModelState>()
    val authDataState: LiveData<AuthModelState>
        get() = _authDataState

    private val _photoState = MutableLiveData<PhotoModel?>()
    val photoState: LiveData<PhotoModel?>
        get() = _photoState

    val authData: LiveData<AuthModel> = repository.authData

    fun authenticate() {
        viewModelScope.launch {
            try {
                _authDataState.value = AuthModelState(authenticating = true)
                repository.authenticate(authCreds.login, authCreds.password)
                _authDataState.value = AuthModelState()
            } catch (e: Exception) {
                _authDataState.value = AuthModelState(error = true)
            }
        }
    }

    fun unAuthenticate() {
        repository.unAuthenticate()
    }

    fun saveAuthCreds(userCreds: AuthCredsModel) {
        authCreds = userCreds
    }

    fun saveRegCreds(regCreds: RegCredsModel) {
        this.regCreds = regCreds
    }

    fun register() {
        viewModelScope.launch {
            try {
                _authDataState.value = AuthModelState(registering = true)
                _photoState.value?.let {
                    repository.register(
                            regCreds.name,
                            regCreds.login,
                            regCreds.password,
                            it.file
                    )
                }
                _authDataState.value = AuthModelState()
            } catch (e: Exception) {
                _authDataState.value = AuthModelState(error = true)
            }
        }
    }

    fun changePhoto(photoModel: PhotoModel?) {
        _photoState.value = photoModel
    }
}
