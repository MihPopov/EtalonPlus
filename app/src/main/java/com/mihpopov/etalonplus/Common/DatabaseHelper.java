package com.mihpopov.etalonplus.Common;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mihpopov.etalonplus.Data.Answer;
import com.mihpopov.etalonplus.Data.ComplexCriteria;
import com.mihpopov.etalonplus.Data.Etalon;
import com.mihpopov.etalonplus.Data.Grade;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для работы с локальной базой данных SQLite.
 * Содержит методы для управления эталонами, ответами, критериями и оценками, в том числе их системой.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "etalons_db";
    private static final int DATABASE_VERSION = 7;

    public static final String TABLE_ETALONS = "etalons";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_CREATION_DATE = "creation_date";
    public static final String COLUMN_TASKS_COUNT = "tasks_count";

    public static final String TABLE_ANSWERS = "answers_table";
    public static final String COLUMN_ETALON_ID = "etalon_id";
    public static final String COLUMN_TASK_NUM = "task_number";
    public static final String COLUMN_ANSWER_TYPE = "answer_type";
    public static final String COLUMN_RIGHT_ANSWER = "right_answer";
    public static final String COLUMN_POINTS = "points";
    public static final String COLUMN_ORDER_MATTERS = "order_matters";
    public static final String COLUMN_CHECK_METHOD = "check_method";

    public static final String TABLE_COMPLEX_GRADING = "complex_grading_table";
    public static final String COLUMN_ANSWER_ID = "answer_id";
    public static final String COLUMN_MIN_MISTAKES = "min_mistakes";
    public static final String COLUMN_MAX_MISTAKES = "max_mistakes";

    public static final String TABLE_CRITERIA = "criteria_table";
    public static final String COLUMN_CRITERIA = "criteria";

    public static final String TABLE_GRADES = "grades_table";
    public static final String COLUMN_MIN_POINTS = "min_points";
    public static final String COLUMN_MAX_POINTS = "max_points";
    public static final String COLUMN_GRADE = "grade";

    public static final String TABLE_GRADES_SYSTEM = "grades_system_table";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Создание таблиц
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEtalonTable = "CREATE TABLE " + TABLE_ETALONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_ICON + " BLOB, " +
                COLUMN_CREATION_DATE + " TEXT, " +
                COLUMN_TASKS_COUNT + " INTEGER)";

        String createAnswersTable = "CREATE TABLE " + TABLE_ANSWERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ETALON_ID + " INTEGER, " +
                COLUMN_TASK_NUM + " INTEGER, " +
                COLUMN_ANSWER_TYPE + " TEXT, " +
                COLUMN_RIGHT_ANSWER + " TEXT, " +
                COLUMN_POINTS + " REAL, " +
                COLUMN_ORDER_MATTERS + " INTEGER, " +
                COLUMN_CHECK_METHOD + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_ETALON_ID + ") REFERENCES " + TABLE_ETALONS + "(" + COLUMN_ID + "))";

        String createComplexGradingTable = "CREATE TABLE " + TABLE_COMPLEX_GRADING + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ANSWER_ID + " INTEGER, " +
                COLUMN_MIN_MISTAKES + " INTEGER, " +
                COLUMN_MAX_MISTAKES + " INTEGER, " +
                COLUMN_POINTS + " REAL, " +
                "FOREIGN KEY(" + COLUMN_ANSWER_ID + ") REFERENCES " + TABLE_ANSWERS + "(" + COLUMN_ID + "))";

        String createCriteriaTable = "CREATE TABLE " + TABLE_CRITERIA + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ANSWER_ID + " INTEGER, " +
                COLUMN_CRITERIA + " BLOB, " +
                "FOREIGN KEY(" + COLUMN_ANSWER_ID + ") REFERENCES " + TABLE_ANSWERS + "(" + COLUMN_ID + "))";

        String createGradesTable = "CREATE TABLE " + TABLE_GRADES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ETALON_ID + " INTEGER, " +
                COLUMN_MIN_POINTS + " REAL, " +
                COLUMN_MAX_POINTS + " REAL, " +
                COLUMN_GRADE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_ETALON_ID + ") REFERENCES " + TABLE_ETALONS + "(" + COLUMN_ID + "))";

        String createGradesSystemTable = "CREATE TABLE " + TABLE_GRADES_SYSTEM + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GRADE + " TEXT NOT NULL)";

        db.execSQL(createEtalonTable);
        db.execSQL(createAnswersTable);
        db.execSQL(createComplexGradingTable);
        db.execSQL(createCriteriaTable);
        db.execSQL(createGradesTable);
        db.execSQL(createGradesSystemTable);

        String[] defaultGrades = {"2", "3", "4", "5"};
        for (String grade : defaultGrades) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GRADE, grade);
            db.insert(TABLE_GRADES_SYSTEM, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLEX_GRADING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CRITERIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES_SYSTEM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETALONS);
        onCreate(db);
    }

    // Добавление нового эталона
    public long addEtalon(String name, byte[] icon, String date, int tasksCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ICON, icon);
        values.put(COLUMN_CREATION_DATE, date);
        values.put(COLUMN_TASKS_COUNT, tasksCount);
        return db.insert(TABLE_ETALONS, null, values);
    }

    // Добавление ответа с кратким ответом
    public long addAnswer(int etalonId, int taskNumber, String answerType, String rightAnswer, double points, int orderMatters, String checkMethod) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ETALON_ID, etalonId);
        values.put(COLUMN_TASK_NUM, taskNumber);
        values.put(COLUMN_ANSWER_TYPE, answerType);
        values.put(COLUMN_RIGHT_ANSWER, rightAnswer);
        values.put(COLUMN_POINTS, points);
        values.put(COLUMN_ORDER_MATTERS, orderMatters);
        values.put(COLUMN_CHECK_METHOD, checkMethod);
        return db.insert(TABLE_ANSWERS, null, values);
    }

    // Добавление ответа с развернутым ответом
    public long addAnswer(int etalonId, int taskNumber, String answerType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ETALON_ID, etalonId);
        values.put(COLUMN_TASK_NUM, taskNumber);
        values.put(COLUMN_ANSWER_TYPE, answerType);
        return db.insert(TABLE_ANSWERS, null, values);
    }

    // Добавление диапазона ошибок для поэлементной оценки
    public void addComplexCriteria(int answerId, int minMistakes, int maxMistakes, double points) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANSWER_ID, answerId);
        values.put(COLUMN_MIN_MISTAKES, minMistakes);
        values.put(COLUMN_MAX_MISTAKES, maxMistakes);
        values.put(COLUMN_POINTS, points);
        db.insert(TABLE_COMPLEX_GRADING, null, values);
    }

    // Добавление изображения критериев оценивания
    public void addCriteria(int answerId, byte[] imageData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANSWER_ID, answerId);
        values.put(COLUMN_CRITERIA, imageData);
        db.insert(TABLE_CRITERIA, null, values);
    }

    // Добавление диапазона баллов для оценки
    public void addGrade(int etalonId, double minPoints, double maxPoints, String grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ETALON_ID, etalonId);
        values.put(COLUMN_MIN_POINTS, minPoints);
        values.put(COLUMN_MAX_POINTS, maxPoints);
        values.put(COLUMN_GRADE, grade);
        db.insert(TABLE_GRADES, null, values);
    }

    // Получение списка всех эталонов
    public List<Etalon> getAllEtalons() {
        List<Etalon> etalonList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ETALONS, null);
        try {
            @SuppressLint("DiscouragedPrivateApi") Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 20 * 1024 * 1024);
        } catch (Exception ignored) {}
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                byte[] icon = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ICON));
                String creationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATION_DATE));
                int tasksCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASKS_COUNT));
                etalonList.add(new Etalon(id, name, icon, creationDate, tasksCount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return etalonList;
    }

    //Полное удаление эталона
    public void deleteEtalon(int etalonId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_ANSWERS, new String[]{COLUMN_ID}, COLUMN_ETALON_ID + "=?",
                new String[]{String.valueOf(etalonId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int answerId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                db.delete(TABLE_COMPLEX_GRADING, COLUMN_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)});
                db.delete(TABLE_CRITERIA, COLUMN_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.delete(TABLE_ANSWERS, COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
        db.delete(TABLE_GRADES, COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
        db.delete(TABLE_ETALONS, COLUMN_ID + "=?", new String[]{String.valueOf(etalonId)});
        db.close();
    }

    // Получение эталона по ID
    public Etalon getEtalonById(int etalonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Etalon etalon = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ETALONS + " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(etalonId)});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            byte[] icon = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_ICON));
            String creationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATION_DATE));
            int tasksCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASKS_COUNT));
            etalon = new Etalon(id, name, icon, creationDate, tasksCount);
        }
        cursor.close();
        db.close();
        return etalon;
    }

    // Получение всех ответов для эталона
    public List<Answer> getAnswersByEtalonId(int etalonId) {
        List<Answer> answers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ANSWERS + " WHERE " + COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int taskNumber = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_NUM));
                String answerType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANSWER_TYPE));
                if (answerType.equals("Развёрнутый ответ")) {
                    answers.add(new Answer(id, etalonId, taskNumber, answerType));
                    continue;
                }
                String rightAnswer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RIGHT_ANSWER));
                double points = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_POINTS));
                int orderMatters = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORDER_MATTERS));
                String checkMethod = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECK_METHOD));
                answers.add(new Answer(id, etalonId, taskNumber, answerType, rightAnswer, points, orderMatters, checkMethod));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return answers;
    }

    // Получение системы оценивания для поэлементной оценки краткого ответа
    public List<ComplexCriteria> getComplexGradingByAnswerId(int answerId) {
        List<ComplexCriteria> gradingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COMPLEX_GRADING + " WHERE " + COLUMN_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                int minMistakes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MIN_MISTAKES));
                int maxMistakes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_MISTAKES));
                double points = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_POINTS));
                gradingList.add(new ComplexCriteria(id, answerId, minMistakes, maxMistakes, points));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return gradingList;
    }

    // Получение изображений критериев оценивания
    public List<byte[]> getCriteriaByAnswerId(int answerId) {
        List<byte[]> criteria = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CRITERIA, new String[]{COLUMN_CRITERIA}, COLUMN_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)},
                null, null, null);
        try {
            @SuppressLint("DiscouragedPrivateApi") Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
            field.setAccessible(true);
            field.set(null, 20 * 1024 * 1024);
        } catch (Exception ignored) {}
        if (cursor.moveToFirst()) {
            do {
                criteria.add(cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_CRITERIA)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return criteria;
    }

    // Получение шкалы оценок для эталона
    public List<Grade> getGradesByEtalonId(int etalonId) {
        List<Grade> grades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GRADES + " WHERE " + COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                double minPoints = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_MIN_POINTS));
                double maxPoints = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_MAX_POINTS));
                String grade = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GRADE));
                grades.add(new Grade(id, etalonId, minPoints, maxPoints, grade));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return grades;
    }

    // Получение системы оценок
    public List<String> getGradesSystem() {
        List<String> grades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GRADE + " FROM " + TABLE_GRADES_SYSTEM, null);
        if (cursor.moveToFirst()) {
            do {
                grades.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return grades;
    }

    // Обновление системы оценок
    public void updateGradesSystem(List<String> newGrades) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_GRADES_SYSTEM);
        for (String grade : newGrades) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GRADE, grade);
            db.insert(TABLE_GRADES_SYSTEM, null, values);
        }
        db.close();
    }
}