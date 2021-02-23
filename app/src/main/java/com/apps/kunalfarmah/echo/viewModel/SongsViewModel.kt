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
    private val _isDataReady: MutableLiveData<Boolean> = MutableLiveData()

    val songsList: MutableLiveData<List<Songs>>
        get() = _songsList

    val isDataReady: MutableLiveData<Boolean>
        get() = _isDataReady

    lateinit var list: List<Songs>


    fun init() {
        viewModelScope.launch {
            songsRepository.fetchSongs()
        }.invokeOnCompletion { isDataReady.value = true }
    }

    fun getAllSongs() {
        viewModelScope.launch {
            list = songsRepository.getAllSongs()
        }.invokeOnCompletion { songsList.value = list }
    }
}