package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.PushToken

interface ApiService {

    //////////// <<----->> USERS <<----->> ////////////

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") id: Long): Response<User>

    //////////// <<----->> POSTS <<----->> ////////////

    @GET("posts/{id}")
    suspend fun getById(@Path("id") id: Long): Response<Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{post_id}/likes")
    suspend fun likeById(@Path("post_id") id: Long): Response<Post>

    @DELETE("posts/{post_id}/likes")
    suspend fun dislikeById(@Path("post_id") id: Long): Response<Post>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path ("id") id: Long,@Query ("count") count: Int): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path ("id") id: Long, @Query ("count") count: Int
    ): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query ("count") count: Int
    ): Response<List<Post>>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    //////////// <<----->> EVENTS <<----->> ////////////

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: Long): Response<Event>

    @POST("events")
    suspend fun saveEvents(@Body event: Event): Response<Event>

    @DELETE("events/{id}")
    suspend fun removeEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{event_id}/likes")
    suspend fun likeEventById(@Path("event_id") id: Long): Response<Event>

    @DELETE("events/{event_id}/likes")
    suspend fun dislikeEventById(@Path("event_id") id: Long): Response<Event>

    @POST("events/{event_id}/participants")
    suspend fun checkInEventById(@Path("event_id") id: Long): Response<Event>

    @DELETE("events/{event_id}/participants")
    suspend fun checkOutEventById(@Path("event_id") id: Long): Response<Event>

    @GET("events/{id}/before")
    suspend fun getBeforeEvents(@Path ("id") id: Long, @Query ("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/newer")
    suspend fun getNewerEvents(@Path("id") id: Long): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getAfterEvents(@Path ("id") id: Long, @Query ("count") count: Int
    ): Response<List<Event>>

    @GET("events/latest")
    suspend fun getLatestEvents(@Query ("count") count: Int
    ): Response<List<Event>>

    //////////// <<----->> JOBS <<----->> ////////////

    @GET("my/jobs")
    suspend fun getJobs(): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun removeJobById(@Path("id") id: Long): Response<Unit>

    @POST("{id}/jobs")
    suspend fun getUserJobs(@Path("id") id: Long): Response<List<Job>>

    //////////// <<----->> AUTHENTICATION <<----->> ////////////

    @POST("users/push-tokens")
    suspend fun save(@Body pushToken: PushToken): Response<Unit>

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

    //////////// <<----->> MEDIA <<----->> ////////////

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>
}