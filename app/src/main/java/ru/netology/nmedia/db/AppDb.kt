package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostUserEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.EventEntity
import ru.netology.nmedia.entity.EventRemoteKeyEntity

@Database(entities = [PostEntity::class,
                    PostRemoteKeyEntity::class,
                    EventEntity::class,
                    PostUserEntity::class,
                    EventRemoteKeyEntity::class, ],
                    version = 3,
                    exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postUserDao(): PostUserDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao

}