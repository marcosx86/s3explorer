package net.m21xx.s3explorer.ui.connection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConnectionScreen(
    viewModel: NewConnectionViewModel = hiltViewModel(),
    onConnectionSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation on success
    if (uiState.connectionResult?.isSuccess == true) {
        onConnectionSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Connect with S3 endpoint") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.accessKey,
                onValueChange = { viewModel.updateAccessKey(it) },
                label = { Text("Access Key ID / Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.secretKey,
                onValueChange = { viewModel.updateSecretKey(it) },
                label = { Text("Secret Access Key / Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (uiState.isSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { viewModel.toggleSecretVisibility() }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Toggle visibility"
                        )
                    }
                }
            )

            OutlinedTextField(
                value = uiState.endpointUrl,
                onValueChange = { viewModel.updateEndpointUrl(it) },
                label = { Text("Endpoint URL") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            OutlinedTextField(
                value = uiState.bucketName,
                onValueChange = { viewModel.updateBucketName(it) },
                label = { Text("Bucket Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = { /* TODO: List buckets */ }) {
                            Icon(imageVector = Icons.Default.List, contentDescription = "List buckets")
                        }
                        IconButton(onClick = { /* TODO: Create bucket */ }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Create bucket")
                        }
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.termsAccepted,
                    onCheckedChange = { viewModel.toggleTermsAccepted(it) }
                )
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.connectionResult?.exceptionOrNull()?.let { error ->
                Text(
                    text = error.message ?: "Connection failed",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { viewModel.testConnection() },
                enabled = uiState.isConnectEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isTestingConnection) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Connect")
                }
            }
        }
    }
}
