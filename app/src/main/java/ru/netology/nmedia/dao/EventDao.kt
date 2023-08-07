package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity WHERE isNew = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM EventEntity")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM EventEntity WHERE isNew = 1")
    suspend fun getIsNewCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE EventEntity SET isNew = 0 WHERE isNew = 1")
    suspend fun update()

    @Query("SELECT * FROM EventEntity WHERE id < :id ORDER BY id DESC LIMIT :count")
    suspend fun getBefore(
        id: Long,
        count: Int
    ): List<EventEntity>

    @Query("SELECT * FROM EventEntity ORDER BY id DESC LIMIT :count")
    suspend fun getLatest(
        count: Int
    ): List<EventEntity>

    @Query("DELETE FROM EventEntity")
    suspend fun removeAll()

    @Query("DELETE FROM EventEntity")
    suspend fun removeAllEvents()
}
