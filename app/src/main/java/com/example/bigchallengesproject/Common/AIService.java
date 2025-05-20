package com.example.bigchallengesproject.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.*;
import java.util.*;

public class AIService {
//    private static final String BASE_URL = "https://ml-api.cloudpub.ru/";
//    Retrofit retrofit;
//    API api;
    Python py;

    public AIService(Context context) {
        if (!Python.isStarted()) Python.start(new AndroidPlatform(context));
        py = Python.getInstance();
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(waitTime + 30, TimeUnit.SECONDS)
//                .writeTimeout(waitTime + 30, TimeUnit.SECONDS)
//                .build();
//
//        retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        api = retrofit.create(API.class);
    }

    public void recognizeTextBlocks(Bitmap bitmap, SimpleCallback<HashMap<Integer, String>> callback, String taskTypes, String symbolsToIgnore) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] imageBytes = bos.toByteArray();

        new RecognitionTask(py, callback, taskTypes, symbolsToIgnore).execute(imageBytes);

//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
//
//        Call<List<String>> call = api.uploadImage(body);
//        call.enqueue(new Callback<List<String>>() {
//            @Override
//            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
//                if (response.isSuccessful()) {
//                    results.clear();
//                    results.addAll(response.body());
//                    callback.onLoad(results);
//                }
//                else {
//                    callback.onLoad(null);
//                    Log.e("OCR", "Ошибка: " + response.code());
//                }
//            }
//            @Override
//            public void onFailure(Call<List<String>> call, Throwable t) {
//                callback.onLoad(null);
//                Log.e("OCR", "Сбой соединения: " + t.getMessage());
//            }
//        });
    }

    private static class RecognitionTask extends AsyncTask<byte[], Void, HashMap<Integer, String>> {

        private final Python python;
        private final SimpleCallback<HashMap<Integer, String>> callback;
        private final String taskTypes;
        private final String symbolsToIgnore;
        private Exception exception;

        public RecognitionTask(Python python, SimpleCallback<HashMap<Integer, String>> callback, String taskTypes, String symbolsToIgnore) {
            this.python = python;
            this.callback = callback;
            this.taskTypes = taskTypes;
            this.symbolsToIgnore = symbolsToIgnore;
        }

        @Override
        protected HashMap<Integer, String> doInBackground(byte[]... bytes) {
            try {
                String result = python.getModule("g4f_api").callAttr("process_image", bytes[0], taskTypes).toString();
                return parseRecognizedText(result.split("\n"));
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<Integer, String> result) {
            if (exception != null) {
                callback.onLoad(null);
            } else {
                callback.onLoad(result);
            }
        }

        private HashMap<Integer, String> parseRecognizedText(String[] recognizedText) {
            HashMap<Integer, String> answersMap = new HashMap<>();
            for (String block : recognizedText) {
                if (!block.isEmpty()) {
                    int spaceIndex = block.indexOf(" ");
                    if (spaceIndex != -1) {
                        try {
                            int taskNumber = Integer.parseInt(block.substring(0, spaceIndex).trim());
                            String answer = block.substring(spaceIndex + 1).trim();
                            StringBuilder cleanedAnswer = new StringBuilder();
                            for (char c : answer.toCharArray()) {
                                if (symbolsToIgnore.indexOf(c) == -1) {
                                    Log.d("777", c + " " + symbolsToIgnore);
                                    cleanedAnswer.append(c);
                                }
                            }
                            answersMap.put(taskNumber, cleanedAnswer.toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return answersMap;
        }
    }
}