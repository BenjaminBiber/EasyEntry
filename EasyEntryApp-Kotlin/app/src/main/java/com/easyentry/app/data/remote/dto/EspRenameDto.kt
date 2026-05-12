package com.easyentry.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EspRenameDto(
    @Json(name = "Name") val name: String
)
