package com.coffeeinjection.message.presentation.activity

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.domain.repository.MessageRepository
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repo: MessageRepository
) : ViewModel() {

    private val _profileUri = MutableStateFlow<Uri?>(null)
    val profileUri : StateFlow<Uri?> = _profileUri

    fun setPhoto(uri: Uri?) {
        _profileUri.value = uri
    }

    /**
     * 메세지 보내기
     */
    fun sendLetter(receiverNickname: String, content: String) {
        viewModelScope.launch {
            runCatching {
                repo.sendLetter(receiverNickname, content)
            }.onSuccess { res ->
                Logger.d("[letter/send] success, id=${res.letterId}")
            }.onFailure { e ->
                Logger.error("[letter/send] fail msg=${e.message} cause=${e.cause}")
            }
        }
    }
}
