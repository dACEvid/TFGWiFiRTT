package com.davidperez.tfgwifirtt.data

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.davidperez.tfgwifirtt.model.UserSettings
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Interface to the RTT-compatible Devices data layer.
 */
interface UserSettingsRepository {
    suspend fun getUserSettings(): Flow<UserSettings>

    suspend fun setShowRTTCompatibleOnly(value: Boolean)

    suspend fun setPerformContinuousRttRanging(value: Boolean)

    suspend fun setSaveRttResults(value: Boolean)

    suspend fun setSaveLastRttOperationOnly(value: Boolean)

    suspend fun setRttPeriod(value: Long)

    suspend fun setRttInterval(value: Long)

    suspend fun setIgnoreRttPeriod(value: Boolean)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserSettingsRepositoryImpl @Inject constructor(private val application: Application) : UserSettingsRepository {
    // Keys for user settings
    companion object {
        val SHOW_RTT_COMPATIBLE_ONLY = booleanPreferencesKey("show_rtt_compatible_only")
        val PERFORM_CONTINUOUS_RTT_RANGING = booleanPreferencesKey("perform_single_rtt_request")
        val RTT_RANGING_PERIOD = longPreferencesKey("rtt_ranging_period")
        val INTERVAL_BETWEEN_RTT_REQUESTS = longPreferencesKey("interval_between_rtt_requests")
        val IGNORE_RTT_RANGING_PERIOD = booleanPreferencesKey("ignore_rtt_ranging_period")
        val SAVE_RTT_RESULTS = booleanPreferencesKey("save_rtt_results")
        val SAVE_LAST_RTT_OPERATION_ONLY = booleanPreferencesKey("save_last_rtt_operation_only")
    }

    override suspend fun getUserSettings() = application.applicationContext.dataStore.data.map {
        UserSettings(
            showOnlyRttCompatibleAps = it[SHOW_RTT_COMPATIBLE_ONLY] ?: false,
            performContinuousRttRanging = it[PERFORM_CONTINUOUS_RTT_RANGING] ?: false,
            rttPeriod =  it[RTT_RANGING_PERIOD] ?: 10,
            rttInterval = it[INTERVAL_BETWEEN_RTT_REQUESTS] ?: 100,
            ignoreRttPeriod = it[IGNORE_RTT_RANGING_PERIOD] ?: false,
            saveRttResults = it[SAVE_RTT_RESULTS] ?: true,
            saveOnlyLastRttOperation = it[SAVE_LAST_RTT_OPERATION_ONLY] ?: false
        )
    }

    override suspend fun setShowRTTCompatibleOnly(value: Boolean) {
        try {
            application.applicationContext.dataStore.edit {
                it[SHOW_RTT_COMPATIBLE_ONLY] = value
            }
        } catch (e: IOException) {
            // exception thrown if the write to disk fails
            showErrorMsg()
        }
    }

    override suspend fun setPerformContinuousRttRanging(value: Boolean) {
        try {
            application.applicationContext.dataStore.edit {
                it[PERFORM_CONTINUOUS_RTT_RANGING] = value
            }
        } catch (e: IOException) {
            showErrorMsg()
        }
    }

    override suspend fun setSaveRttResults(value: Boolean) {
        try {
            application.applicationContext.dataStore.edit {
                it[SAVE_RTT_RESULTS] = value
            }
        } catch (e: IOException) {
            showErrorMsg()
        }
    }

    override suspend fun setSaveLastRttOperationOnly(value: Boolean) {
        try {
            application.applicationContext.dataStore.edit {
                it[SAVE_LAST_RTT_OPERATION_ONLY] = value
            }
        } catch (e: IOException) {
            showErrorMsg()
        }
    }

    override suspend fun setRttPeriod(value: Long) {
        try {
            application.applicationContext.dataStore.edit {
                it[RTT_RANGING_PERIOD] = value
            }
        } catch (e: IOException) {
            showErrorMsg()
        }
    }

    override suspend fun setRttInterval(value: Long) {
        try {
            application.applicationContext.dataStore.edit {
                it[INTERVAL_BETWEEN_RTT_REQUESTS] = value
            }
        } catch (e: IOException) {
            showErrorMsg()
        }
    }

    override suspend fun setIgnoreRttPeriod(value: Boolean) {
        try {
            application.applicationContext.dataStore.edit {
                it[IGNORE_RTT_RANGING_PERIOD] = value
            }
        } catch (e: IOException) {
            showErrorMsg()
        }
    }

    private fun showErrorMsg() {
        Toast.makeText(this.application, "Update of setting failed", Toast.LENGTH_LONG).show()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSettingsRepositoryModule {
    @Binds
    abstract fun bindRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository
}
