package com.example.gymapp.presentation.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.CreateInstitutoRequest
import com.example.gymapp.domain.model.Instituto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminInstitutoViewModel @Inject constructor(
    private val erpService: ErpService
) : ViewModel() {

    private val _institutos = MutableStateFlow<List<Instituto>>(emptyList())
    val institutos: StateFlow<List<Instituto>> = _institutos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadInstitutos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = erpService.getInstitutos(limit = 100)
                _institutos.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = "Erro ao carregar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createInstituto(name: String) {
        viewModelScope.launch {
            try {
                erpService.createInstituto(CreateInstitutoRequest(name))
                loadInstitutos()
            } catch (e: Exception) {
                _error.value = "Erro ao criar: ${e.message}"
            }
        }
    }

    fun updateInstituto(id: String, name: String) {
        viewModelScope.launch {
            try {
                erpService.updateInstituto(id, CreateInstitutoRequest(name))
                loadInstitutos()
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar: ${e.message}"
            }
        }
    }

    fun deleteInstituto(id: String) {
        viewModelScope.launch {
            try {
                erpService.deleteInstituto(id)
                loadInstitutos()
            } catch (e: Exception) {
                _error.value = "Erro ao excluir: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
