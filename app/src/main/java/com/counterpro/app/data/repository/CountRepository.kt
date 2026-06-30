package com.counterpro.app.data.repository

import com.counterpro.app.data.db.CountDatabase
import com.counterpro.app.data.db.CountHistoryEntity
import com.counterpro.app.data.db.toDomain
import com.counterpro.app.domain.model.CountEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountRepository(private val db: CountDatabase) {
    fun getHistory(): Flow<List<CountEntry>> = db.dao().getAll().map { list -> list.map { it.toDomain() } }
    suspend fun save(label: String, count: Int) = db.dao().insert(CountHistoryEntity(label = label, count = count))
    suspend fun delete(id: Long) = db.dao().delete(id)
    suspend fun clearAll() = db.dao().deleteAll()
}
