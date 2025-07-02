package com.mihpopov.etalonplus.Common;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Worker для фонового распознавания ответов на тестовые задания.
 */
public class RecognitionWorker extends Worker {
    public RecognitionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Map<Integer, List<Bitmap>> bitmapMap = DataStore.loadBitmapsForRecognition(getApplicationContext());
        String taskTypes = getInputData().getString("taskTypes");
        String symbolsToIgnore = getInputData().getString("symbolsToIgnore");
        AIService aiService = new AIService(getApplicationContext());
        Map<Integer, HashMap<Integer, String>> result = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(bitmapMap.size());
        for (Map.Entry<Integer, List<Bitmap>> entry : bitmapMap.entrySet()) {
            int index = entry.getKey();
            List<Bitmap> pages = entry.getValue();
            aiService.recognizeTestAnswers(pages, new SimpleCallback<HashMap<Integer, String>>() {
                @Override
                public void onLoad(HashMap<Integer, String> answersMap) {
                    synchronized (result) {
                        result.put(index, answersMap);
                    }
                    latch.countDown();
                }
            }, taskTypes, symbolsToIgnore);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.failure();
        }
        DataStore.saveRecognitionResults(getApplicationContext(), result);
        NotificationUtils.postNotification(getApplicationContext(), "Распознавание завершено", "Все работы успешно распознаны", 100);
        return Result.success();
    }
}