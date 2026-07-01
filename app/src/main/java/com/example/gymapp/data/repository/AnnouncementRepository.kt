package com.example.gymapp.data.repository

import com.example.gymapp.data.local.AnnouncementDao
import com.example.gymapp.data.local.LocalAnnouncement
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.domain.model.Announcement
import com.example.gymapp.utils.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface AnnouncementRepository {
    suspend fun loadAnnouncements(institutoId: String): List<Announcement>
    fun observeAnnouncements(institutoId: String): Flow<List<Announcement>>
    suspend fun refreshAnnouncements(institutoId: String)
}

@Singleton
class AnnouncementRepositoryImpl @Inject constructor(
    private val erpService: ErpService,
    private val announcementDao: AnnouncementDao,
    private val networkMonitor: NetworkMonitor
) : AnnouncementRepository {

    override suspend fun loadAnnouncements(institutoId: String): List<Announcement> {
        val isOnline = networkMonitor.isCurrentlyOnline()
        
        if (isOnline) {
            try {
                val response = erpService.getAnnouncements(limit = 100)
                val announcements = response.data ?: emptyList()
                if (announcements.isNotEmpty()) {
                    cacheAnnouncements(institutoId, announcements)
                }
                return announcements
            } catch (e: Exception) {
                // Fall through to local cache
            }
        }
        
        // Return cached announcements
        return getCachedAnnouncements(institutoId)
    }

    override fun observeAnnouncements(institutoId: String): Flow<List<Announcement>> {
        return announcementDao.getByInstitutoFlow(institutoId)
            .map { localList ->
                localList.map { local ->
                    Announcement(
                        id = local.id,
                        title = local.title,
                        content = local.content,
                        type = local.type,
                        instituto = local.instituto,
                        authorId = local.authorId,
                        publishedAt = local.publishedAt,
                        createdAt = local.createdAt,
                        updatedAt = local.updatedAt,
                        authorName = local.authorName
                    )
                }
            }
    }

    override suspend fun refreshAnnouncements(institutoId: String) {
        val isOnline = networkMonitor.isCurrentlyOnline()
        if (!isOnline) return
        
        try {
            val response = erpService.getAnnouncements(limit = 100)
            val announcements = response.data ?: emptyList()
            if (announcements.isNotEmpty()) {
                cacheAnnouncements(institutoId, announcements)
            }
        } catch (e: Exception) {
            // Silently fail - local cache will be used
        }
    }

    private suspend fun cacheAnnouncements(institutoId: String, announcements: List<Announcement>) {
        val localAnnouncements = announcements
            .filter { it.instituto == institutoId }
            .map { LocalAnnouncement(
                id = it.id,
                title = it.title,
                content = it.content,
                type = it.type ?: "",
                instituto = it.instituto,
                authorId = it.authorId,
                publishedAt = it.publishedAt,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                authorName = it.authorName
            )}
        announcementDao.insertAll(localAnnouncements)
    }

    private suspend fun getCachedAnnouncements(institutoId: String): List<Announcement> {
        val local = announcementDao.getByInstituto(institutoId)
        return local.map { Announcement(
            id = it.id,
            title = it.title,
            content = it.content,
            type = it.type,
            instituto = it.instituto,
            authorId = it.authorId,
            publishedAt = it.publishedAt,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            authorName = it.authorName
        )}
    }
}