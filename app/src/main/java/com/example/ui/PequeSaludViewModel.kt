package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChildProfile
import com.example.data.HealthRecord
import com.example.data.PequeSaludRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

class PequeSaludViewModel(
    private val repository: PequeSaludRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedChildId = MutableStateFlow("")
    val selectedChildId: StateFlow<String> = _selectedChildId.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    val childrenState: StateFlow<DataUiState<List<ChildProfile>>> = repository.observeChildren(currentUserId)
        .onEach { list ->
            if (list.isEmpty()) {
                _selectedChildId.value = ""
            } else if (list.none { it.id == _selectedChildId.value }) {
                _selectedChildId.value = list.first().id
            }
        }
        .map<List<ChildProfile>, DataUiState<List<ChildProfile>>> { DataUiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing children", e)
            emit(DataUiState.Error(e.localizedMessage ?: "Error al cargar los perfiles de tus hijos"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    val recordsState: StateFlow<DataUiState<List<HealthRecord>>> = repository.observeRecords(currentUserId)
        .map<List<HealthRecord>, DataUiState<List<HealthRecord>>> { DataUiState.Success(it) }
        .catch { e ->
            Log.w(TAG, "Error observing records", e)
            emit(DataUiState.Error(e.localizedMessage ?: "Error al cargar los registros clínicos"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DataUiState.Loading
        )

    init {
        viewModelScope.launch {
            try {
                repository.purgePreseededDemoData()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to purge demo data", e)
            }
        }
    }

    fun selectChild(childId: String) {
        _selectedChildId.value = childId
    }

    fun clearMessages() {
        _actionError.value = null
        _actionMessage.value = null
    }

    fun addChild(profile: ChildProfile) {
        viewModelScope.launch {
            val result = repository.createChild(profile)
            result.onSuccess { newId ->
                _selectedChildId.value = newId
                _actionMessage.value = "Perfil de ${profile.name} registrado correctamente"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo guardar el perfil"
            }
        }
    }

    fun updateChild(profile: ChildProfile) {
        viewModelScope.launch {
            val result = repository.updateChild(profile)
            result.onSuccess {
                _actionMessage.value = "Perfil de ${profile.name} actualizado"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo actualizar el perfil"
            }
        }
    }

    fun deleteChild(childId: String) {
        viewModelScope.launch {
            val result = repository.deleteChild(childId)
            result.onSuccess {
                _actionMessage.value = "Perfil eliminado correctamente"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo eliminar el perfil"
            }
        }
    }

    fun addRecord(record: HealthRecord) {
        viewModelScope.launch {
            val result = repository.createRecord(record)
            result.onSuccess {
                _actionMessage.value = "Registro \"${record.title}\" guardado"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo guardar el registro"
            }
        }
    }

    fun toggleRecordStatus(record: HealthRecord) {
        viewModelScope.launch {
            val nextStatus = when (record.status) {
                "Pendiente" -> "Aplicada"
                "Aplicada" -> "Pendiente"
                "Programada" -> "Completada"
                "Activo" -> "Completado"
                else -> "Aplicada"
            }
            val result = repository.updateRecordStatus(record.id, nextStatus)
            result.onSuccess {
                _actionMessage.value = "${record.title}: marcado como $nextStatus"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo actualizar el estado"
            }
        }
    }

    fun replyOrUpdateRecord(
        recordId: String,
        newStatus: String,
        newNotes: String,
        successMessage: String = "Respuesta guardada y enviada a los padres"
    ) {
        viewModelScope.launch {
            val result = repository.updateRecordNotesAndStatus(recordId, newStatus, newNotes)
            result.onSuccess {
                _actionMessage.value = successMessage
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo actualizar el registro"
            }
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            val result = repository.deleteRecord(recordId)
            result.onSuccess {
                _actionMessage.value = "Registro eliminado"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudo eliminar el registro"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val result = repository.clearAllUserData()
            result.onSuccess {
                _selectedChildId.value = ""
                _actionMessage.value = "Expediente reiniciado en blanco, listo para ingresar nuevos datos"
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "No se pudieron limpiar los datos"
            }
        }
    }

    private companion object {
        const val TAG = "PequeSaludVM"
    }
}
