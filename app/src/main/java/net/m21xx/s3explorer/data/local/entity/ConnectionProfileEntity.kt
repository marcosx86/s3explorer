package net.m21xx.s3explorer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "connection_profiles")
data class ConnectionProfileEntity(
    @PrimaryKey val profileId: String = UUID.randomUUID().toString(),
    val alias: String,
    val endpointUrl: String,
    val accessKey: String,
    val defaultBucket: String,
    val region: String = "us-east-1",
    val lastUsedAt: Long = System.currentTimeMillis()
)
