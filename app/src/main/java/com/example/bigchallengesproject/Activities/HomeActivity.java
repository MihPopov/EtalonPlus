package com.example.bigchallengesproject.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bigchallengesproject.R;

//import org.opencv.android.Utils;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfPoint;
//import org.opencv.core.Rect;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));
    }

//    public Bitmap detectTextBlocks(Bitmap bitmap) {
//        Mat imgMat = new Mat();
//        Utils.bitmapToMat(bitmap, imgMat);
//
//        // 1. Конвертируем в оттенки серого
//        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGBA2GRAY);
//
//        // 2. Применяем размытие для удаления шумов
//        Imgproc.GaussianBlur(imgMat, imgMat, new Size(5, 5), 0);
//
//        // 3. Бинаризация методом Otsu
//        Imgproc.threshold(imgMat, imgMat, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
//
//        // 4. Морфологическая обработка для соединения букв в слова
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(70, 7));
//        Imgproc.morphologyEx(imgMat, imgMat, Imgproc.MORPH_CLOSE, kernel);
//
//        // 5. Поиск контуров (блоков текста)
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(imgMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        // 6. Рисуем прямоугольники вокруг текста (с фильтрацией)
//        Bitmap resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(resultBitmap);
//        Paint paint = new Paint();
//        paint.setColor(Color.GREEN);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(4);
//
//        for (MatOfPoint contour : contours) {
//            Rect rect = Imgproc.boundingRect(contour);
//
//            if (rect.height < 10 || rect.width < 50) continue; // Отбрасываем слишком маленькие блоки
//            double aspectRatio = (double) rect.width / rect.height;
//            if (aspectRatio < 1.2 || aspectRatio > 10) continue; // Фильтруем странные пропорции
//
//            // Рисуем только отфильтрованные блоки
//            canvas.drawRect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, paint);
//        }
//
//        return resultBitmap;
//    }

    public void onCheckCardClick(View view) {
        startActivity(new Intent(HomeActivity.this, CheckActivity.class));
    }

    public void onStorageCardClick(View view) {
        startActivity(new Intent(HomeActivity.this, StorageActivity.class));
    }

    public void onSettingsCardClick(View view) {
        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
    }

    public void onFeedbackCardClick(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.yandex.ru/u/67e16e2050569081bd96015f/")));
    }
}