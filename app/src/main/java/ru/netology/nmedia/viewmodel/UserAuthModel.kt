package ru.netology.nmedia.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class UserAuthModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private var creds: CredsModel = CredsModel()

    private val _authDataState = MutableLiveData<AuthModelState>()
    val authDataState: LiveData<AuthModelState>
        get() = _authDataState

    val authData: LiveData<AuthModel> = repository.authData

    fun authenticate() {
        viewModelScope.launch {
            try {
                _authDataState.value = AuthModelState(authenticating = true)
                repository.authenticate(creds.login, creds.password)
                _authDataState.value = AuthModelState()
            } catch (e: Exception) {
                _authDataState.value = AuthModelState(error = true)
            }
        }
    }

    fun saveCreds(userCreds: CredsModel) {
        creds = userCreds
    }
}
