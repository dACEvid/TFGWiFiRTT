package com.davidperez.tfgwifirtt.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface RTTCompatibleDeviceDao {
    @Query("SELECT * FROM rtt_compatible_devices")
    fun getAll(): Flow<List<RTTCompatibleDevice>>

    @Query("SELECT * FROM rtt_compatible_devices WHERE id IN (:deviceIds)")
    fun getAllByIds(deviceIds: IntArray): List<RTTCompatibleDevice>

    @Query("SELECT * FROM rtt_compatible_devices WHERE model LIKE :model")
    fun getByModel(model: String): RTTCompatibleDevice

    @Insert
    fun insert(devices: RTTCompatibleDevice)

    @Delete
    fun delete(device: RTTCompatibleDevice)
}

class RTTCompatibleDevicesRepository