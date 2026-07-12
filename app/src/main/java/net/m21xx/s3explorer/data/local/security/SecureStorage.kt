package net.m21xx.s3explorer.data.local.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "s3_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSecretKey(profileId: String, secretKey: String) {
        sharedPreferences.edit().putString(profileId, secretKey).apply()
    }

    fun getSecretKey(profileId: String): String? {
        return sharedPreferences.getString(profileId, null)
    }

    fun deleteSecretKey(profileId: String) {
        sharedPreferences.edit().remove(profileId).apply()
    }
}
