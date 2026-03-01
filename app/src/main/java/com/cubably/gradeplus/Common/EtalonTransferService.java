package com.cubably.gradeplus.Common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.cubably.gradeplus.Data.Answer;
import com.cubably.gradeplus.Data.ComplexCriteria;
import com.cubably.gradeplus.Data.Etalon;
import com.cubably.gradeplus.Data.Grade;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Сервис для экспорта и импорта эталонов в ZIP.
 */
public class EtalonTransferService {

    private final DatabaseHelper dbHelper;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public EtalonTransferService(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    private ExportModel buildExportModel(int etalonId) {
        Etalon etalon = dbHelper.getEtalonById(etalonId);
        if (etalon == null) throw new IllegalStateException("Эталон не найден");
        ExportModel model = new ExportModel();
        model.name = etalon.getName();
        model.icon = "icon.png";
        model.creationDate = etalon.getCreationDate();
        model.tasksCount = etalon.getTasksCount();
        model.grades = dbHelper.getGradesByEtalonId(etalonId);
        List<Answer> answers = dbHelper.getAnswersByEtalonId(etalonId);
        model.answers = new ArrayList<>();
        for (Answer answer : answers) {
            AnswerModel am = new AnswerModel();
            am.taskNumber = answer.getTaskNumber();
            am.answerType = answer.getAnswerType();
            am.rightAnswer = answer.getRightAnswer();
            am.points = answer.getPoints();
            am.orderMatters = answer.getOrderMatters();
            am.checkMethod = answer.getCheckMethod();
            am.complexCriteria = dbHelper.getComplexGradingByAnswerId(answer.getId());
            am.criteriaImages = new ArrayList<>();
            List<byte[]> images = dbHelper.getCriteriaByAnswerId(answer.getId());
            for (int i = 0; i < images.size(); i++) {
                String fileName = "criteria_" + answer.getId() + "_" + (i + 1) + ".png";
                am.criteriaImages.add(fileName);
            }
            model.answers.add(am);
        }
        return model;
    }

    @Nullable
    public File exportEtalonZip(int etalonId, String exportFileName, Context context) {
        try {
            ExportModel exportModel = buildExportModel(etalonId);
            File zipFile = new File(context.getExternalFilesDir(null), exportFileName + ".zip");
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos))) {
                String json = gson.toJson(exportModel);
                ZipEntry jsonEntry = new ZipEntry("etalon.json");
                zos.putNextEntry(jsonEntry);
                zos.write(json.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                byte[] iconBytes = dbHelper.getEtalonById(etalonId).getIcon();
                if (iconBytes != null && iconBytes.length > 0) {
                    ZipEntry iconEntry = new ZipEntry("icon.png");
                    zos.putNextEntry(iconEntry);
                    zos.write(iconBytes);
                    zos.closeEntry();
                }
                if (exportModel.answers != null) {
                    for (AnswerModel answer : exportModel.answers) {
                        List<byte[]> images = dbHelper.getCriteriaByAnswerId(answer.taskNumber);
                        for (int i = 0; i < images.size(); i++) {
                            byte[] imageBytes = images.get(i);
                            if (imageBytes == null || imageBytes.length == 0) continue;
                            String fileName = "criteria/" + "criteria_" + answer.taskNumber + "_" + (i + 1) + ".png";
                            zos.putNextEntry(new ZipEntry(fileName));
                            zos.write(imageBytes);
                            zos.closeEntry();
                        }
                    }
                }
            }
            return zipFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public Long importEtalonZip(Uri uri, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            Map<String, byte[]> zipEntries = new HashMap<>();
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                zipEntries.put(entry.getName(), baos.toByteArray());
                zis.closeEntry();
            }

            byte[] jsonBytes = zipEntries.get("etalon.json");
            if (jsonBytes == null) throw new IllegalStateException("etalon.json отсутствует");
            String json = new String(jsonBytes, StandardCharsets.UTF_8);
            ExportModel model = gson.fromJson(json, ExportModel.class);
            byte[] iconBytes = zipEntries.get("icon.png");
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            long newEtalonId = dbHelper.addEtalon(
                    model.name,
                    iconBytes,
                    sdf.format(new Date()),
                    model.tasksCount
            );
            if (newEtalonId == -1) throw new IllegalStateException("Не удалось создать эталон");
            if (model.grades != null) {
                for (Grade g : model.grades) {
                    dbHelper.addGrade(
                            (int) newEtalonId,
                            g.getMinPoints(),
                            g.getMaxPoints(),
                            g.getGrade()
                    );
                }
            }
            if (model.answers != null) {
                for (AnswerModel am : model.answers) {
                    long newAnswerId;
                    if ("Развёрнутый ответ".equals(am.answerType)) {
                        newAnswerId = dbHelper.addAnswer(
                                (int) newEtalonId,
                                am.taskNumber,
                                am.answerType
                        );
                    } else {
                        newAnswerId = dbHelper.addAnswer(
                                (int) newEtalonId,
                                am.taskNumber,
                                am.answerType,
                                am.rightAnswer,
                                am.points,
                                am.orderMatters,
                                am.checkMethod
                        );
                    }
                    if (newAnswerId == -1) throw new IllegalStateException("Ошибка создания ответа");
                    if (am.complexCriteria != null) {
                        for (ComplexCriteria cc : am.complexCriteria) {
                            dbHelper.addComplexCriteria(
                                    (int) newAnswerId,
                                    cc.getMinMistakes(),
                                    cc.getMaxMistakes(),
                                    cc.getPoints()
                            );
                        }
                    }
                    if (am.criteriaImages != null) {
                        for (String fileName : am.criteriaImages) {
                            String zipPath = "criteria/" + fileName;
                            byte[] imageBytes = zipEntries.get(zipPath);
                            if (imageBytes != null) {
                                dbHelper.addCriteria(
                                        (int) newAnswerId,
                                        imageBytes
                                );
                            }
                        }
                    }
                }
            }
            db.setTransactionSuccessful();
            return newEtalonId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.endTransaction();
        }
    }

    private static class ExportModel {
        String name;
        String icon;
        String creationDate;
        int tasksCount;
        List<AnswerModel> answers;
        List<Grade> grades;
    }

    private static class AnswerModel {
        int taskNumber;
        String answerType;
        String rightAnswer;
        double points;
        int orderMatters;
        String checkMethod;
        List<ComplexCriteria> complexCriteria;
        public List<String> criteriaImages;
    }
}