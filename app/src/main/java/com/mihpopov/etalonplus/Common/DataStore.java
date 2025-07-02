package com.mihpopov.etalonplus.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mihpopov.etalonplus.Presentation.WorkAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Класс для хранения и загрузки временных данных (изображений и результатов) для работы с фоновыми процессами.
 * Использует файловую систему и SharedPreferences.
 */
public class DataStore {

    // Сохранение изображений работ для проверки
    public static void saveBitmapsForCheck(Context context, List<WorkAdapter.WorkItem> works) {
        Map<Integer, List<Bitmap>> map = new HashMap<>();
        for (int i = 0; i < works.size(); i++) {
            map.put(i, getBitmapsFromPages(context, works.get(i).getPages()));
        }
        saveBitmapMap(context, "check_bitmaps", map);
    }

    // Загрузка изображений работ для проверки
    public static Map<Integer, List<Bitmap>> loadBitmapsForCheck(Context context) {
        return loadBitmapMap(context, "check_bitmaps");
    }

    // Сохранение изображений работ для распознавания
    public static void saveBitmapsForRecognition(Context context, List<WorkAdapter.WorkItem> works) {
        Map<Integer, List<Bitmap>> map = new HashMap<>();
        for (int i = 0; i < works.size(); i++) {
            map.put(i, getBitmapsFromPages(context, works.get(i).getPages()));
        }
        saveBitmapMap(context, "recognition_bitmaps", map);
    }

    // Загрузка изображений работ для распознавания
    public static Map<Integer, List<Bitmap>> loadBitmapsForRecognition(Context context) {
        return loadBitmapMap(context, "recognition_bitmaps");
    }

    // Сохранение результатов распознавания
    public static void saveRecognitionResults(Context context, Map<Integer, HashMap<Integer, String>> result) {
        String json = new Gson().toJson(result);
        context.getSharedPreferences("recognition", Context.MODE_PRIVATE).edit().putString("rec_results", json).apply();
    }

    // Загрузка результатов распознавания
    public static Map<Integer, HashMap<Integer, String>> loadRecognitionResults(Context context) {
        String json = context.getSharedPreferences("recognition", Context.MODE_PRIVATE).getString("rec_results", "{}");
        Type type = new TypeToken<Map<Integer, HashMap<Integer, String>>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    // Сохранение изображений критериев оценивания
    public static void saveCriteria(Context context, Map<Integer, List<WorkAdapter.PageItem>> map) {
        Map<Integer, List<Bitmap>> result = new HashMap<>();
        for (Map.Entry<Integer, List<WorkAdapter.PageItem>> entry : map.entrySet()) {
            result.put(entry.getKey(), getBitmapsFromPages(context, entry.getValue()));
        }
        saveBitmapMap(context, "check_criteria", result);
    }

    // Загрузка изображений критериев оценивания
    public static Map<Integer, List<Bitmap>> loadCriteria(Context context) {
        return loadBitmapMap(context, "check_criteria");
    }

    // Сохранение результатов проверки
    public static void saveCheckResults(Context context, Map<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>> result) {
        String json = new Gson().toJson(result);
        context.getSharedPreferences("check", Context.MODE_PRIVATE).edit().putString("check_results", json).apply();
    }

    // Загрузка результатов проверки
    public static List<Pair<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>>> loadCheckResults(Context context) {
        String json = context.getSharedPreferences("check", Context.MODE_PRIVATE).getString("check_results", "{}");
        Type type = new TypeToken<Map<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>>>() {}.getType();
        Map<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>> raw = new Gson().fromJson(json, type);
        List<Pair<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>>> result = new ArrayList<>();
        for (Map.Entry<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>> entry : raw.entrySet()) {
            result.add(new Pair<>(entry.getKey(), entry.getValue()));
        }
        result.sort(Comparator.comparingInt(pair -> pair.first));
        return result;
    }

    // Сохранение коллекции изображений в файлы
    private static void saveBitmapMap(Context context, String key, Map<Integer, List<Bitmap>> map) {
        File dir = new File(context.getFilesDir(), key);
        if (dir.exists()) {
            deleteDirectory(dir);
        }
        dir.mkdirs();
        for (Map.Entry<Integer, List<Bitmap>> entry : map.entrySet()) {
            int mapKey = entry.getKey();
            List<Bitmap> bitmaps = entry.getValue();
            for (int i = 0; i < bitmaps.size(); i++) {
                File file = new File(dir, mapKey + "_" + i + ".png");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    bitmaps.get(i).compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Загрузка коллекции изображений из файлов
    private static Map<Integer, List<Bitmap>> loadBitmapMap(Context context, String key) {
        File dir = new File(context.getFilesDir(), key);
        Map<Integer, TreeMap<Integer, Bitmap>> tempMap = new HashMap<>();
        if (!dir.exists() || !dir.isDirectory()) return new HashMap<>();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
        if (files == null) return new HashMap<>();
        for (File file : files) {
            String name = file.getName().replace(".png", "");
            String[] parts = name.split("_");
            if (parts.length != 2) continue;
            try {
                int mapKey = Integer.parseInt(parts[0]);
                int index = Integer.parseInt(parts[1]);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap == null) continue;
                if (!tempMap.containsKey(mapKey)) {
                    tempMap.put(mapKey, new TreeMap<>());
                }
                tempMap.get(mapKey).put(index, bitmap);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        Map<Integer, List<Bitmap>> result = new HashMap<>();
        for (Map.Entry<Integer, TreeMap<Integer, Bitmap>> entry : tempMap.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return result;
    }

    // Удаление директории с временными файлами
    private static void deleteDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        dir.delete();
    }

    // Конвертация URI страниц в Bitmap
    private static List<Bitmap> getBitmapsFromPages(Context context, List<WorkAdapter.PageItem> pages) {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (WorkAdapter.PageItem pageItem : pages) {
            Uri imageUri = pageItem.getUri();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                bitmaps.add(bitmap);
            } catch (IOException e) {
                bitmaps.add(null);
            }
        }
        return bitmaps;
    }
}