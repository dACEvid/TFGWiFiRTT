package com.davidperez.tfgwifirtt.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.davidperez.tfgwifirtt.model.RTTCompatibleDevice

@Database(entities = [RTTCompatibleDevice::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rttCompatibleDeviceDao(): RTTCompatibleDeviceDao
}