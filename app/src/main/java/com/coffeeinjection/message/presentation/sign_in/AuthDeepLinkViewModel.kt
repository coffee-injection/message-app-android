package com.coffeeinjection.message.presentation.sign_in

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AuthDeepLinkViewModel @Inject constructor() : ViewModel() {

    private val _googleCode = MutableStateFlow<String?>(null)
    val googleCode: StateFlow<String?> = _googleCode.asStateFlow()

    fun setGoogleCode(code: String) {
        _googleCode.value = code
    }

    fun consumeGoogleCode() {
        _googleCode.value = null
    }
}
