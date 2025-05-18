package com.example.bigchallengesproject.Presentation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.bigchallengesproject.Common.DatabaseHelper;
import com.example.bigchallengesproject.Data.Answer;
import com.example.bigchallengesproject.Data.ComplexCriteria;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.Data.Grade;
import com.example.bigchallengesproject.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class EtalonActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    int etalonId;
    String[] checkMethods = new String[]{"Полное совпадение", "Поэлементное совпадение"};

    TextInputEditText etalonNameInput, etalonTasksCount;
    TextView etalonCreationDate;
    ImageView etalonIconPreview;
    Bitmap etalonIconBitmap;
    CardView uploadIconCard;
    androidx.gridlayout.widget.GridLayout answersEtalonTable, gradesEtalonTable;
    DatabaseHelper dbHelper;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etalon);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        etalonNameInput = findViewById(R.id.etalon_name_input);
        etalonTasksCount = findViewById(R.id.tasks_count_etalon);
        etalonCreationDate = findViewById(R.id.etalon_creation_date);
        etalonIconPreview = findViewById(R.id.etalon_icon_preview);
        uploadIconCard = findViewById(R.id.upload_icon_card);
        answersEtalonTable = findViewById(R.id.answers_etalon_table);
        gradesEtalonTable = findViewById(R.id.grades_etalon_table);
        dbHelper = new DatabaseHelper(this);

        ArrayAdapter<String> checkMethodsAdapter = new ArrayAdapter<>(EtalonActivity.this, R.layout.spinner_item, checkMethods);
        checkMethodsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        etalonId = getIntent().getIntExtra("etalon_id", -1);
        if (etalonId != -1) {
            Etalon etalon = dbHelper.getEtalonById(etalonId);
            if (etalon != null) {
                etalonNameInput.setText(etalon.getName());
                etalonTasksCount.setText(String.valueOf(etalon.getTasksCount()));
                etalonCreationDate.setText("Дата создания: " + etalon.getCreationDate());
                Bitmap bitmap = BitmapFactory.decodeByteArray(etalon.getIcon(), 0, etalon.getIcon().length);
                etalonIconBitmap = bitmap;
                etalonIconPreview.setImageBitmap(bitmap);
                answersEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_answers_header, null));
                List<Answer> answers = dbHelper.getAnswersByEtalonId(etalonId);
                for (Answer answer : answers) {
                    View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
                    ((TextView) row.findViewById(R.id.task_num)).setText(answer.getTaskNumber() + "");
                    ((EditText) row.findViewById(R.id.right_answer_input)).setText(answer.getRightAnswer());
                    ((EditText) row.findViewById(R.id.points_input)).setText(answer.getPoints());
                    ((MaterialCheckBox) row.findViewById(R.id.order_matters_checkbox)).setChecked(answer.getOrderMatters() == 1);
                    Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
                    androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                    CardView editComplexGradingTableCard = row.findViewById(R.id.edit_complex_grading_table_card);

                    checkMethodsDropdown.setAdapter(checkMethodsAdapter);

                    int selection = answer.getCheckMethod().equals("Полное совпадение") ? 0 : 1;
                    checkMethodsDropdown.setSelection(selection);

                    ((LinearLayout) editComplexGradingTableCard.getChildAt(0)).getChildAt(0).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_row, null), complexGradingTable.getChildCount() - 1);
                        }
                    });
                    ((LinearLayout) editComplexGradingTableCard.getChildAt(0)).getChildAt(1).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (complexGradingTable.getChildCount() > 4) complexGradingTable.removeViewAt(complexGradingTable.getChildCount() - 2);
                        }
                    });

                    checkMethodsDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @SuppressLint("InflateParams")
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            complexGradingTable.removeAllViews();
                            if (position == 1) {
                                List<ComplexCriteria> gradingList = dbHelper.getComplexGradingByAnswerId(answer.getId());
                                complexGradingTable.setVisibility(VISIBLE);
                                editComplexGradingTableCard.setVisibility(VISIBLE);

                                complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_header, null));
                                for (ComplexCriteria complexCriteria : gradingList) {
                                    View row2 = getLayoutInflater().inflate(R.layout.table_complex_grading_row, null);
                                    ((EditText) row2.findViewById(R.id.min_mistakes_input)).setText(complexCriteria.getMinMistakes() + "");
                                    ((EditText) row2.findViewById(R.id.max_mistakes_input)).setText(complexCriteria.getMaxMistakes() + "");
                                    ((EditText) row2.findViewById(R.id.complex_points_input)).setText(complexCriteria.getPoints());
                                    complexGradingTable.addView(row2);
                                }
                                complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_merged_row, null));
                            }
                            else {
                                complexGradingTable.setVisibility(GONE);
                                editComplexGradingTableCard.setVisibility(GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    answersEtalonTable.addView(row);
                }
                etalonTasksCount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            int newTasksCount = Integer.parseInt(s.toString().trim());
                            int oldTasksCount = answersEtalonTable.getChildCount() - 1;
                            int d = newTasksCount - oldTasksCount;
                            if (d > 0) {
                                while (d != 0) {
                                    View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
                                    ((TextView) row.findViewById(R.id.task_num)).setText(answersEtalonTable.getChildCount() + "");
                                    Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
                                    androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                                    CardView editComplexGradingTableCard = row.findViewById(R.id.edit_complex_grading_table_card);
                                    checkMethodsDropdown.setAdapter(checkMethodsAdapter);

                                    ((LinearLayout) editComplexGradingTableCard.getChildAt(0)).getChildAt(0).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_row, null), complexGradingTable.getChildCount() - 1);
                                        }
                                    });
                                    ((LinearLayout) editComplexGradingTableCard.getChildAt(0)).getChildAt(1).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (complexGradingTable.getChildCount() > 4) complexGradingTable.removeViewAt(complexGradingTable.getChildCount() - 2);
                                        }
                                    });

                                    checkMethodsDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @SuppressLint("InflateParams")
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                            complexGradingTable.removeAllViews();
                                            if (position == 1) {
                                                complexGradingTable.setVisibility(VISIBLE);
                                                editComplexGradingTableCard.setVisibility(VISIBLE);

                                                complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_header, null));
                                                View firstRow = getLayoutInflater().inflate(R.layout.table_complex_grading_row, null);
                                                ((EditText) firstRow.findViewById(R.id.min_mistakes_input)).setText("0");
                                                ((EditText) firstRow.findViewById(R.id.complex_points_input)).setText(((EditText) row.findViewById(R.id.points_input)).getText().toString().trim());
                                                complexGradingTable.addView(firstRow);
                                                complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_row, null));
                                                complexGradingTable.addView(getLayoutInflater().inflate(R.layout.table_complex_grading_merged_row, null));
                                            }
                                            else {
                                                complexGradingTable.setVisibility(GONE);
                                                editComplexGradingTableCard.setVisibility(GONE);
                                            }
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> parent) {}
                                    });
                                    answersEtalonTable.addView(row);
                                    d--;
                                }
                            }
                            else {
                                while (d != 0 && answersEtalonTable.getChildCount() > 2) {
                                    answersEtalonTable.removeViewAt(answersEtalonTable.getChildCount() - 1);
                                    d++;
                                }
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                });
                gradesEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_grades_header, null));
                List<Grade> grades = dbHelper.getGradesByEtalonId(etalonId);
                for (Grade grade : grades) {
                    androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_grades_row, null);
                    ((EditText) row.findViewById(R.id.min_points_input)).setText(grade.getMinPoints());
                    ((EditText) row.findViewById(R.id.max_points_input)).setText(grade.getMaxPoints());
                    ((TextView) row.findViewById(R.id.grade_view)).setText(grade.getGrade());
                    gradesEtalonTable.addView(row);
                }
            }
        }

        uploadIconCard.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                etalonIconBitmap = bitmap;
                etalonIconPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onExitFromEtalonClick(View view) {
        startActivity(new Intent(EtalonActivity.this, StorageActivity.class));
    }

    public boolean isTablesCorrect() {
        for (int i = 1; i < answersEtalonTable.getChildCount(); i++) {
            View row = answersEtalonTable.getChildAt(i);
            EditText answer = row.findViewById(R.id.right_answer_input);
            EditText points = row.findViewById(R.id.points_input);
            Spinner checkMethodDropdown = row.findViewById(R.id.check_method_dropdown);
            if (answer.getText().toString().isEmpty()) return false;
            try {
                Double.parseDouble(points.getText().toString().trim());
            } catch (NumberFormatException e) {
                return false;
            }
            if (checkMethodDropdown.getSelectedItem().equals("Поэлементное совпадение")) {
                androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                for (int j = 1; j < complexGradingTable.getChildCount() - 1; j++) {
                    View row2 = complexGradingTable.getChildAt(j);
                    EditText minMistakesInput = row2.findViewById(R.id.min_mistakes_input);
                    EditText maxMistakesInput = row2.findViewById(R.id.max_mistakes_input);
                    EditText complexPointsInput = row2.findViewById(R.id.complex_points_input);
                    try {
                        Integer.parseInt(minMistakesInput.getText().toString().trim());
                        Integer.parseInt(maxMistakesInput.getText().toString().trim());
                        Double.parseDouble(complexPointsInput.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
        }
        for (int i = 1; i < gradesEtalonTable.getChildCount(); i++) {
            View row = gradesEtalonTable.getChildAt(i);
            EditText minPoints = row.findViewById(R.id.min_points_input);
            EditText maxPoints = row.findViewById(R.id.max_points_input);
            try {
                Double.parseDouble(minPoints.getText().toString().trim());
                Double.parseDouble(maxPoints.getText().toString().trim());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
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
            if (newName.isEmpty() || !isTablesCorrect()) {
                Toast.makeText(this, "Заполните данные эталона корректно!", Toast.LENGTH_SHORT).show();
                return;
            }
            ContentValues etalonValues = new ContentValues();
            etalonValues.put(DatabaseHelper.COLUMN_NAME, newName);
            etalonValues.put(DatabaseHelper.COLUMN_TASKS_COUNT, newTasksCount);
            etalonValues.put(DatabaseHelper.COLUMN_ICON, iconBytes);

            db.update(DatabaseHelper.TABLE_ETALONS, etalonValues,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(etalonId)});
            Cursor cursor = db.query(DatabaseHelper.TABLE_ANSWERS,
                    new String[]{DatabaseHelper.COLUMN_ANSWER_ID},
                    DatabaseHelper.COLUMN_ETALON_ID + "=?",
                    new String[]{String.valueOf(etalonId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    int answerId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ANSWER_ID));
                    db.delete(DatabaseHelper.TABLE_COMPLEX_GRADING, DatabaseHelper.COLUMN_COMPLEX_GRADING_ANSWER_ID + "=?", new String[]{String.valueOf(answerId)});
                } while (cursor.moveToNext());
            }
            db.delete(DatabaseHelper.TABLE_ANSWERS, DatabaseHelper.COLUMN_ETALON_ID + "=?",
                    new String[]{String.valueOf(etalonId)});
            db.delete(DatabaseHelper.TABLE_GRADES, DatabaseHelper.COLUMN_ETALON_ID + "=?",
                    new String[]{String.valueOf(etalonId)});

            for (int i = 1; i < answersEtalonTable.getChildCount(); i++) {
                View row = answersEtalonTable.getChildAt(i);
                TextView taskNumInput = row.findViewById(R.id.task_num);
                EditText rightAnswerInput = row.findViewById(R.id.right_answer_input);
                EditText pointsInput = row.findViewById(R.id.points_input);
                MaterialCheckBox checkBox = row.findViewById(R.id.order_matters_checkbox);
                Spinner checkMethodDropdown = row.findViewById(R.id.check_method_dropdown);
                int taskNum = Integer.parseInt(taskNumInput.getText().toString().trim());
                String rightAnswer = rightAnswerInput.getText().toString().trim();
                String points = pointsInput.getText().toString().trim();
                int orderMatters = checkBox.isChecked() ? 1 : 0;
                String checkMethod = (String) checkMethodDropdown.getSelectedItem();
                long answerId = dbHelper.addAnswer(etalonId, taskNum, rightAnswer, points, orderMatters, checkMethod);
                if (answerId != -1 && checkMethod.equals("Поэлементное совпадение")) {
                    androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                    for (int j = 1; j < complexGradingTable.getChildCount() - 1; j++) {
                        View row2 = complexGradingTable.getChildAt(j);
                        EditText minMistakesInput = row2.findViewById(R.id.min_mistakes_input);
                        EditText maxMistakesInput = row2.findViewById(R.id.max_mistakes_input);
                        EditText complexPointsInput = row2.findViewById(R.id.complex_points_input);
                        int minMistakes = Integer.parseInt(minMistakesInput.getText().toString().trim());
                        int maxMistakes = Integer.parseInt(maxMistakesInput.getText().toString().trim());
                        String complexPoints = complexPointsInput.getText().toString().trim();
                        dbHelper.addComplexCriteria((int) answerId, minMistakes, maxMistakes, complexPoints);
                    }
                }
            }
            for (int i = 1; i < gradesEtalonTable.getChildCount(); i++) {
                View row = gradesEtalonTable.getChildAt(i);
                EditText minPointsInput = row.findViewById(R.id.min_points_input);
                EditText maxPointsInput = row.findViewById(R.id.max_points_input);
                TextView gradeView = row.findViewById(R.id.grade_view);
                String minPoints = minPointsInput.getText().toString().trim();
                String maxPoints = maxPointsInput.getText().toString().trim();
                String grade = gradeView.getText().toString().trim();
                dbHelper.addGrade(etalonId, minPoints, maxPoints, grade);
            }
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