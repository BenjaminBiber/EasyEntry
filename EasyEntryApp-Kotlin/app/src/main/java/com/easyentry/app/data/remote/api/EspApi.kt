package com.easyentry.app.data.remote.api

import com.easyentry.app.data.remote.dto.EspControlDto
import com.easyentry.app.data.remote.dto.EspRenameDto
import com.easyentry.app.data.remote.dto.EspStatusDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface EspApi {

    @GET
    suspend fun getStatus(@Url url: String): EspStatusDto

    @PUT
    suspend fun controlDoor(@Url url: String, @Body body: EspControlDto): ResponseBody

    @POST
    suspend fun renameDevice(@Url url: String, @Body body: EspRenameDto): ResponseBody
}
