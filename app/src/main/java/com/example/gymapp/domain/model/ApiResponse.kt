package com.example.gymapp.domain.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val data: T?
)

data class PaginatedResponse<T>(
    val data: List<T>?,
    val meta: PaginationMeta?
)

data class PaginationMeta(
 val total: Int? = null,
 val limit: Int? = null,
 val offset: Int? = null
)
