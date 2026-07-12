package net.m21xx.s3explorer.data.remote

import aws.sdk.kotlin.services.s3.model.Object

data class S3ListResult(
    val folders: List<String>,
    val files: List<Object>
)
