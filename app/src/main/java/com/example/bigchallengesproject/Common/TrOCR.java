package com.example.bigchallengesproject.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.bigchallengesproject.API;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
//    private OrtEnvironment env;
//    private OrtSession encoderSession;
//    private OrtSession decoderSession;
    private static final String BASE_URL = "http://10.1.30.116:8000/";
    private Context context;
    Retrofit retrofit;
    API api;

    public TrOCR(Context context) {
        this.context = context;
//        env = OrtEnvironment.getEnvironment();
//        encoderSession = env.createSession(loadModel("encoder_model.onnx").getAbsolutePath(), new OrtSession.SessionOptions());
//        decoderSession = env.createSession(loadModel("decoder_model.onnx").getAbsolutePath(), new OrtSession.SessionOptions());
    }

    private File loadModel(String assetName) throws IOException {
        InputStream is = context.getAssets().open(assetName);
        File file = new File(context.getFilesDir(), assetName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }

    public void recognizeTextBlock(Bitmap bitmap, SimpleCallback<List<String>> callback) throws Exception {
        List<Rect> textBlocks = detectTextBlocks(bitmap);
        List<String> results = new ArrayList<>();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(API.class);

//        Mat processed = preprocessImage(bitmap);
//        File imageFile = matToFile(processed, "frag.png");
//
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);
//
//        Call<String> call = api.uploadImage(body);
//        call.enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                if (response.isSuccessful()) {
////                    results.add(response.body());
//                    System.out.println(response.body());
//                }
//                else Log.e("OCR", "Ошибка: " + response.code());
//            }
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                Log.e("OCR", "Сбой соединения: " + t.getMessage());
//            }
//        });

        List<MultipartBody.Part> images = new ArrayList<>();
        for (Rect rect : textBlocks) {
            Bitmap cropped = Bitmap.createBitmap(bitmap, rect.x, rect.y, rect.width, rect.height);
            Mat processed = preprocessImage(cropped);
            File imageFile = matToFile(processed, "frag_" + System.currentTimeMillis() + ".png");
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", imageFile.getName(), requestFile);
            images.add(body);
        }

        Call<List<String>> call = api.uploadImage(images);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful()) {
                    results.clear();
                    results.addAll(response.body());
                    callback.onLoad(results);
                }
                else Log.e("OCR", "Ошибка: " + response.code());
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.e("OCR", "Сбой соединения: " + t.getMessage());
            }
        });
    }

    private List<Rect> detectTextBlocks(Bitmap bitmap) {
        Mat imgMat = new Mat();
        Utils.bitmapToMat(bitmap, imgMat);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(imgMat, imgMat, new Size(5, 5), 0);
        Imgproc.threshold(imgMat, imgMat, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(70, 7));
        Imgproc.morphologyEx(imgMat, imgMat, Imgproc.MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> validRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if (rect.height < 10 || rect.width < 50) continue;
            double aspectRatio = (double) rect.width / rect.height;
            if (aspectRatio < 1.2 || aspectRatio > 10) continue;
            validRects.add(rect);
        }
        return validRects;
    }

    private Mat preprocessImage(Bitmap bitmap) {
        Mat img = new Mat();
        Utils.bitmapToMat(bitmap, img);
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);
        Imgproc.resize(img, img, new Size(384, 384), 0, 0, Imgproc.INTER_LINEAR);
        return img;
    }

    public File matToFile(Mat mat, String filename) {
        File file = new File(context.getCacheDir(), filename);
        Imgcodecs.imwrite(file.getAbsolutePath(), mat);
        return file;
    }

    private String runInference(File imageFile) throws Exception {
//        OnnxTensor input = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputTensor), inputShape);
//        Map<String, OnnxTensor> inputs = Collections.singletonMap("pixel_values", input);
//        OrtSession.Result encoderResult = encoderSession.run(inputs);
//        OnnxTensor encoderOutput = (OnnxTensor) encoderResult.get(0);
//
//        long[] inputIds = tokenizeInput();
//        OnnxTensor inputIdsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), new long[]{1, inputIds.length});
//        Map<String, OnnxTensor> decoderInputs = new HashMap<>();
//        decoderInputs.put("encoder_hidden_states", encoderOutput);
//        decoderInputs.put("input_ids", inputIdsTensor);
//        OrtSession.Result decoderResult = decoderSession.run(decoderInputs);
//
//        float[][][] rawLogits = (float[][][]) decoderResult.get(0).getValue();
//        float[][] logits = rawLogits[0];
//        return decodeTokens(logits);
        return "";
    }

    private long[] tokenizeInput() throws JSONException, IOException {
        InputStream vocabStream = context.getAssets().open("vocab.json");
        InputStreamReader vocabReader = new InputStreamReader(vocabStream);
        BufferedReader bufferedReader = new BufferedReader(vocabReader);
        StringBuilder vocabBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            vocabBuilder.append(line);
        }
        bufferedReader.close();
        JSONObject vocabJson = new JSONObject(vocabBuilder.toString());

        long[] inputIds = new long[]{vocabJson.getLong("<s>")};
        return inputIds;
    }

    private String decodeTokens(float[][] logits) throws IOException, JSONException {
        InputStream vocabStream = context.getAssets().open("vocab.json");
        InputStreamReader vocabReader = new InputStreamReader(vocabStream);
        BufferedReader bufferedReader = new BufferedReader(vocabReader);
        StringBuilder vocabBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            vocabBuilder.append(line);
        }
        bufferedReader.close();

        JSONObject vocabJson = new JSONObject(vocabBuilder.toString());

        Map<Integer, java.lang.String> idToToken = new HashMap<>();
        Iterator<java.lang.String> keys = vocabJson.keys();
        while (keys.hasNext()) {
            java.lang.String key = keys.next();
            idToToken.put(vocabJson.getInt(key), key);
        }

        StringBuilder decodedText = new StringBuilder();
        for (float[] tokenProbs : logits) {
            int maxIdx = 0;
            float maxVal = tokenProbs[0];

            for (int j = 1; j < tokenProbs.length; j++) {
                if (tokenProbs[j] > maxVal) {
                    maxVal = tokenProbs[j];
                    maxIdx = j;
                }
            }

            java.lang.String token = idToToken.get(maxIdx);
            if (!token.equals("<pad>") && !token.equals("</s>")) {
                decodedText.append(token);
            }
        }

        return decodedText.toString().replace("▁", " ").trim();
    }

//    public void close() throws Exception {
//        encoderSession.close();
//        decoderSession.close();
//        env.close();
//    }
}