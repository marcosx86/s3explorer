package net.m21xx.s3explorer.ui.connection

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onConnectionSuccess: (String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showBucketSheet by remember { mutableStateOf(false) }

    // Handle navigation on success
    uiState.connectionResult?.onSuccess { profileId ->
        onConnectionSuccess(profileId, uiState.bucketName)
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
                value = uiState.region,
                onValueChange = { viewModel.updateRegion(it) },
                label = { Text("Region (Optional)") },
                placeholder = { Text("us-east-1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.bucketName,
                onValueChange = { viewModel.updateBucketName(it) },
                label = { Text("Bucket Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { 
                        showBucketSheet = true
                        viewModel.fetchBuckets() 
                    }) {
                        Icon(imageVector = Icons.Default.List, contentDescription = "List buckets")
                    }
                }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleTermsAccepted(!uiState.termsAccepted) }
            ) {
                Checkbox(
                    checked = uiState.termsAccepted,
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I agree to the Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Validation and Save Feedback
            if (uiState.isConnectionValidated) {
                Text(
                    text = "Connection parameters validated successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            uiState.validationError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.validateConnection() },
                    enabled = uiState.isValidateEnabled,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isValidatingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Validate")
                    }
                }

                Button(
                    onClick = { viewModel.testConnection() },
                    enabled = uiState.isConnectEnabled,
                    modifier = Modifier.weight(1f)
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
        
        if (showBucketSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBucketSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select a Bucket", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isFetchingBuckets) {
                        CircularProgressIndicator()
                    } else if (uiState.fetchBucketsError != null) {
                        Text(
                            text = uiState.fetchBucketsError ?: "Error fetching buckets",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (uiState.availableBuckets.isEmpty()) {
                        Text("No buckets found.")
                    } else {
                        uiState.availableBuckets.forEach { bucket ->
                            TextButton(
                                onClick = {
                                    viewModel.updateBucketName(bucket)
                                    showBucketSheet = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(bucket)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
