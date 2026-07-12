package net.m21xx.s3explorer.ui.connection

data class NewConnectionState(
    val accessKey: String = "",
    val secretKey: String = "",
    val endpointUrl: String = "",
    val bucketName: String = "",
    val termsAccepted: Boolean = false,
    val isTestingConnection: Boolean = false,
    val isSecretVisible: Boolean = false,
    val connectionResult: Result<Unit>? = null
) {
    val isConnectEnabled: Boolean
        get() = accessKey.isNotBlank() &&
                secretKey.isNotBlank() &&
                endpointUrl.isNotBlank() &&
                termsAccepted &&
                !isTestingConnection
}
