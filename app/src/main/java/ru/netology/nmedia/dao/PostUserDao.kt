package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostUserEntity

@Dao
interface PostUserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(posts: List<PostUserEntity>)

    @Query("SELECT * FROM PostUserEntity WHERE postId = :id")
    suspend fun getById(id: Long) : List<PostUserEntity>

    @Query("DELETE FROM PostUserEntity")
    suspend fun removeAll()

}
