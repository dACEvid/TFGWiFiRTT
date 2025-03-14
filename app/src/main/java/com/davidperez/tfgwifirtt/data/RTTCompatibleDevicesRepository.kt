package com.davidperez.tfgwifirtt.data

import android.app.Application
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Interface to the RTT-compatible Devices data layer.
 */
interface RTTCompatibleDevicesRepository {

    /**
     * Get RTT-compatible devices.
     */
    suspend fun getRTTCompatibleDevices()

    /**
     * Observe the RTT-compatible devices.
     */
    fun observeRTTCompatibleDevicesList(): Flow<List<RTTCompatibleDevice>>
}

class RTTCompatibleDevicesRepositoryImpl @Inject constructor(private val application: Application) : RTTCompatibleDevicesRepository {
    private val rttCompatibleDevicesList = MutableStateFlow<List<RTTCompatibleDevice>>(emptyList())

    override suspend fun getRTTCompatibleDevices() {
        val db = Firebase.firestore
        db.collection("compatible-devices")
            .addSnapshotListener { value, error -> // For real-time listening of the collection
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    rttCompatibleDevicesList.value = value.toObjects()
                }
            }
    }

    override fun observeRTTCompatibleDevicesList(): Flow<List<RTTCompatibleDevice>> = rttCompatibleDevicesList.asStateFlow()
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindRepository(impl: RTTCompatibleDevicesRepositoryImpl): RTTCompatibleDevicesRepository
}
