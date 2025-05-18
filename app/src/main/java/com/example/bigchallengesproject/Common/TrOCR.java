package com.example.bigchallengesproject.Common;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.bigchallengesproject.Domain.API;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrOCR {
    private static final String BASE_URL = "https://ml-api.cloudpub.ru/";
    Retrofit retrofit;
    API api;

    public TrOCR(int waitTime) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(waitTime + 30, TimeUnit.SECONDS)
                .writeTimeout(waitTime + 30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(API.class);
    }

    public void recognizeTextBlocks(Bitmap bitmap, SimpleCallback<List<String>> callback) throws Exception {
        List<String> results = new ArrayList<>();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] imageBytes = bos.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

        Call<List<String>> call = api.uploadImage(body);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    results.clear();
                    results.addAll(response.body());
                    callback.onLoad(results);
                }
                else {
                    callback.onLoad(null);
                    Log.e("OCR", "Ошибка: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onLoad(null);
                Log.e("OCR", "Сбой соединения: " + t.getMessage());
            }
        });
    }
}