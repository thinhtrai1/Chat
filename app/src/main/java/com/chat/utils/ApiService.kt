package com.chat.utils

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
                      @Part("name") address: RequestBody,
                      @Part("phone") phone: RequestBody,
                      @Part("email") contact_no: RequestBody,
                      @Part("password") latitude: RequestBody,
                      @Part image: MultipartBody.Part?): Call<User>
}