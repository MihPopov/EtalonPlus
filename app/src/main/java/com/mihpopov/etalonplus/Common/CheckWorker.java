package com.mihpopov.etalonplus.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Worker для фоновой проверки работ учащихся.
 */
public class CheckWorker extends Worker {
    public CheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int worksCount = getInputData().getInt("worksCount", 0);
        int[] detailedTasksArray = getInputData().getIntArray("detailedTasks");
        if (detailedTasksArray == null) return Result.failure();
        Map<Integer, List<Bitmap>> worksMap = DataStore.loadBitmapsForCheck(context);
        Map<Integer, List<Bitmap>> criteriaMap = DataStore.loadCriteria(context);
        AIService aiService = new AIService(context);
        Map<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>> allResults = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(worksMap.size() * detailedTasksArray.length);
        for (int i = 1; i <= worksCount; i++) {
            HashMap<Integer, HashMap<String, Pair<Integer, String>>> workResults = new HashMap<>();
            allResults.put(i, workResults);
            List<Bitmap> workPages = worksMap.get(i - 1);
            if (workPages == null) {
                latch.countDown();
                continue;
            }
            for (int t : detailedTasksArray) {
                List<Bitmap> criteria = criteriaMap.get(t);
                if (criteria == null) {
                    latch.countDown();
                    continue;
                }
                final int workIndex = i;
                aiService.evaluateDetailedTask(workPages, t, criteria, new SimpleCallback<HashMap<String, Pair<Integer, String>>>() {
                    @Override
                    public void onLoad(HashMap<String, Pair<Integer, String>> data) {
                        synchronized (allResults) {
                            allResults.get(workIndex).put(t, data);
                        }
                        latch.countDown();
                    }
                });
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.failure();
        }
        DataStore.saveCheckResults(context, allResults);
        NotificationUtils.postNotification(getApplicationContext(), "Проверка завершена", "Все работы успешно проверены", 101);
        return Result.success();
    }
}