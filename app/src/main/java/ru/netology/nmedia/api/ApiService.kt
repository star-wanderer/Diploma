package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.PushToken

interface ApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("events/{id}/newer")
    suspend fun getNewerEvents(@Path("id") id: Long): Response<List<Event>>

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @POST("posts")
    suspend fun saveEvents(@Body event: Event): Response<Event>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{post_id}/likes")
    suspend fun likeById(@Path("post_id") id: Long): Response<Post>

    @DELETE("posts/{post_id}/likes")
    suspend fun dislikeById(@Path("post_id") id: Long): Response<Post>

    @Multipart
    @POST("media")
    suspend fun upload(
        @Part media: MultipartBody.Part
    ): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("password") password: String
    ): Response<AuthModel>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun register(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("name") name: String,
    ): Response<AuthModel>

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("password") password: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part,
    ): Response<AuthModel>

    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

    @GET("posts/{id}/before")
    suspend fun getBefore(
        @Path ("id") id: Long,
        @Query ("count") count: Int
    ): Response<List<Post>>

    @GET("events/{id}/before")
    suspend fun getBeforeEvents(
        @Path ("id") id: Long,
        @Query ("count") count: Int
    ): Response<List<Event>>

    @GET("posts/{id}/after")
    suspend fun getAfter(
        @Path ("id") id: Long,
        @Query ("count") count: Int
    ): Response<List<Post>>

    @GET("events/{id}/after")
    suspend fun getAfterEvents(
        @Path ("id") id: Long,
        @Query ("count") count: Int
    ): Response<List<Event>>

    @GET("posts/latest")
    suspend fun getLatest(
        @Query ("count") count: Int
    ): Response<List<Post>>

    @GET("events/latest")
    suspend fun getLatestEvents(
        @Query ("count") count: Int
    ): Response<List<Event>>
}