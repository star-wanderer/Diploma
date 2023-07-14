package ru.netology.nmedia.vendor

import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class VendorModule {

    @Singleton
    @Provides
    fun provideGoogleApiAvailability(
    ): GoogleApiAvailability = GoogleApiAvailability.getInstance()

    @Singleton
    @Provides
    fun provideFirebase(
    ): FirebaseMessaging = FirebaseMessaging.getInstance()
}