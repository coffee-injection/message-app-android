package com.coffeeinjection.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.domain.repository.MessageRepository
import com.coffeeinjection.message.presentation.home.SeaMessageUiModel
import com.coffeeinjection.message.presentation.home.SeaZone
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MessageRepository
): ViewModel() {

    private val _seaMessages = MutableStateFlow<List<SeaMessageUiModel>>(emptyList())
    val seaMessages: StateFlow<List<SeaMessageUiModel>> = _seaMessages.asStateFlow()


    /**
     * 받은 메세지 → 바다 위 병 아이콘 리스트로 변환
     */
    fun loadReceivedMessages() {
        viewModelScope.launch {
            try {
                val list = repo.fetchLetterList()
                Logger.d("HomeViewModel", "[letter/list] size = ${list.size}")

                // 랜덤으로 존 배정 (나중에 우선순위/상태에 따라 바꿔도 됨)
                val random = Random(System.currentTimeMillis())
                val uiList = list.map { letter ->
                    val zone = when (random.nextInt(3)) {
                        0 -> SeaZone.SHALLOW
                        1 -> SeaZone.MIDDLE
                        else -> SeaZone.DEEP
                    }
                    SeaMessageUiModel(
                        letterId = letter.letterId,
                        zone = zone,
                        content = letter.content,
                        senderName = letter.senderName
                    )
                }

                _seaMessages.value = uiList

            } catch (e: Exception) {
                Logger.e("HomeViewModel", "loadReceivedMessages error : ${e.message}")
            }
        }
    }


}