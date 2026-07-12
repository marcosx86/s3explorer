package net.m21xx.s3explorer

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.m21xx.s3explorer.data.local.AppDatabase
import net.m21xx.s3explorer.data.local.dao.ConnectionProfileDao
import net.m21xx.s3explorer.data.local.entity.ConnectionProfileEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionProfileDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ConnectionProfileDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.connectionProfileDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetProfile() = runBlocking {
        val profile = ConnectionProfileEntity(
            profileId = "test-id",
            alias = "My S3",
            endpointUrl = "http://localhost:9000",
            accessKey = "minioadmin",
            defaultBucket = "test-bucket"
        )
        
        dao.insertProfile(profile)
        
        val retrievedProfile = dao.getProfileById("test-id")
        
        assertNotNull(retrievedProfile)
        assertEquals("My S3", retrievedProfile?.alias)
        assertEquals("test-bucket", retrievedProfile?.defaultBucket)
    }

    @Test
    fun getAllProfilesFlow() = runBlocking {
        val profile1 = ConnectionProfileEntity(profileId = "id-1", alias = "Alias 1", endpointUrl = "url", accessKey = "key", defaultBucket = "bucket")
        val profile2 = ConnectionProfileEntity(profileId = "id-2", alias = "Alias 2", endpointUrl = "url", accessKey = "key", defaultBucket = "bucket")

        dao.insertProfile(profile1)
        dao.insertProfile(profile2)

        val profiles = dao.getAllProfiles().first()
        assertEquals(2, profiles.size)
    }
}
