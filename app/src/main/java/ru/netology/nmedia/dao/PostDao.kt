package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE isNew = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM PostEntity WHERE isNew = 1")
    suspend fun getIsNewCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE PostEntity SET isNew = 0 WHERE isNew = 1")
    suspend fun update()

    @Query("SELECT * FROM PostEntity WHERE id < :id ORDER BY id DESC LIMIT :count")
    suspend fun getBefore(
        id: Long,
        count: Int
    ): List<PostEntity>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC LIMIT :count")
    suspend fun getLatest(
        count: Int
    ): List<PostEntity>

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()
}
