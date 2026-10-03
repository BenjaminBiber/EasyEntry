package com.easyentry.app.data.repository

import com.easyentry.app.data.remote.DeviceProbe
import com.easyentry.app.data.remote.EspNetworkConfig
import com.easyentry.app.data.remote.MonotonicClock
import com.easyentry.app.data.remote.ProbeDiagnostics
import com.easyentry.app.data.remote.ProbeTarget
import com.easyentry.app.data.remote.api.EspApi
import com.easyentry.app.data.remote.dto.EspControlDto
import com.easyentry.app.data.remote.dto.EspRenameDto
import com.easyentry.app.data.remote.dto.EspStatusDto
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.DeviceStatus
import com.easyentry.app.domain.model.GroupIcon
import com.easyentry.app.domain.model.Reachability
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceReachabilityRepositoryTest {

    private val config = EspNetworkConfig(
        connectTimeoutMs = 3_000,
        readTimeoutMs = 4_000,
        writeTimeoutMs = 4_000,
        callTimeoutMs = 10_000,
        probeAttempts = 3,
        backoffMs = longArrayOf(0, 300, 900),
    )

    @Test
    fun `result of a probe that waited for the host lock is not discarded`() = runTest {
        // Die Runde laeuft dreimal in einen 3-s-Timeout, danach antwortet das Geraet.
        val api = FakeEspApi { call ->
            if (call < 3) {
                delay(3_000)
                throw SocketTimeoutException("timeout")
            }
        }
        val repo = createRepository(api, device(id = 1, url = "gate"))

        // t=2 s: die Runde haelt den Host-Lock. Nachlauf eines Steuerbefehls stellt sich an.
        advanceTimeBy(2_000)
        runCurrent()
        launch { repo.refreshOne(ProbeTarget(1, "gate")) }.join()

        // Der Nachlauf hat NACH dem Fehlschlag der Runde geprueft, sein ONLINE ist das neueste.
        assertEquals(4, api.pingCalls)
        assertEquals(Reachability.ONLINE, repo.status.value[1]?.state)
    }

    @Test
    fun `refreshIfStale probes again once results are older than the stale limit`() = runTest {
        val api = FakeEspApi { }
        val repo = createRepository(api, device(id = 1, url = "gate"))
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(1, api.pingCalls)

        advanceTimeBy(DeviceReachabilityRepository.STALE_AFTER_MS + 1_000)
        repo.refreshIfStale()
        runCurrent()

        assertEquals(2, api.pingCalls)
    }

    @Test
    fun `refreshIfStale leaves fresh results alone`() = runTest {
        val api = FakeEspApi { }
        val repo = createRepository(api, device(id = 1, url = "gate"))
        advanceTimeBy(1_000)
        runCurrent()

        advanceTimeBy(DeviceReachabilityRepository.STALE_AFTER_MS / 2)
        repo.refreshIfStale()
        runCurrent()

        assertEquals(1, api.pingCalls)
    }

    @Test
    fun `refreshIfStale does not restart a round that is still running`() = runTest {
        // Ein Ping dauert 3 s. Ein Neustart wuerde den laufenden abbrechen und neu pingen.
        val api = FakeEspApi { delay(3_000) }
        val repo = createRepository(api, device(id = 1, url = "gate"))
        advanceTimeBy(1_000)
        runCurrent()

        repo.refreshIfStale()
        advanceTimeBy(10_000)
        runCurrent()

        assertEquals(1, api.pingCalls)
        assertEquals(Reachability.ONLINE, repo.status.value[1]?.state)
    }

    private fun TestScope.createRepository(api: EspApi, vararg devices: Device): DeviceReachabilityRepository {
        val clock = MonotonicClock { testScheduler.currentTime }
        return DeviceReachabilityRepository(
            deviceGroupRepository = FakeGroupRepository(devices.toList()),
            probe = DeviceProbe(api, config, ProbeDiagnostics(), clock),
            scope = backgroundScope,
            clock = clock,
        )
    }

    private fun device(id: Int, url: String) = Device(
        id = id,
        name = "Tor $id",
        status = DeviceStatus.CLOSED,
        deviceUrl = url,
        isOpened = false,
        deviceGroupId = 1,
    )
}

/** [onPing] bekommt die laufende Nummer des Aufrufs (0-basiert) und wirft fuer einen Fehlschlag. */
private class FakeEspApi(private val onPing: suspend (call: Int) -> Unit) : EspApi {
    var pingCalls = 0
        private set

    override suspend fun ping(url: String): Response<Unit> {
        onPing(pingCalls++)
        return Response.success(Unit)
    }

    override suspend fun getStatus(url: String): EspStatusDto = error("unused")
    override suspend fun controlDoor(url: String, body: EspControlDto): ResponseBody = error("unused")
    override suspend fun renameDevice(url: String, body: EspRenameDto): ResponseBody = error("unused")
}

private class FakeGroupRepository(devices: List<Device>) : DeviceGroupRepository {
    val groups = MutableStateFlow(listOf(DeviceGroup(1, "Haus", 0L, GroupIcon.HOME, devices)))

    override fun getGroupsWithDevices() = groups
    override suspend fun addGroup(groupName: String, color: Long, icon: GroupIcon) = error("unused")
    override suspend fun renameGroup(groupId: Int, newName: String) = error("unused")
    override suspend fun updateGroup(groupId: Int, name: String, color: Long, icon: GroupIcon) = error("unused")
    override suspend fun deleteGroup(groupId: Int) = error("unused")
    override suspend fun addDevice(deviceUrl: String, deviceName: String) = error("unused")
    override suspend fun deleteDevice(deviceId: Int) = error("unused")
    override suspend fun moveDevice(deviceId: Int, newGroupId: Int) = error("unused")
    override suspend fun deviceUrlExists(url: String) = error("unused")
    override suspend fun groupNameExists(name: String) = error("unused")
    override suspend fun deleteAllGroupsAndDevices() = error("unused")
    override suspend fun reorderDevices(orderedDeviceIds: List<Int>) = error("unused")
}
