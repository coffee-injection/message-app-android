package com.coffeeinjection.message.presentation.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeeinjection.message.data.remote.dto.LoadBookmarkResponse
import com.coffeeinjection.message.domain.repository.MessageRepository
import com.coffeeinjection.message.presentation.bookmark.model.BookmarkModel
import com.coffeeinjection.message.presentation.bookmark.model.toUiModel
import com.coffeeinjection.message.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repo: MessageRepository
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<BookmarkModel>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkModel>> = _bookmarks

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun loadBookmarks() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            runCatching { repo.loadBookmarksList() }
                .onSuccess { dtoList ->
                    _bookmarks.value = dtoList.map { it.toUiModel() } // ✅ List<BookmarkModel>
                    Logger.d("[bookmark/list] success size=${dtoList.map { it.toUiModel() }.size}")
                }
                .onFailure { e ->
                    _bookmarks.value = emptyList()
                    _error.value = e.message ?: "북마크 목록 조회에 실패했습니다."
                    Logger.error("[bookmark/list] fail msg=${e.message} cause=${e.cause}")
                }

            _loading.value = false
        }
    }
}
