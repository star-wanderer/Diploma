package ru.netology.nmedia.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun provideUserDao(db: AppDb):UserDao = db.userDao()

    @Provides
    fun provideUserRemoteKeyDao(db: AppDb): UserRemoteKeyDao = db.userRemoteKeyDao()

    @Provides
    fun provideJobDao(db: AppDb): JobDao = db.jobDao()

    @Provides
    fun provideJobRemoteKeyDao(db: AppDb): JobRemoteKeyDao = db.jobRemoteKeyDao()

    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()

    @Provides
    fun providePostUserDao(db: AppDb): PostUserDao = db.postUserDao()

    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()

    @Provides
    fun provideEventDao(db: AppDb): EventDao = db.eventDao()

    @Provides
    fun provideEventUserDao(db: AppDb): EventUserDao = db.eventUserDao()

    @Provides
    fun provideEventRemoteKeyDao(db: AppDb): EventRemoteKeyDao = db.eventRemoteKeyDao()

    @Provides
    fun provideEventSpeakerDao(db: AppDb): EventSpeakerDao = db.eventSpeakerDao()
}