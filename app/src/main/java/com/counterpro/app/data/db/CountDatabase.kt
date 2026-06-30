package com.counterpro.app.data.db

import androidx.room.*
import com.counterpro.app.domain.model.CountEntry
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "count_history")
data class CountHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)

fun CountHistoryEntity.toDomain() = CountEntry(id, label, count, timestamp)

@Dao
interface CountHistoryDao {
    @Query("SELECT * FROM count_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CountHistoryEntity>>

    @Insert
    suspend fun insert(entry: CountHistoryEntity)

    @Query("DELETE FROM count_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM count_history")
    suspend fun deleteAll()
}

@Database(entities = [CountHistoryEntity::class], version = 1, exportSchema = false)
abstract class CountDatabase : RoomDatabase() {
    abstract fun dao(): CountHistoryDao
}
