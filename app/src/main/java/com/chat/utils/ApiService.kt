package com.chat.utils

import com.chat.models.Chat
import com.chat.models.ChatRoom
import com.chat.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(@Field("email") email: String, @Field("password") password: String): Call<User>

    @FormUrlEncoded
    @POST("signUp.php")
    fun signUp(@Field("email") email: String, @Field("password") password: String, @Field("name") name: String): Call<User>

    @Multipart
    @POST("updateProfile.php")
    fun updateProfile(
        @Part("userId") userId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("email") contact_no: RequestBody,
        @Part("password") latitude: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<User>

    @GET("getChatRoom.php")
    fun getChatRoom(@Query("userId") userId: Int, @Query("search") search: String, @Query("page") page: Int): Call<ArrayList<ChatRoom>>

    @GET("getUser.php")
    fun getUser(@Query("userId") userId: Int, @Query("search") search: String): Call<ArrayList<User>>

    @Multipart
    @POST("createRoom.php")
    fun createRoom(
        @Part("userId") userId: Int,
        @Part("name") name: RequestBody,
        @Part("member") member: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<ChatRoom>

    @GET("getMessage.php")
    fun getMessage(
        @Query("userId") userId: Int,
        @Query("roomId") roomId: Int,
        @Query("search") search: String,
        @Query("latestTime") latestTime: Long
    ): Call<ArrayList<Chat>>

    @Multipart
    @POST("sendMessage.php")
    fun sendMessage(
        @Part("userId") userId: RequestBody,
        @Part("roomId") roomId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("message") message: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Call<Chat>

    @FormUrlEncoded
    @POST("updateFireBaseToken.php")
    fun updateFireBaseToken(@Field("userId") userId: Int, @Field("deviceId") deviceId: String, @Field("token") token: String): Call<ResponseBody>
}