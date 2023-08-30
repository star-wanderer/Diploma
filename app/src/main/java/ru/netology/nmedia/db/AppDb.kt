package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.*
import ru.netology.nmedia.entity.*

@Database(entities = [
                    UserEntity::class,
                    UserRemoteKeyEntity::class,
                    JobEntity::class,
                    JobRemoteKeyEntity::class,
                    PostEntity::class,
                    PostUserEntity::class,
                    PostRemoteKeyEntity::class,
                    EventEntity::class,
                    EventUserEntity::class,
                    EventSpeakerEntity::class,
                    EventRemoteKeyEntity::class, ],
                    version = 6,
                    exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userRemoteKeyDao():UserRemoteKeyDao
    abstract fun jobDao(): JobDao
    abstract fun jobRemoteKeyDao(): JobRemoteKeyDao
    abstract fun postDao(): PostDao
    abstract fun postUserDao(): PostUserDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventDao(): EventDao
    abstract fun eventUserDao(): EventUserDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun eventSpeakerDao(): EventSpeakerDao
}