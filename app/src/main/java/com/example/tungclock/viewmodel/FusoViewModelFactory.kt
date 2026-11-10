package com.example.tungclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tungclock.data.repository.FusoHorarioRepository

class FusoViewModelFactory(private val repository: FusoHorarioRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FusoViewModel::class.java))
        return FusoViewModel(repository) as T
    }
}
