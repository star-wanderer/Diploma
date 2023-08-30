package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.EventUserEntity

@Dao
interface EventUserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(events: List<EventUserEntity>)

    @Query("SELECT * FROM EventUserEntity WHERE eventId = :id")
    suspend fun getById(id: Long) : List<EventUserEntity>

    @Query("DELETE FROM EventUserEntity")
    suspend fun removeAll()

}
