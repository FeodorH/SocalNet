package com.example.socalnet.data.remote.cloudinary

import com.example.socalnet.data.dto.CloudinaryDeleteResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface CloudinaryApi {

    @FormUrlEncoded
    @POST("v1_1/{cloudName}/delete_by_token")
    suspend fun deleteByToken(
        @Path("cloudName") cloudName: String,
        @Field("token") token: String
    ): Response<CloudinaryDeleteResponse>
}