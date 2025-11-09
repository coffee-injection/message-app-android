package com.coffeeinjection.message.presentation.activity

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {

    private val _profileUri = MutableStateFlow<Uri?>(null)
    val profileUri : StateFlow<Uri?> = _profileUri

    fun setPhoto(uri: Uri?) {
        _profileUri.value = uri
    }
}