package net.m21xx.s3explorer.data.model

data class ConnectionExportItem(
    val profileId: String,
    val alias: String,
    val endpointUrl: String,
    val accessKey: String,
    val defaultBucket: String,
    val region: String,
    val secretKey: String
)
