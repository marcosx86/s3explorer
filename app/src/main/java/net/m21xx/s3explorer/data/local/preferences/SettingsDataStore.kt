package net.m21xx.s3explorer.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.m21xx.s3explorer.ui.explorer.ExplorerViewMode
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val VIEW_MODE_KEY = stringPreferencesKey("view_mode")

    val viewMode: Flow<ExplorerViewMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[VIEW_MODE_KEY] ?: ExplorerViewMode.DETAILED_LIST.name
        try {
            ExplorerViewMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            ExplorerViewMode.DETAILED_LIST
        }
    }

    suspend fun setViewMode(mode: ExplorerViewMode) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = mode.name
        }
    }
}
