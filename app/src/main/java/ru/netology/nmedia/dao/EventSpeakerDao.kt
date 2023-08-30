package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.EventSpeakerEntity

@Dao
interface EventSpeakerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(events: List<EventSpeakerEntity>)

    @Query("SELECT * FROM EventSpeakerEntity WHERE eventId = :id")
    suspend fun getById(id: Long) : List<EventSpeakerEntity>

    @Query("DELETE FROM EventSpeakerEntity")
    suspend fun removeAll()

}
