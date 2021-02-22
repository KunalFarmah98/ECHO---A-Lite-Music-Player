package com.apps.kunalfarmah.echo.viewModel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apps.kunalfarmah.echo.Songs
import com.apps.kunalfarmah.echo.repository.SongsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongsViewModel
@ViewModelInject
constructor(private val songsRepository: SongsRepository,
            @Assisted private val savedSateHandle: SavedStateHandle) : ViewModel() {

    private val _songsList: MutableLiveData<List<Songs>> = MutableLiveData()

    val songsList: MutableLiveData<List<Songs>>
        get() = _songsList


    fun init() {
        viewModelScope.launch {
            songsRepository.fetchSongs()
        }
    }

    fun getAllSongs() {
        viewModelScope.launch {
            _songsList.value = songsRepository.getAllSongs();
        }
    }
}