package com.chat.utils

import com.chat.models.ChatRoom
import com.chat.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService{
    @FormUrlEncoded
    @POST("login.php")
    fun login(@Field("email") email: String, @Field("password") password: String): Call<User>

    @FormUrlEncoded
    @POST("signUp.php")
    fun signUp(@Field("email") email: String, @Field("password") password: String, @Field("name") name: String): Call<User>

    @Multipart
    @POST("updateProfile.php")
    fun updateProfile(@Part("userId") userId: RequestBody,
                      @Part("name") name: RequestBody,
                      @Part("phone") phone: RequestBody,
                      @Part("email") contact_no: RequestBody,
                      @Part("password") latitude: RequestBody,
                      @Part image: MultipartBody.Part?): Call<User>

    @GET("getChatRoom.php")
    fun getChatRoom(@Query("userId") userId: Int, @Query("search") search: String, @Query("page") page: Int): Call<ArrayList<ChatRoom>>

    @GET("getUser.php")
    fun getUser(@Query("search") search: String): Call<ArrayList<User>>

    @Multipart
    @POST("createRoom.php")
    fun createRoom(@Part("userId") userId: Int,
                      @Part("name") name: String,
                      @Part("member") member: String,
                      @Part image: MultipartBody.Part?): Call<ChatRoom>
}