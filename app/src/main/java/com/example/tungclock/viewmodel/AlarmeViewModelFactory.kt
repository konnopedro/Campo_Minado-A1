package com.example.tungclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tungclock.data.repository.AlarmeRepository

class AlarmeViewModelFactory(private val repository: AlarmeRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AlarmeViewModel::class.java))
        return AlarmeViewModel(repository) as T
    }
}
