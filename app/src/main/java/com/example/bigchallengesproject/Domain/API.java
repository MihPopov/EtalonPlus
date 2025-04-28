package com.example.bigchallengesproject.Domain;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface API {

    @Multipart
    @POST("/ocr")
    Call<List<String>> uploadImage(@Part MultipartBody.Part file);
}