package com.coffeeinjection.message.data.model

import androidx.lifecycle.MutableLiveData

data class PushEvent(
    val title: String?,
    val body: String?
)

object PushBus {
    val message = MutableLiveData<PushEvent>()
}