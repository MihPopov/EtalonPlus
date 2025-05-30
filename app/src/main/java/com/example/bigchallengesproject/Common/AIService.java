package com.example.bigchallengesproject.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.chaquo.python.PyObject;
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

    public void recognizeTestAnswers(List<Bitmap> pages, SimpleCallback<HashMap<Integer, String>> callback, String taskTypes, String symbolsToIgnore) {
        new RecognitionTask(py, callback, taskTypes, symbolsToIgnore).execute(pages.toArray(new Bitmap[0]));

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

    public void evaluateDetailedTask(List<Bitmap> workPages, int taskNum, List<Bitmap> criteria, SimpleCallback<HashMap<String, Pair<Integer, String>>> callback) {
        new CheckDetailedTask(py, callback, taskNum).execute(workPages, criteria);
    }

    private static class RecognitionTask extends AsyncTask<Bitmap, Void, HashMap<Integer, String>> {

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
        protected HashMap<Integer, String> doInBackground(Bitmap... bitmaps) {
            HashMap<Integer, String> combinedResults = new HashMap<>();
            try {
                for (Bitmap bitmap : bitmaps) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                    byte[] imageBytes = bos.toByteArray();
                    String result = python.getModule("g4f_api").callAttr("get_test_answers", imageBytes, taskTypes).toString();
                    HashMap<Integer, String> pageResults = parseRecognizedText(result.split("\n"));
                    combinedResults.putAll(pageResults);
                }
                return combinedResults;
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

    private static class CheckDetailedTask extends AsyncTask<List<Bitmap>, Void, HashMap<String, Pair<Integer, String>>> {
        private final Python python;
        private final SimpleCallback<HashMap<String, Pair<Integer, String>>> callback;
        private final int taskNum;
        private Exception exception;

        public CheckDetailedTask(Python python, SimpleCallback<HashMap<String, Pair<Integer, String>>> callback, int taskNum) {
            this.python = python;
            this.callback = callback;
            this.taskNum = taskNum;
        }

        @Override
        protected HashMap<String, Pair<Integer, String>> doInBackground(List<Bitmap>... lists) {
            try {
                List<Bitmap> workPages = lists[0];
                List<Bitmap> criteria = lists[1];
                List<byte[]> workData = convertBitmaps(workPages);
                List<byte[]> criteriaData = convertBitmaps(criteria);

                PyObject result = python.getModule("g4f_api").callAttr("check_detailed_task", PyObject.fromJava(workData.toArray()),
                        PyObject.fromJava(criteriaData.toArray()), taskNum);
                return parseEvaluationResult(result);
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        private List<byte[]> convertBitmaps(List<Bitmap> bitmaps) {
            List<byte[]> result = new ArrayList<>();
            for (Bitmap bitmap : bitmaps) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                result.add(bos.toByteArray());
            }
            return result;
        }

        private HashMap<String, Pair<Integer, String>> parseEvaluationResult(PyObject result) {
            HashMap<String, Pair<Integer, String>> evaluation = new HashMap<>();
            Map<PyObject, PyObject> pyMap = result.asMap();
            for (Map.Entry<PyObject, PyObject> entry : pyMap.entrySet()) {
                String key = entry.getKey().toString();
                List<PyObject> value = entry.getValue().asList();
                int score = value.get(0).toInt();
                String comment = value.get(1).toString();
                evaluation.put(key, new Pair<>(score, comment));
            }
            return evaluation;
        }

        @Override
        protected void onPostExecute(HashMap<String, Pair<Integer, String>> result) {
            if (exception != null) {
                callback.onLoad(null);
                Log.d("777", exception.toString());
            } else {
                callback.onLoad(result);
            }
        }
    }
}