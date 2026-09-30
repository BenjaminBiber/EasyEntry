package com.easyentry.app.data.remote.api

import com.easyentry.app.data.remote.dto.EspControlDto
import com.easyentry.app.data.remote.dto.EspRenameDto
import com.easyentry.app.data.remote.dto.EspStatusDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface EspApi {

    @GET
    suspend fun getStatus(@Url url: String): EspStatusDto

    /**
     * Erreichbarkeitspruefung. Der Body wird bewusst NICHT deserialisiert.
     *
     * [getStatus] verlangt ein vollstaendig passendes [EspStatusDto] mit non-null Feldern; ein
     * abgeschnittener Body oder ein abweichender Firmware-Stand haette ein antwortendes Geraet
     * als nicht erreichbar gemeldet. Jede HTTP-Antwort - auch 404 oder 500 - beweist dagegen,
     * dass der Server laeuft.
     *
     * Kein @HEAD: der ESP-Sketch registriert nur GET, PUT und POST.
     */
    @GET
    suspend fun ping(@Url url: String): Response<Unit>

    @PUT
    suspend fun controlDoor(@Url url: String, @Body body: EspControlDto): ResponseBody

    @POST
    suspend fun renameDevice(@Url url: String, @Body body: EspRenameDto): ResponseBody
}
