package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, JobEntity>

    @Query("SELECT * FROM JobEntity WHERE isNew = 0 ORDER BY id DESC")
    fun getAll(): Flow<List<JobEntity>>

    @Query("SELECT COUNT(*) == 0 FROM JobEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM JobEntity")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM JobEntity WHERE isNew = 1")
    suspend fun getIsNewCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(job: JobEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(jobs: List<JobEntity>)

    @Query("SELECT * FROM JobEntity WHERE id = :id")
    suspend fun getById(id: Long) : JobEntity

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("UPDATE JobEntity SET isNew = 0 WHERE isNew = 1")
    suspend fun update()

    @Query("SELECT * FROM JobEntity WHERE id < :id ORDER BY id DESC LIMIT :count")
    suspend fun getBefore(id: Long, count: Int): List<JobEntity>

    @Query("SELECT * FROM JobEntity ORDER BY id DESC LIMIT :count")
    suspend fun getLatest(count: Int): List<JobEntity>

    @Query("DELETE FROM JobEntity")
    suspend fun removeAll()

}
