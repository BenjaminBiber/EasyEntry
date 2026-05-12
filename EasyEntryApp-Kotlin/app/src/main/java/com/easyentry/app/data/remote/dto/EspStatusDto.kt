package com.easyentry.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EspStatusDto(
    val name: String,
    @Json(name = "IsOpen") val isOpen: Boolean
)
