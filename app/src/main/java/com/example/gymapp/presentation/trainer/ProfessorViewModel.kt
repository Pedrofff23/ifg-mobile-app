package com.example.gymapp.presentation.trainer

import androidx.lifecycle.ViewModel
import com.example.gymapp.utils.ErrorUtils
import androidx.lifecycle.viewModelScope
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.GroupService
import com.example.gymapp.data.remote.UserService
import com.example.gymapp.data.remote.ProfileService
import com.example.gymapp.domain.model.Announcement
import com.example.gymapp.domain.model.AssignWorkoutRequest
import com.example.gymapp.domain.model.AssignGroupWorkoutRequest
import com.example.gymapp.domain.model.AddGroupMemberRequest
import com.example.gymapp.domain.model.CreateGroupRequest
import android.content.Context
import android.net.Uri
import com.example.gymapp.domain.model.CreateAnnouncementRequest
import com.example.gymapp.domain.model.CreateTemplateRequest
import com.example.gymapp.domain.model.Exercise
import com.example.gymapp.domain.model.UpdateRoleRequest
import com.example.gymapp.domain.model.UpdateStatusRequest
import com.example.gymapp.domain.model.UpdateBlockedRequest
import com.example.gymapp.domain.model.UpdateUserRequest
import com.example.gymapp.domain.model.User
import com.example.gymapp.domain.model.WorkoutTemplate
import com.example.gymapp.domain.model.StudentGroup
import com.example.gymapp.domain.model.WorkoutAssignment
import com.example.gymapp.domain.model.WorkoutSession
import com.example.gymapp.domain.model.AlunoProfile
import com.example.gymapp.domain.model.BodyMeasurement
import com.example.gymapp.domain.model.AlunoStats
import com.example.gymapp.domain.model.ExerciseProgressPoint
import com.example.gymapp.domain.model.Instituto
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
import com.example.gymapp.domain.model.ExerciseCustomMetric
import com.example.gymapp.domain.model.SetExerciseMetricRequest
import com.example.gymapp.domain.model.UpdateAnnouncementRequest
import com.example.gymapp.domain.model.AuditLogEntry
import com.example.gymapp.domain.model.BackgroundJob
import retrofit2.HttpException
import android.util.Log

@HiltViewModel
class ProfessorViewModel @Inject constructor(
	private val erpService: ErpService,
	private val userService: UserService,
	private val groupService: GroupService,
	private val profileService: ProfileService,
	private val tokenManager: TokenManager,
	val themeManager: ThemeManager,
) : ViewModel() {

    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students.asStateFlow()

    private val _allStudents = MutableStateFlow<List<User>>(emptyList())

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

    private val _groups = MutableStateFlow<List<StudentGroup>>(emptyList())
    val groups: StateFlow<List<StudentGroup>> = _groups.asStateFlow()

    private val _selectedWorkoutHubTab = MutableStateFlow(0)
    val selectedWorkoutHubTab: StateFlow<Int> = _selectedWorkoutHubTab.asStateFlow()


    // ---------- Student Detail State ----------
    private val _selectedStudentDetail = MutableStateFlow<User?>(null)
    val selectedStudentDetail: StateFlow<User?> = _selectedStudentDetail.asStateFlow()

    private val _studentProfile = MutableStateFlow<AlunoProfile?>(null)
    val studentProfile: StateFlow<AlunoProfile?> = _studentProfile.asStateFlow()

    private val _studentMeasurements = MutableStateFlow<List<BodyMeasurement>>(emptyList())
    val studentMeasurements: StateFlow<List<BodyMeasurement>> = _studentMeasurements.asStateFlow()

    private val _studentAssignments = MutableStateFlow<List<WorkoutAssignment>>(emptyList())
    val studentAssignments: StateFlow<List<WorkoutAssignment>> = _studentAssignments.asStateFlow()

    private val _studentSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val studentSessions: StateFlow<List<WorkoutSession>> = _studentSessions.asStateFlow()

    private val _studentStats = MutableStateFlow<AlunoStats?>(null)
    val studentStats: StateFlow<AlunoStats?> = _studentStats.asStateFlow()

    // ---------- Student Exercise Progress State ----------
    private val _studentExerciseProgress = MutableStateFlow<Map<String, List<ExerciseProgressPoint>>>(emptyMap())
    val studentExerciseProgress: StateFlow<Map<String, List<ExerciseProgressPoint>>> = _studentExerciseProgress.asStateFlow()

    private val _studentExerciseProgressLoading = MutableStateFlow(false)
    val studentExerciseProgressLoading: StateFlow<Boolean> = _studentExerciseProgressLoading.asStateFlow()

    // ---------- Student Exercise Custom Metrics State ----------
    private val _studentExerciseCustomMetrics = MutableStateFlow<Map<String, ExerciseCustomMetric>>(emptyMap())
    val studentExerciseCustomMetrics: StateFlow<Map<String, ExerciseCustomMetric>> = _studentExerciseCustomMetrics.asStateFlow()

    // ---------- Student Groups State ----------
    private val _studentGroups = MutableStateFlow<List<StudentGroup>>(emptyList())
    val studentGroups: StateFlow<List<StudentGroup>> = _studentGroups.asStateFlow()

    // ---------- Student Active Assignments State ----------
    private val _studentActiveAssignments = MutableStateFlow<Map<String, WorkoutAssignment?>>(emptyMap())
    val studentActiveAssignments: StateFlow<Map<String, WorkoutAssignment?>> = _studentActiveAssignments.asStateFlow()

    // ---------- Announcement type filter ----------
    private val _announcementTypeFilter = MutableStateFlow<String?>(null)
    val announcementTypeFilter: StateFlow<String?> = _announcementTypeFilter.asStateFlow()

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

    fun setSelectedWorkoutHubTab(tab: Int) {
        _selectedWorkoutHubTab.value = tab
    }

    fun updateUserName(fullName: String, instituto: String?) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			val userId = tokenManager.getUserIdSync() ?: return@launch
    			userService.updateProfile(userId, UpdateUserRequest(fullName = fullName, instituto = instituto))
    			_userName.value = fullName
    			tokenManager.saveUserName(fullName)
    			_successMessage.value = "Nome atualizado com sucesso!"
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    // ==================== DASHBOARD ====================

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val usersResponse = userService.getUsers(limit = 100)
                val studentsList = (usersResponse.data ?: emptyList()).filter { it.role.equals("aluno", ignoreCase = true) }
                _allStudents.value = studentsList
                _students.value = studentsList

                _templates.value = erpService.getTemplates(withWorkoutDays = true).data ?: emptyList()
                _exercises.value = erpService.getExercises().data ?: emptyList()
                _announcements.value = erpService.getAnnouncements().data ?: emptyList()

            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                _templates.value = erpService.getTemplates(
                    withWorkoutDays = true
                ).data ?: emptyList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                val mappedRequest = request.copy(
                    type = mapWorkoutType(request.type),
                    difficulty = mapDifficulty(request.difficulty)
                )
                erpService.createTemplate(mappedRequest)
                _successMessage.value = "Treino criado com sucesso"
                loadTemplates()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                val response = erpService.deleteTemplate(id)
                if (!response.isSuccessful) {
                    throw Exception("Erro ao excluir treino: ${response.code()}")
                }
                _successMessage.value = "Treino excluído com sucesso"
                loadTemplates()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
    _error.value = ErrorUtils.parseErrorMessage(e)
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
    _error.value = ErrorUtils.parseErrorMessage(e)
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
        videoUrls: List<String>,
        fileUris: List<Uri>
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
                val videoUrlPart = null

                val videoUrlsParts = videoUrls.map { url ->
                    MultipartBody.Part.createFormData("video_urls", url)
                }

                val fileParts = mutableListOf<MultipartBody.Part>()
                fileUris.forEach { uri ->
                    val fileData = getFileFromUri(context, uri)
                    if (fileData != null) {
                        val (file, mimeType) = fileData
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        fileParts.add(MultipartBody.Part.createFormData("files", file.name, requestFile))
                    }
                }

                erpService.createExercise(
                    name = namePart,
                    description = descPart,
                    muscleGroup = musclePart,
                    usesWeight = weightPart,
                    videoUrl = videoUrlPart,
                    videoUrls = if (videoUrlsParts.isNotEmpty()) videoUrlsParts else null,
                    files = if (fileParts.isNotEmpty()) fileParts else null
                )
                _successMessage.value = "Exercício criado com sucesso"
                loadExercises()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
        keepMediaIds: List<String>,
        newVideoUrls: List<String>,
        newFileUris: List<Uri>
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
                val videoUrlPart = null
                val mediaPathPart = null
                val mediaTypePart = null

                val keepMediaIdsParts = keepMediaIds.map { mediaId ->
                    MultipartBody.Part.createFormData("keep_media_ids", mediaId)
                }

                val videoUrlsParts = newVideoUrls.map { url ->
                    MultipartBody.Part.createFormData("video_urls", url)
                }

                val fileParts = mutableListOf<MultipartBody.Part>()
                newFileUris.forEach { uri ->
                    val fileData = getFileFromUri(context, uri)
                    if (fileData != null) {
                        val (file, mimeType) = fileData
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        fileParts.add(MultipartBody.Part.createFormData("files", file.name, requestFile))
                    }
                }

                erpService.updateExercise(
                    id = id,
                    name = namePart,
                    description = descPart,
                    muscleGroup = musclePart,
                    usesWeight = weightPart,
                    videoUrl = videoUrlPart,
                    mediaPath = mediaPathPart,
                    mediaType = mediaTypePart,
                    keepMediaIds = if (keepMediaIdsParts.isNotEmpty()) keepMediaIdsParts else null,
                    videoUrls = if (videoUrlsParts.isNotEmpty()) videoUrlsParts else null,
                    files = if (fileParts.isNotEmpty()) fileParts else null
                )
                _successMessage.value = "Exercício atualizado com sucesso"
                loadExercises()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): Pair<File, String>? {
        return try {
            val contentResolver = context.contentResolver
            var extension = ".tmp"
            var mimeType = "application/octet-stream"

            try {
                val type = contentResolver.getType(uri)
                if (type != null) {
                    mimeType = type
                    val mimeExt = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
                    if (mimeExt != null) {
                        extension = ".$mimeExt"
                    }
                }
            } catch (e: Exception) {
                Log.e("GymApp/Error", "Failed to get MIME type", e)
            }

            if (extension == ".tmp") {
                try {
                    val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
                    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val name = cursor.getString(0)
                            if (name != null) {
                                val lastDot = name.lastIndexOf('.')
                                if (lastDot != -1) {
                                    extension = name.substring(lastDot)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GymApp/Error", "Failed to query metadata", e)
                }
            }

            val inputStream = contentResolver.openInputStream(uri)
                ?: throw Exception("Não foi possível abrir o arquivo.")

            val dir = context.cacheDir ?: context.filesDir
            val tempFile = File(dir, "upload_${System.currentTimeMillis()}${extension.replace("/", "_")}")

            inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            Pair(tempFile, mimeType)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Falha ao processar o arquivo de mídia.")
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

    private fun mapWorkoutType(type: String): String {
        return when (type.lowercase()) {
            "força" -> "forca"
            "hipertrofia" -> "hipertrofia"
            "resistência" -> "resistencia"
            "funcional" -> "funcional"
            "cardio" -> "resistencia"
            else -> "hipertrofia"
        }
    }

    private fun mapDifficulty(difficulty: String): String {
        return when (difficulty.lowercase()) {
            "iniciante" -> "iniciante"
            "intermediário", "intermediario" -> "intermediario"
            "avançado", "avancado" -> "avancado"
            else -> "iniciante"
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
    _error.value = ErrorUtils.parseErrorMessage(e)
    } finally {
    _isLoading.value = false
    }
    }
    }

    // ==================== ANNOUNCEMENTS ====================

    fun loadAnnouncements(type: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _announcements.value = erpService.getAnnouncements(type = type).data ?: emptyList()
                _announcementTypeFilter.value = type
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setAnnouncementTypeFilter(type: String?) {
        loadAnnouncements(type)
    }

    fun createAnnouncement(request: CreateAnnouncementRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                erpService.createAnnouncement(request)
                _successMessage.value = "Aviso criado com sucesso"
                loadAnnouncements(_announcementTypeFilter.value)
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                _successMessage.value = "Aviso excluído com sucesso"
                loadAnnouncements(_announcementTypeFilter.value)
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAnnouncement(id: String, request: UpdateAnnouncementRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                erpService.updateAnnouncement(id, request)
                _successMessage.value = "Aviso atualizado com sucesso"
                loadAnnouncements(_announcementTypeFilter.value)
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                _successMessage.value = "Treino atribuído com sucesso"
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
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
                val usersResponse = userService.getUsers(limit = 100)
                val studentsList = (usersResponse.data ?: emptyList()).filter { it.role.equals("aluno", ignoreCase = true) }
                _allStudents.value = studentsList
                _students.value = studentsList
                
                // Concurrently load active assignment for each student
                val assignmentsMap = mutableMapOf<String, WorkoutAssignment?>()
                studentsList.forEach { student ->
                    launch {
                        try {
                            val currentResp = erpService.getCurrentAssignment(student.id)
                            synchronized(assignmentsMap) {
                                assignmentsMap[student.id] = currentResp.data
                            }
                            _studentActiveAssignments.value = assignmentsMap.toMap()
                        } catch (e: Exception) {
                            // ignore individual loading failures
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun searchStudents(query: String) {
    if (query.isBlank()) {
    _students.value = _allStudents.value
    return
    }
    val lowerQuery = query.lowercase()
    _students.value = _allStudents.value.filter { student ->
    (student.fullName ?: "").lowercase().contains(lowerQuery) ||
    student.email.lowercase().contains(lowerQuery)
    }
    }

    // ==================== ADMIN ====================

    fun loadAllUsers() {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    _allUsers.value = userService.getUsers(limit = 100).data ?: emptyList()
    } catch (e: Exception) {
    _error.value = ErrorUtils.parseErrorMessage(e)
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
    userService.updateRole(userId, UpdateRoleRequest(newRole))
    _successMessage.value = "Papel atualizado com sucesso"
    loadAllUsers()
    } catch (e: Exception) {
    _error.value = ErrorUtils.parseErrorMessage(e)
    } finally {
    _isLoading.value = false
    }
    }
    }

    fun updateUserBlocked(userId: String, isBlocked: Boolean) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			userService.updateBlocked(userId, UpdateBlockedRequest(isBlocked))
    			_successMessage.value = "Status de bloqueio atualizado com sucesso"
    			loadAllUsers()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    // ==================== ADMIN (Audit & Jobs) ====================

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _backgroundJobs = MutableStateFlow<List<BackgroundJob>>(emptyList())
    val backgroundJobs: StateFlow<List<BackgroundJob>> = _backgroundJobs.asStateFlow()

    fun loadAuditLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _auditLogs.value = erpService.getAuditLogs(limit = 50).data ?: emptyList()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBackgroundJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _backgroundJobs.value = erpService.getBackgroundJobs()
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== GROUPS ====================

    fun loadGroups() {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			_groups.value = groupService.getGroups(withUsers = true, with = "assignments").data ?: emptyList()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    fun createGroup(name: String, description: String?) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			groupService.createGroup(CreateGroupRequest(name, description))
    			_successMessage.value = "Grupo criado com sucesso"
    			loadGroups()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    fun updateGroup(groupId: String, name: String, description: String?) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			groupService.updateGroup(groupId, CreateGroupRequest(name, description))
    			_successMessage.value = "Grupo atualizado com sucesso"
    			loadGroups()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    fun deleteGroup(groupId: String) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			groupService.deleteGroup(groupId)
    			_successMessage.value = "Grupo excluído com sucesso"
    			loadGroups()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    fun addGroupMember(groupId: String, userId: String) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			groupService.addMember(groupId, AddGroupMemberRequest(userId))
    			_successMessage.value = "Membro adicionado com sucesso"
    			loadGroups()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    fun removeGroupMember(groupId: String, userId: String) {
    	viewModelScope.launch {
    		_isLoading.value = true
    		_error.value = null
    		try {
    			groupService.removeMember(groupId, userId)
    			_successMessage.value = "Membro removido com sucesso"
    			loadGroups()
    		} catch (e: Exception) {
    			_error.value = ErrorUtils.parseErrorMessage(e)
    		} finally {
    			_isLoading.value = false
    		}
    	}
    }

    fun fetchGroupDetail(groupId: String, onResult: (StudentGroup?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = groupService.getGroup(groupId, with = "assignments")
                onResult(response.data)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    // ==================== STUDENT DETAIL ====================
    fun loadStudentDetail(studentId: String) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    val userResp = userService.getUser(studentId)
    _selectedStudentDetail.value = userResp.data

    try {
    val profileResp = profileService.getProfile(studentId)
    _studentProfile.value = profileResp.data
    val measResp = profileService.getMeasurements(studentId)
    _studentMeasurements.value = measResp.data ?: emptyList()
    } catch (e: Exception) {
    _studentProfile.value = null
    _studentMeasurements.value = emptyList()
    }

    val assignResp = erpService.getAssignmentsByAluno(studentId)
    _studentAssignments.value = assignResp.data ?: emptyList()

    val sessionsResp = erpService.getSessionsByAluno(studentId)
    _studentSessions.value = sessionsResp.data ?: emptyList()

    try {
        val allGroups = groupService.getGroups(withUsers = true).data ?: emptyList()
        _studentGroups.value = allGroups.filter { group ->
            group.members?.any { it.userId == studentId } == true
        }
    } catch (e: Exception) {
        Log.e("GymApp/Error", "Failed to load student groups", e)
        _studentGroups.value = emptyList()
    }

    try {
    val statsResp = erpService.getAlunoStats(studentId)
    _studentStats.value = statsResp.data
    } catch (e: Exception) {
    _studentStats.value = null
    }
    } catch (e: Exception) {
    _error.value = ErrorUtils.parseErrorMessage(e)
    } finally {
    _isLoading.value = false
    }
    }
    }

    // ==================== STUDENT EXERCISE PROGRESS ====================

    /**
    * Loads exercise progress data for a specific student's exercises.
    * Results are stored in [studentExerciseProgress] map keyed by exercise ID.
    * Clears previous progress data when loading for a new student.
    */
    fun loadStudentExerciseProgress(exerciseIds: List<String>) {
    viewModelScope.launch {
    _studentExerciseProgressLoading.value = true
    try {
    val newMap = mutableMapOf<String, List<ExerciseProgressPoint>>()
    for (id in exerciseIds) {
    try {
    val response = erpService.getExerciseProgress(id)
    newMap[id] = response.data ?: emptyList()
    } catch (_: Exception) {
    newMap[id] = emptyList()
    }
    }
    _studentExerciseProgress.value = newMap
    loadExerciseCustomMetrics(exerciseIds)
    } finally {
    _studentExerciseProgressLoading.value = false
    }
    }
    }

    /** Load custom metric preferences for given exercise IDs */
    fun loadExerciseCustomMetrics(exerciseIds: List<String>) {
        viewModelScope.launch {
            val newMap = mutableMapOf<String, ExerciseCustomMetric>()
            for (id in exerciseIds) {
                try {
                    val resp = erpService.getExerciseMetric(id)
                    resp.data?.let { newMap[id] = it }
                } catch (e: HttpException) {
                    if (e.code() != 404) {
                        // ignore other errors
                    }
                } catch (_: Exception) {
                    // ignore
                }
            }
            if (newMap.isNotEmpty()) {
                _studentExerciseCustomMetrics.value = _studentExerciseCustomMetrics.value + newMap
            }
        }
    }

    /** Set custom metric for an exercise and refresh state */
    fun setExerciseMetric(exerciseId: String, metricType: String) {
        viewModelScope.launch {
            try {
                erpService.setExerciseMetric(SetExerciseMetricRequest(exerciseId = exerciseId, metricType = metricType))
                loadExerciseCustomMetrics(listOf(exerciseId))
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            }
        }
    }

    /** Delete custom metric for an exercise */
    fun deleteExerciseMetric(exerciseId: String) {
        viewModelScope.launch {
            try {
                erpService.deleteExerciseMetric(exerciseId)
                val updated = _studentExerciseCustomMetrics.value.toMutableMap()
                updated.remove(exerciseId)
                _studentExerciseCustomMetrics.value = updated
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            }
        }
    }

    // ==================== INSTITUTOS ====================
    private val _institutos = MutableStateFlow<List<Instituto>>(emptyList())
    val institutos: StateFlow<List<Instituto>> = _institutos.asStateFlow()

    fun loadInstitutos() {
        viewModelScope.launch {
            try {
                val response = erpService.getInstitutos(limit = 100)
                _institutos.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _error.value = "Erro ao carregar institutos: ${e.message}"
            }
        }
    }

    fun updateStudentInstituto(studentId: String, newInstitutoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentStudentName = _selectedStudentDetail.value?.fullName ?: "Aluno"
                userService.updateProfile(
                    studentId,
                    UpdateUserRequest(
                        fullName = currentStudentName,
                        instituto = newInstitutoId
                    )
                )
                _successMessage.value = "Instituto do aluno atualizado com sucesso!"
                loadStudentDetail(studentId)
            } catch (e: Exception) {
                _error.value = ErrorUtils.parseErrorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearStudentExerciseProgress() {
        _studentExerciseProgress.value = emptyMap()
    }

    // ==================== GROUP ASSIGNMENTS ====================

    fun assignWorkoutToGroup(request: AssignGroupWorkoutRequest) {
    viewModelScope.launch {
    _isLoading.value = true
    _error.value = null
    try {
    erpService.assignWorkoutToGroup(request)
    _successMessage.value = "Treino atribuído ao grupo com sucesso"
    } catch (e: Exception) {
    _error.value = ErrorUtils.parseErrorMessage(e)
    } finally {
    _isLoading.value = false
    }
    }
    }
    }

