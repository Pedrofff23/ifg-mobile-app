package com.example.gymapp.data.repository

import com.example.gymapp.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val repository: WorkoutSessionRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        networkMonitor.isOnline
            .onEach { isOnline ->
                if (isOnline) {
                    scope.launch {
                        repository.syncPendingActions()
                    }
                }
            }
            .launchIn(scope)
    }

    fun triggerSync() {
        scope.launch {
            repository.syncPendingActions()
        }
    }
}
