package com.example.gymapp.presentation.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.UserService
import com.example.gymapp.domain.model.Announcement
import com.example.gymapp.domain.model.AssignWorkoutRequest
import android.content.Context
import android.net.Uri
import com.example.gymapp.domain.model.CreateAnnouncementRequest
import com.example.gymapp.domain.model.CreateTemplateRequest
import com.example.gymapp.domain.model.Exercise
import com.example.gymapp.domain.model.TemplateExerciseInput
import com.example.gymapp.domain.model.UpdateRoleRequest
import com.example.gymapp.domain.model.UpdateStatusRequest
import com.example.gymapp.domain.model.User
import com.example.gymapp.domain.model.WorkoutTemplate
import com.example.gymapp.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfessorViewModel @Inject constructor(
    private val erpService: ErpService,
    private val userService: UserService,
    private val tokenManager: TokenManager,
    val themeManager: ThemeManager,
) : ViewModel() {

    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students.asStateFlow()

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<WorkoutTemplate>> = _templates.asStateFlow()

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    init {
    loadUserName()
    loadUserRole()
    loadDashboardData()
    }

    private fun loadUserName() {
    viewModelScope.launch {
    _userName.value = tokenManager.getUserNameSync()
    }
    }

    private fun loadUserRole() {
    viewModelScope.launch {
    _isAdmin.value = tokenManager.getUserRoleSync().equals("admin", ignoreCase = true)
    }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    // ==================== DASHBOARD ====================

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val usersResponse = userService.getUsers()
                _students.value = (usersResponse.data ?: emptyList()).filter { it.role.equals("aluno", ignoreCase = true) }

                _templates.value = erpService.getTemplates().data ?: emptyList()
                _exercises.value = erpService.getExercises().data ?: emptyList()
                _announcements.value = erpService.getAnnouncements().data ?: emptyList()

            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== TEMPLATES ====================

    fun loadTemplates() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _templates.value = erpService.getTemplates().data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load templates"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTemplate(request: CreateTemplateRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                erpService.createTemplate(request)
                _successMessage.value = "Template created successfully"
                loadTemplates()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create template"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTemplate(id: String) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    erpService.deleteTemplate(id)
    _successMessage.value = "Template excluído com sucesso"
    loadTemplates()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to delete template"
    } finally {
    _isLoading.value = false
    }
    }
    }

    fun updateTemplate(id: String, request: CreateTemplateRequest) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    erpService.updateTemplate(id, request)
    _successMessage.value = "Template atualizado com sucesso"
    loadTemplates()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to update template"
    } finally {
    _isLoading.value = false
    }
    }
    }

    // ==================== EXERCISES ====================

    fun loadExercises(search: String? = null, muscleGroup: String? = null) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    _exercises.value = erpService.getExercises(
    search = search,
    muscleGroup = muscleGroup
    ).data ?: emptyList()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to load exercises"
    } finally {
    _isLoading.value = false
    }
    }
    }

    fun createExercise(
        context: Context,
        name: String,
        description: String?,
        muscleGroup: String,
        usesWeight: Boolean,
        videoUrl: String?,
        fileUri: Uri?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
                val mappedMuscle = mapMuscleGroup(muscleGroup)
                val musclePart = mappedMuscle.toRequestBody("text/plain".toMediaTypeOrNull())
                val weightPart = usesWeight.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val videoUrlPart = videoUrl?.toRequestBody("text/plain".toMediaTypeOrNull())

                var filePart: MultipartBody.Part? = null
                if (fileUri != null) {
                    val file = getFileFromUri(context, fileUri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                        filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    }
                }

                erpService.createExercise(namePart, descPart, musclePart, weightPart, videoUrlPart, filePart)
                _successMessage.value = "Exercício criado com sucesso"
                loadExercises()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create exercise"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExercise(
        id: String,
        context: Context,
        name: String,
        description: String?,
        muscleGroup: String,
        usesWeight: Boolean,
        videoUrl: String?,
        fileUri: Uri?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
                val mappedMuscle = mapMuscleGroup(muscleGroup)
                val musclePart = mappedMuscle.toRequestBody("text/plain".toMediaTypeOrNull())
                val weightPart = usesWeight.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val videoUrlPart = videoUrl?.toRequestBody("text/plain".toMediaTypeOrNull())

                var filePart: MultipartBody.Part? = null
                if (fileUri != null) {
                    val file = getFileFromUri(context, fileUri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                        filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    }
                }

                erpService.updateExercise(id, namePart, descPart, musclePart, weightPart, videoUrlPart, filePart)
                _successMessage.value = "Exercício atualizado com sucesso"
                loadExercises()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update exercise"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun mapMuscleGroup(muscleGroup: String): String {
        return when (muscleGroup.lowercase()) {
            "peito" -> "peito"
            "costas" -> "costas"
            "ombros" -> "ombros"
            "bíceps", "tríceps", "braços" -> "bracos"
            "pernas", "glúteos" -> "pernas"
            "core", "abdômen", "abdomen" -> "abdomen"
            else -> "peito"
        }
    }

    fun deleteExercise(id: String) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    erpService.deleteExercise(id)
    _successMessage.value = "Exercício excluído com sucesso"
    loadExercises()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to delete exercise"
    } finally {
    _isLoading.value = false
    }
    }
    }

    // ==================== ANNOUNCEMENTS ====================

    fun loadAnnouncements() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _announcements.value = erpService.getAnnouncements().data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load announcements"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAnnouncement(request: CreateAnnouncementRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                erpService.createAnnouncement(request)
                _successMessage.value = "Announcement created successfully"
                loadAnnouncements()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to create announcement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                erpService.deleteAnnouncement(id)
                _successMessage.value = "Announcement deleted successfully"
                loadAnnouncements()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete announcement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== ASSIGNMENTS ====================

    fun assignWorkout(request: AssignWorkoutRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                erpService.assignWorkout(request)
                _successMessage.value = "Workout assigned successfully"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to assign workout"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== STUDENTS ====================

    fun loadStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val usersResponse = userService.getUsers()
                _students.value = (usersResponse.data ?: emptyList()).filter { it.role.equals("aluno", ignoreCase = true) }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load students"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchStudents(query: String) {
    val currentStudents = _students.value
    if (query.isBlank()) {
    loadStudents()
    return
    }
    val lowerQuery = query.lowercase()
    _students.value = currentStudents.filter { student ->
    (student.fullName ?: "").lowercase().contains(lowerQuery) ||
    (student.email ?: "").lowercase().contains(lowerQuery)
    }
    }

    // ==================== ADMIN ====================

    fun loadAllUsers() {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    _allUsers.value = userService.getUsers().data ?: emptyList()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to load users"
    } finally {
    _isLoading.value = false
    }
    }
    }

    fun updateUserRole(userId: String, newRole: String) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    userService.updateUserRole(userId, UpdateRoleRequest(newRole))
    _successMessage.value = "Papel atualizado com sucesso"
    loadAllUsers()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to update role"
    } finally {
    _isLoading.value = false
    }
    }
    }

    fun updateUserStatus(userId: String, isActive: Boolean) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    userService.updateUserStatus(userId, UpdateStatusRequest(isActive))
    _successMessage.value = "Status atualizado com sucesso"
    loadAllUsers()
    } catch (e: Exception) {
    _error.value = e.message ?: "Failed to update status"
    } finally {
    _isLoading.value = false
    }
    }
    }
    }
