package net.m21xx.s3explorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "s3_objects",
    primaryKeys = ["profileId", "bucketName", "objectKey"]
)
data class S3ObjectEntity(
    val profileId: String,
    val objectKey: String,
    val bucketName: String,
    val size: Long,
    val lastModified: Long, // Use timestamp
    val isDirectory: Boolean,
    val parentPrefix: String, // Added parentPrefix to help build folder tree locally
    val extension: String
)
