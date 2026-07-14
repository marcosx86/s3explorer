package net.m21xx.s3explorer.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.m21xx.s3explorer.ui.explorer.ExplorerViewMode
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class GlobalPreferences(
    val enableSafMount: Boolean = false,
    val trustSslCertificate: Boolean = false,
    val enableLockScreen: Boolean = false,
    val displayLongDateFormat: Boolean = false,
    val hideDotfiles: Boolean = false,
    val showImageThumbnails: Boolean = true,
    val sortBy: net.m21xx.s3explorer.ui.explorer.SortBy = net.m21xx.s3explorer.ui.explorer.SortBy.NAME,
    val sortDirection: net.m21xx.s3explorer.ui.explorer.SortDirection = net.m21xx.s3explorer.ui.explorer.SortDirection.ASCENDING
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
    private val ENABLE_SAF_MOUNT_KEY = booleanPreferencesKey("enable_saf_mount")
    private val TRUST_SSL_KEY = booleanPreferencesKey("trust_ssl_certificate")
    private val ENABLE_LOCK_SCREEN_KEY = booleanPreferencesKey("enable_lock_screen")
    private val DISPLAY_LONG_DATE_KEY = booleanPreferencesKey("display_long_date")
    private val HIDE_DOTFILES_KEY = booleanPreferencesKey("hide_dotfiles")
    private val SHOW_THUMBNAILS_KEY = booleanPreferencesKey("show_thumbnails")
    private val SORT_BY_KEY = stringPreferencesKey("sort_by")
    private val SORT_DIR_KEY = stringPreferencesKey("sort_dir")

    val viewMode: Flow<ExplorerViewMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[VIEW_MODE_KEY] ?: ExplorerViewMode.DETAILED_LIST.name
        try {
            ExplorerViewMode.valueOf(modeName)
        } catch (e: IllegalArgumentException) {
            ExplorerViewMode.DETAILED_LIST
        }
    }

    val globalPreferences: Flow<GlobalPreferences> = context.dataStore.data.map { prefs ->
        GlobalPreferences(
            enableSafMount = prefs[ENABLE_SAF_MOUNT_KEY] ?: false,
            trustSslCertificate = prefs[TRUST_SSL_KEY] ?: false,
            enableLockScreen = prefs[ENABLE_LOCK_SCREEN_KEY] ?: false,
            displayLongDateFormat = prefs[DISPLAY_LONG_DATE_KEY] ?: false,
            hideDotfiles = prefs[HIDE_DOTFILES_KEY] ?: false,
            showImageThumbnails = prefs[SHOW_THUMBNAILS_KEY] ?: true,
            sortBy = try { net.m21xx.s3explorer.ui.explorer.SortBy.valueOf(prefs[SORT_BY_KEY] ?: "NAME") } catch (e: Exception) { net.m21xx.s3explorer.ui.explorer.SortBy.NAME },
            sortDirection = try { net.m21xx.s3explorer.ui.explorer.SortDirection.valueOf(prefs[SORT_DIR_KEY] ?: "ASCENDING") } catch (e: Exception) { net.m21xx.s3explorer.ui.explorer.SortDirection.ASCENDING }
        )
    }

    suspend fun setViewMode(mode: ExplorerViewMode) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = mode.name
        }
    }

    suspend fun setSafMount(enabled: Boolean) = context.dataStore.edit { it[ENABLE_SAF_MOUNT_KEY] = enabled }
    suspend fun setTrustSsl(enabled: Boolean) = context.dataStore.edit { it[TRUST_SSL_KEY] = enabled }
    suspend fun setLockScreen(enabled: Boolean) = context.dataStore.edit { it[ENABLE_LOCK_SCREEN_KEY] = enabled }
    suspend fun setLongDateFormat(enabled: Boolean) = context.dataStore.edit { it[DISPLAY_LONG_DATE_KEY] = enabled }
    suspend fun setHideDotfiles(enabled: Boolean) = context.dataStore.edit { it[HIDE_DOTFILES_KEY] = enabled }
    suspend fun setShowThumbnails(enabled: Boolean) = context.dataStore.edit { it[SHOW_THUMBNAILS_KEY] = enabled }
    suspend fun setSortBy(sortBy: net.m21xx.s3explorer.ui.explorer.SortBy) = context.dataStore.edit { it[SORT_BY_KEY] = sortBy.name }
    suspend fun setSortDirection(dir: net.m21xx.s3explorer.ui.explorer.SortDirection) = context.dataStore.edit { it[SORT_DIR_KEY] = dir.name }
}
