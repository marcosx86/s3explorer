package net.m21xx.s3explorer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GlobalSettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.uiState.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showUADialog by remember { mutableStateOf(false) }
    var tempUserAgent by remember { mutableStateOf("") }
    
    var showGracePeriodDialog by remember { mutableStateOf(false) }
    var tempGracePeriod by remember { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    fun confirmAppLockToggle(currentChecked: Boolean) {
        val fragmentActivity = context as? androidx.fragment.app.FragmentActivity
        if (fragmentActivity == null) {
            viewModel.toggleLockScreen(!currentChecked)
            return
        }

        val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
        val biometricPrompt = androidx.biometric.BiometricPrompt(fragmentActivity, executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.toggleLockScreen(!currentChecked)
                }
            })

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirm Action")
            .setSubtitle(if (currentChecked) "Authenticate to disable app lock" else "Authenticate to enable app lock")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Section: System Integration
            SectionHeader("System Integration & OS Mounting")
            
            ListItem(
                headlineContent = { Text("Enable Mount (SAF)") },
                supportingContent = { Text("Allow Android system to browse this bucket.") },
                trailingContent = {
                    Switch(
                        checked = prefs.enableSafMount,
                        onCheckedChange = { viewModel.toggleSafMount(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleSafMount(!prefs.enableSafMount) 
                }
            )

            ListItem(
                headlineContent = { Text("Trust insecure SSL/TLS certificates") },
                supportingContent = { Text("Allows connections to self-signed endpoints like MinIO.") },
                trailingContent = {
                    Switch(
                        checked = prefs.trustSslCertificate,
                        onCheckedChange = { viewModel.toggleTrustSsl(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleTrustSsl(!prefs.trustSslCertificate) 
                }
            )

            HorizontalDivider()

            // Section: App Security
            SectionHeader("App Security")

            ListItem(
                headlineContent = { Text("Enable native lock screen") },
                supportingContent = { Text("Require biometrics or PIN to open the app.") },
                trailingContent = {
                    Switch(
                        checked = prefs.enableLockScreen,
                        onCheckedChange = { confirmAppLockToggle(prefs.enableLockScreen) }
                    )
                },
                modifier = Modifier.clickable { 
                    confirmAppLockToggle(prefs.enableLockScreen)
                }
            )

            if (prefs.enableLockScreen) {
                ListItem(
                    headlineContent = { Text("AppLock grace period") },
                    supportingContent = { 
                        if (prefs.lockGracePeriodSeconds == 0) {
                            Text("Always ask on foreground")
                        } else {
                            Text("${prefs.lockGracePeriodSeconds} seconds") 
                        }
                    },
                    modifier = Modifier.clickable { 
                        tempGracePeriod = prefs.lockGracePeriodSeconds.toString()
                        showGracePeriodDialog = true 
                    }
                )
            }

            HorizontalDivider()

            // Section: View Preferences
            SectionHeader("View Preferences")

            ListItem(
                headlineContent = { Text("Display long date format") },
                trailingContent = {
                    Switch(
                        checked = prefs.displayLongDateFormat,
                        onCheckedChange = { viewModel.toggleLongDateFormat(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleLongDateFormat(!prefs.displayLongDateFormat) 
                }
            )

            ListItem(
                headlineContent = { Text("Hide dotfiles") },
                trailingContent = {
                    Switch(
                        checked = prefs.hideDotfiles,
                        onCheckedChange = { viewModel.toggleHideDotfiles(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleHideDotfiles(!prefs.hideDotfiles) 
                }
            )

            ListItem(
                headlineContent = { Text("Choose theme") },
                supportingContent = { Text(prefs.themeMode) },
                modifier = Modifier.clickable { 
                    showThemeDialog = true 
                }
            )

            ListItem(
                headlineContent = { Text("Show image thumbnails") },
                trailingContent = {
                    Switch(
                        checked = prefs.showImageThumbnails,
                        onCheckedChange = { viewModel.toggleShowThumbnails(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleShowThumbnails(!prefs.showImageThumbnails) 
                }
            )
            
            ListItem(
                headlineContent = { Text("Show video thumbnails") },
                trailingContent = {
                    Switch(
                        checked = prefs.showVideoThumbnails,
                        onCheckedChange = { viewModel.toggleShowVideoThumbnails(it) }
                    )
                },
                modifier = Modifier.clickable { 
                    viewModel.toggleShowVideoThumbnails(!prefs.showVideoThumbnails) 
                }
            )

            HorizontalDivider()

            // Section: Miscellaneous
            SectionHeader("Miscellaneous")

            ListItem(
                headlineContent = { Text("Custom user agent") },
                supportingContent = { Text(prefs.customUserAgent) },
                modifier = Modifier.clickable {
                    tempUserAgent = prefs.customUserAgent
                    showUADialog = true 
                }
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    val themes = listOf("Light", "Dark", "System")
                    themes.forEach { theme ->
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = prefs.themeMode == theme,
                                onClick = {
                                    viewModel.setThemeMode(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(theme)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUADialog) {
        AlertDialog(
            onDismissRequest = { showUADialog = false },
            title = { Text("Custom User Agent") },
            text = {
                OutlinedTextField(
                    value = tempUserAgent,
                    onValueChange = { tempUserAgent = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setCustomUserAgent(tempUserAgent)
                    showUADialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showUADialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showGracePeriodDialog) {
        AlertDialog(
            onDismissRequest = { showGracePeriodDialog = false },
            title = { Text("Grace Period (seconds)") },
            text = {
                OutlinedTextField(
                    value = tempGracePeriod,
                    onValueChange = { tempGracePeriod = it.filter { char -> char.isDigit() } },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val seconds = tempGracePeriod.toIntOrNull() ?: 0
                    viewModel.setLockGracePeriod(seconds)
                    showGracePeriodDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showGracePeriodDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}
