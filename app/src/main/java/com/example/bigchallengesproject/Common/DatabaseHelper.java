package com.example.bigchallengesproject.Common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bigchallengesproject.Data.Answer;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.Data.Grade;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "etalons_db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ETALONS = "etalons";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_CREATION_DATE = "creation_date";
    public static final String COLUMN_TASKS_COUNT = "tasks_count";

    public static final String TABLE_ANSWERS = "answersTable";
    public static final String COLUMN_ANSWER_ID = "id";
    public static final String COLUMN_ETALON_ID = "etalon_id";
    public static final String COLUMN_TASK_NUM = "task_number";
    public static final String COLUMN_RIGHT_ANSWER = "right_answer";
    public static final String COLUMN_POINTS = "points";

    public static final String TABLE_GRADES = "gradesTable";
    public static final String COLUMN_GRADE_ID = "id";
    public static final String COLUMN_MIN_POINTS = "min_points";
    public static final String COLUMN_MAX_POINTS = "max_points";
    public static final String COLUMN_GRADE = "grade";

    public static final String TABLE_GRADES_SYSTEM = "gradesSystemTable";
    public static final String COLUMN_GRADE_NAME_ID = "id";
    public static final String COLUMN_GRADE_NAME = "grade";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEtalonTable = "CREATE TABLE " + TABLE_ETALONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_ICON + " BLOB, " +
                COLUMN_CREATION_DATE + " TEXT, " +
                COLUMN_TASKS_COUNT + " INTEGER)";

        String createAnswersTable = "CREATE TABLE " + TABLE_ANSWERS + " (" +
                COLUMN_ANSWER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ETALON_ID + " INTEGER, " +
                COLUMN_TASK_NUM + " INTEGER, " +
                COLUMN_RIGHT_ANSWER + " TEXT, " +
                COLUMN_POINTS + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_ETALON_ID + ") REFERENCES " + TABLE_ETALONS + "(" + COLUMN_ID + "))";

        String createGradesTable = "CREATE TABLE " + TABLE_GRADES + " (" +
                COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ETALON_ID + " INTEGER, " +
                COLUMN_MIN_POINTS + " INTEGER, " +
                COLUMN_MAX_POINTS + " INTEGER, " +
                COLUMN_GRADE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_ETALON_ID + ") REFERENCES " + TABLE_ETALONS + "(" + COLUMN_ID + "))";

        String createGradesSystemTable = "CREATE TABLE " + TABLE_GRADES_SYSTEM + " (" +
                COLUMN_GRADE_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GRADE_NAME + " TEXT NOT NULL)";

        db.execSQL(createEtalonTable);
        db.execSQL(createAnswersTable);
        db.execSQL(createGradesTable);
        db.execSQL(createGradesSystemTable);

        String[] defaultGrades = {"2", "3", "4", "5"};
        for (String grade : defaultGrades) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GRADE_NAME, grade);
            db.insert(TABLE_GRADES_SYSTEM, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES_SYSTEM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETALONS);
        onCreate(db);
    }

    public long addEtalon(String name, byte[] icon, String date, int tasksCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ICON, icon);
        values.put(COLUMN_CREATION_DATE, date);
        values.put(COLUMN_TASKS_COUNT, tasksCount);
        return db.insert(TABLE_ETALONS, null, values);
    }

    public long addAnswer(int etalonId, int taskNumber, String rightAnswer, int points) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ETALON_ID, etalonId);
        values.put(COLUMN_TASK_NUM, taskNumber);
        values.put(COLUMN_RIGHT_ANSWER, rightAnswer);
        values.put(COLUMN_POINTS, points);
        return db.insert(TABLE_ANSWERS, null, values);
    }

    public long addGrade(int etalonId, int minPoints, int maxPoints, String grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ETALON_ID, etalonId);
        values.put(COLUMN_MIN_POINTS, minPoints);
        values.put(COLUMN_MAX_POINTS, maxPoints);
        values.put(COLUMN_GRADE, grade);
        return db.insert(TABLE_GRADES, null, values);
    }

    public List<Etalon> getAllEtalons() {
        List<Etalon> etalonList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ETALONS, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                byte[] icon = cursor.getBlob(cursor.getColumnIndexOrThrow("icon"));
                String creationDate = cursor.getString(cursor.getColumnIndexOrThrow("creation_date"));
                int tasksCount = cursor.getInt(cursor.getColumnIndexOrThrow("tasks_count"));
                etalonList.add(new Etalon(id, name, icon, creationDate, tasksCount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return etalonList;
    }

    public void deleteEtalon(int etalonId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ANSWERS, COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
        db.delete(TABLE_GRADES, COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
        db.delete(TABLE_ETALONS, COLUMN_ID + "=?", new String[]{String.valueOf(etalonId)});
        db.close();
    }

    public Etalon getEtalonById(int etalonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Etalon etalon = null;
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ETALONS + " WHERE " + COLUMN_ID + "=?",
                new String[]{String.valueOf(etalonId)});
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

    public List<Answer> getAnswersByEtalonId(int etalonId) {
        List<Answer> answers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ANSWERS + " WHERE " + COLUMN_ETALON_ID + "=?",
                new String[]{String.valueOf(etalonId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANSWER_ID));
                int taskNumber = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_NUM));
                String rightAnswer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RIGHT_ANSWER));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS));
                answers.add(new Answer(id, etalonId, taskNumber, rightAnswer, points));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return answers;
    }

    public List<Grade> getGradesByEtalonId(int etalonId) {
        List<Grade> grades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GRADES + " WHERE " + COLUMN_ETALON_ID + "=?",
                new String[]{String.valueOf(etalonId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GRADE_ID));
                int minPoints = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MIN_POINTS));
                int maxPoints = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_POINTS));
                String grade = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GRADE));
                grades.add(new Grade(id, etalonId, minPoints, maxPoints, grade));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return grades;
    }

    public List<String> getGradesSystem() {
        List<String> grades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GRADE_NAME + " FROM " + TABLE_GRADES_SYSTEM, null);
        if (cursor.moveToFirst()) {
            do {
                grades.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return grades;
    }

    public void updateGradesSystem(List<String> newGrades) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_GRADES_SYSTEM);
        for (String grade : newGrades) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GRADE_NAME, grade);
            db.insert(TABLE_GRADES_SYSTEM, null, values);
        }
        db.close();
    }
}