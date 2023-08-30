package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, UserEntity>

    @Query("SELECT * FROM UserEntity WHERE isNew = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) == 0 FROM UserEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM UserEntity")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM UserEntity WHERE isNew = 1")
    suspend fun getIsNewCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(users: List<UserEntity>)

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun getById(id: Long) : UserEntity

    @Query("DELETE FROM UserEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE UserEntity SET isNew = 0 WHERE isNew = 1")
    suspend fun update()

    @Query("SELECT * FROM UserEntity WHERE id < :id ORDER BY id DESC LIMIT :count")
    suspend fun getBefore(id: Long, count: Int): List<UserEntity>

    @Query("SELECT * FROM UserEntity ORDER BY id DESC LIMIT :count")
    suspend fun getLatest(count: Int): List<UserEntity>

    @Query("DELETE FROM UserEntity")
    suspend fun removeAll()

}
