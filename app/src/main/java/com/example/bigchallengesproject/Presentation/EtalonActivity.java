package com.example.bigchallengesproject.Presentation;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.bigchallengesproject.Common.DatabaseHelper;
import com.example.bigchallengesproject.Data.Answer;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.Data.Grade;
import com.example.bigchallengesproject.R;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class EtalonActivity extends BaseActivity {

    int etalonId;

    TextInputEditText etalonNameInput, etalonTasksCount;
    TextView etalonCreationDate;
    CardView uploadIconCard;
    androidx.gridlayout.widget.GridLayout answersEtalonTable, gradesEtalonTable, detailedEtalonTable;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etalon);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        etalonNameInput = findViewById(R.id.etalon_name_input);
        etalonTasksCount = findViewById(R.id.tasks_count_etalon);
        etalonCreationDate = findViewById(R.id.etalon_creation_date);
        etalonIcon = findViewById(R.id.etalon_icon_preview);
        uploadIconCard = findViewById(R.id.upload_icon_card);
        answersEtalonTable = findViewById(R.id.answers_etalon_table);
        detailedEtalonTable = findViewById(R.id.detailed_etalon_table);
        gradesEtalonTable = findViewById(R.id.grades_etalon_table);
        dbHelper = new DatabaseHelper(this);

        initAdapters();
        etalonId = getIntent().getIntExtra("etalon_id", -1);
        if (etalonId != -1) {
            Etalon etalon = dbHelper.getEtalonById(etalonId);
            if (etalon != null) {
                etalonNameInput.setText(etalon.getName());
                etalonTasksCount.setText(String.valueOf(etalon.getTasksCount()));
                etalonCreationDate.setText("Дата создания: " + etalon.getCreationDate());
                Bitmap bitmap = BitmapFactory.decodeByteArray(etalon.getIcon(), 0, etalon.getIcon().length);
                etalonIconBitmap = bitmap;
                etalonIcon.setImageBitmap(bitmap);
                answersEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_answers_header, null));
                List<Answer> answers = dbHelper.getAnswersByEtalonId(etalonId);
                for (Answer answer : answers) {
                    View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
                    final int taskNum = answer.getTaskNumber();
                    ((TextView) row.findViewById(R.id.task_num)).setText(taskNum + "");
                    Spinner answerTypesDropdown = row.findViewById(R.id.answer_type_dropdown);
                    Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
                    List<byte[]> criteria = dbHelper.getCriteriaByAnswerId(answer.getId());
                    setupCheckMethodsDropdown(checkMethodsDropdown, row, answer);
                    setupAnswerTypesDropdown(answerTypesDropdown, row, taskNum, this, detailedEtalonTable, criteria, answer);

                    answersEtalonTable.addView(row);
                }
                setupTextChangedListener(etalonTasksCount, this, answersEtalonTable, detailedEtalonTable);
                detailedEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_header, null));
                detailedEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_merged_row, null));
                gradesEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_grades_header, null));
                List<Grade> grades = dbHelper.getGradesByEtalonId(etalonId);
                loadGrades(grades, gradesEtalonTable);
            }
        }

        uploadIconCard.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                setPickEtalonIconRequest();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_CRITERIA_PAGES_REQUEST:
                    handleCriteriaImageSelection(data, detailedEtalonTable);
                    break;

                case CAPTURE_CRITERIA_PAGE_REQUEST:
                    if (cameraImageUri != null) addPageToCriteria(cameraImageUri, detailedEtalonTable);
                    break;

                case PICK_ETALON_IMAGE_REQUEST:
                    handleEtalonImageSelection(data);
                    break;
            }
        }
    }

    public void onExitFromEtalonClick(View view) {
        startActivity(new Intent(EtalonActivity.this, StorageActivity.class));
    }

    public void onSaveEtalonClick(View view) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            int newTasksCount;
            try {
                newTasksCount = Integer.parseInt(etalonTasksCount.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Напишите количество заданий корректно!", Toast.LENGTH_SHORT).show();
                return;
            }
            String newName = etalonNameInput.getText().toString().trim();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            etalonIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] iconBytes = stream.toByteArray();
            if (newName.isEmpty() || !isTablesCorrect(answersEtalonTable, gradesEtalonTable)) {
                Toast.makeText(this, "Заполните данные эталона корректно!", Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues etalonValues = new ContentValues();
            etalonValues.put(DatabaseHelper.COLUMN_NAME, newName);
            etalonValues.put(DatabaseHelper.COLUMN_TASKS_COUNT, newTasksCount);
            etalonValues.put(DatabaseHelper.COLUMN_ICON, iconBytes);

            db.update(DatabaseHelper.TABLE_ETALONS, etalonValues, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(etalonId)});
            Cursor cursor = db.query(DatabaseHelper.TABLE_ANSWERS, new String[]{DatabaseHelper.COLUMN_ID}, DatabaseHelper.COLUMN_ETALON_ID + "=?",
                    new String[]{String.valueOf(etalonId)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    int answerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    db.delete(DatabaseHelper.TABLE_COMPLEX_GRADING, DatabaseHelper.COLUMN_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)});
                    db.delete(DatabaseHelper.TABLE_CRITERIA, DatabaseHelper.COLUMN_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)});
                } while (cursor.moveToNext());
            }
            db.delete(DatabaseHelper.TABLE_ANSWERS, DatabaseHelper.COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});
            db.delete(DatabaseHelper.TABLE_GRADES, DatabaseHelper.COLUMN_ETALON_ID + "=?", new String[]{String.valueOf(etalonId)});

            saveAnswersAndCriteria(etalonId, answersEtalonTable, this);
            saveGrades(etalonId, gradesEtalonTable);
            
            db.setTransactionSuccessful();
            Toast.makeText(this, "Эталон успешно обновлён!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка обновления!", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}