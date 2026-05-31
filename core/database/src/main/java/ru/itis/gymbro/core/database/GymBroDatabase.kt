package ru.itis.gymbro.core.database

import androidx.room.*

@Entity(tableName = "cached_locations")
data class CachedLocation(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val avgRating: Double,
    val reviewCount: Int,
    val cachedAt: Long
) {
    fun isExpired(ttlMillis: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > ttlMillis
    }
}

@Entity(tableName = "cached_events")
data class CachedEvent(
    @PrimaryKey val id: Long,
    val title: String,
    val startTime: String,
    val endTime: String,
    val hostName: String,
    val locationName: String,
    val status: String,
    val cachedAt: Long
) {
    fun isExpired(ttlMillis: Long): Boolean {
        return System.currentTimeMillis() - cachedAt > ttlMillis
    }
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM cached_locations")
    suspend fun getAll(): List<CachedLocation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<CachedLocation>)

    @Query("DELETE FROM cached_locations")
    suspend fun clearAll()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM cached_events")
    suspend fun getAll(): List<CachedEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CachedEvent>)

    @Query("DELETE FROM cached_events")
    suspend fun clearAll()
}

@Database(entities = [CachedLocation::class, CachedEvent::class], version = 1, exportSchema = false)
abstract class GymBroDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun eventDao(): EventDao
}
