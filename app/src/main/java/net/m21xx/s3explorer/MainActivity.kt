package net.m21xx.s3explorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import net.m21xx.s3explorer.ui.navigation.S3NavHost
import net.m21xx.s3explorer.ui.theme.S3ExplorerTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import net.m21xx.s3explorer.data.local.preferences.SettingsDataStore
import javax.inject.Inject
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    private var isAuthenticated by mutableStateOf(false)
    private var isAuthenticating = false
    private var lastBackgroundTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplash = true
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }
        lifecycleScope.launch {
            delay(2000)
            keepSplash = false
        }
        
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                checkAuthentication()
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                if (!isAuthenticating) {
                    lastBackgroundTime = System.currentTimeMillis()
                }
            }
        })

        setContent {
            val globalPrefs by settingsDataStore.globalPreferences.collectAsState(initial = null)
            
            androidx.compose.runtime.LaunchedEffect(globalPrefs?.enableLockScreen) {
                if (globalPrefs?.enableLockScreen == false) {
                    isAuthenticated = true
                }
            }

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (globalPrefs?.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemDark
            }
            
            S3ExplorerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    S3NavHost()
                    
                    if (globalPrefs?.enableLockScreen == true && !isAuthenticated) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {}
                                ),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "App Locked",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { checkAuthentication(forcePrompt = true) }) {
                                        Text("Unlock")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAuthentication(forcePrompt: Boolean = false) {
        lifecycleScope.launch {
            val prefs = settingsDataStore.globalPreferences.first()
            if (prefs.enableLockScreen && !isAuthenticating) {
                if (forcePrompt) {
                    showBiometricPrompt()
                    return@launch
                }

                val currentTime = System.currentTimeMillis()
                val timeInBackground = if (lastBackgroundTime == 0L) 0L else (currentTime - lastBackgroundTime) / 1000
                val gracePeriod = prefs.lockGracePeriodSeconds
                
                val wasBackgrounded = lastBackgroundTime > 0L

                var needsReauth = false;

                if (!isAuthenticated) {
                    // when cold start, isAuthenticated will always be false, so let's call screen lock
                    needsReauth = true
                } else if (wasBackgrounded) {
                    // on warm starts, we need to check if the app was backgrounded and if the grace period has expired
                    if (gracePeriod == 0) {
                        // no grace period, always re-auth
                        needsReauth = true
                    } else if (timeInBackground > gracePeriod) {
                        // grace period expired, re-auth
                        needsReauth = true
                    }
                }

                // val needsReauth = isAuthenticated && wasBackgrounded && (gracePeriod == 0 || timeInBackground > gracePeriod)

                // if (!isAuthenticated || needsReauth) {
                if (needsReauth) {
                    isAuthenticated = false // require re-auth
                    showBiometricPrompt()
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        isAuthenticating = true
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isAuthenticating = false
                    // DO NOT call finish() here. It destroys the activity, causing the app to "vanish"
                    // and lose its state. The user will be shown the lock screen and can tap "Unlock" to retry.
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated = true
                    isAuthenticating = false
                    lastBackgroundTime = 0L
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Let the user try again
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Locked")
            .setSubtitle("Authenticate to access S3 Explorer")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
