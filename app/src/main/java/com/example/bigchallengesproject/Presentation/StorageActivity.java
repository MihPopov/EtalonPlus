package com.example.bigchallengesproject.Presentation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bigchallengesproject.Common.DatabaseHelper;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StorageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    String[] checkMethods = new String[]{"Полное совпадение", "Поэлементное совпадение"};

    ScrollView scrollView;
    RecyclerView recyclerView;
    EtalonsAdapter etalonsAdapter;
    DatabaseHelper dbHelper;
    List<Etalon> etalonList;
    CardView uploadEtalonIconCard;
    ImageView etalonIcon;
    Bitmap etalonIconBitmap;
    LinearLayout etalonCreationPage, etalonTablesCreationPage;
    TextInputEditText etalonNameInput, tasksCountInput;
    androidx.gridlayout.widget.GridLayout answersEtalonTable, gradesEtalonTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        scrollView = findViewById(R.id.etalon_scrollview);
        recyclerView = findViewById(R.id.recycler_view_etalons);
        uploadEtalonIconCard = findViewById(R.id.upload_etalon_icon_card);
        etalonNameInput = findViewById(R.id.etalon_creation_name_input);
        etalonIcon = findViewById(R.id.icon_preview_creation);
        etalonCreationPage = findViewById(R.id.etalon_creation_page);
        etalonTablesCreationPage = findViewById(R.id.etalon_tables_creation_page);
        tasksCountInput = findViewById(R.id.tasks_count_etalon_creation);
        answersEtalonTable = findViewById(R.id.answers_etalon_creation_table);
        gradesEtalonTable = findViewById(R.id.grades_etalon_creation_table);

        etalonIconBitmap = ((BitmapDrawable) getDrawable(R.drawable.etalon_plus)).getBitmap();

        dbHelper = new DatabaseHelper(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etalonList = dbHelper.getAllEtalons();
        etalonsAdapter = new EtalonsAdapter(this, etalonList, new EtalonsAdapter.OnEtalonDeleteListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDelete(int position) {
                int etalonId = etalonList.get(position).getId();
                dbHelper.deleteEtalon(etalonId);
                etalonList.remove(position);
                etalonsAdapter.notifyItemRemoved(position);
            }
        });
        recyclerView.setAdapter(etalonsAdapter);

        uploadEtalonIconCard.setOnClickListener(new View.OnClickListener() {
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
                etalonIconBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                etalonIcon.setImageBitmap(etalonIconBitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onExitFromStorageClick(View view) {
        startActivity(new Intent(StorageActivity.this, HomeActivity.class));
    }

    public void onEtalonCreateClick(View view) {
        etalonCreationPage.setVisibility(VISIBLE);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, etalonCreationPage.getBottom());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void onGenerateTablesClick(View view) {
        int tasksCount;
        try {
            tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Напишите количество заданий корреткно!", Toast.LENGTH_SHORT).show();
            return;
        }
        etalonTablesCreationPage.setVisibility(VISIBLE);
        ArrayAdapter<String> checkMethodsAdapter = new ArrayAdapter<>(StorageActivity.this, R.layout.spinner_item, checkMethods);
        checkMethodsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        answersEtalonTable.removeAllViews();
        answersEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_answers_header, null));
        for (int i = 1; i <= tasksCount; i++) {
            View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
            ((TextView) row.findViewById(R.id.task_num)).setText(i + "");
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
        }
        tasksCountInput.addTextChangedListener(new TextWatcher() {
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
        List<String> gradesSystem = dbHelper.getGradesSystem();
        for (String grade : gradesSystem) {
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_grades_row, null);
            ((TextView) row.findViewById(R.id.grade_view)).setText(grade);
            gradesEtalonTable.addView(row);
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, etalonTablesCreationPage.getBottom());
            }
        });
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

    @SuppressLint("NotifyDataSetChanged")
    public void onCreateEtalonClick(View view) {
        if (!isTablesCorrect()) {
            Toast.makeText(this, "Проверьте корректность заполнения таблиц!", Toast.LENGTH_SHORT).show();
            return;
        }
        int tasksCount;
        try {
            tasksCount = Integer.parseInt(tasksCountInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Напишите количество заданий корректно!", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = etalonNameInput.getText().toString().trim();
        if (name.isEmpty() || etalonIconBitmap == null) {
            Toast.makeText(this, "Заполните данные эталона корректно!", Toast.LENGTH_SHORT).show();
            return;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        etalonIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] iconBytes = stream.toByteArray();
        long etalonId = dbHelper.addEtalon(name, iconBytes, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()), tasksCount);
        if (etalonId != -1) {
            for (int i = 1; i < answersEtalonTable.getChildCount(); i++) {
                View row = answersEtalonTable.getChildAt(i);
                TextView taskNumView = row.findViewById(R.id.task_num);
                EditText rightAnswerInput = row.findViewById(R.id.right_answer_input);
                EditText pointsInput = row.findViewById(R.id.points_input);
                MaterialCheckBox checkBox = row.findViewById(R.id.order_matters_checkbox);
                Spinner checkMethodDropdown = row.findViewById(R.id.check_method_dropdown);
                int taskNum = Integer.parseInt(taskNumView.getText().toString().trim());
                String rightAnswer = rightAnswerInput.getText().toString().trim();
                String points = pointsInput.getText().toString().trim();
                int orderMatters = checkBox.isChecked() ? 1 : 0;
                String checkMethod = (String) checkMethodDropdown.getSelectedItem();
                long answerId = dbHelper.addAnswer((int) etalonId, taskNum, rightAnswer, points, orderMatters, checkMethod);
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
                androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) gradesEtalonTable.getChildAt(i);
                EditText minPointsInput = row.findViewById(R.id.min_points_input);
                EditText maxPointsInput = row.findViewById(R.id.max_points_input);
                TextView gradeView = row.findViewById(R.id.grade_view);
                String minPoints = minPointsInput.getText().toString().trim();
                String maxPoints = maxPointsInput.getText().toString().trim();
                String grade = gradeView.getText().toString().trim();
                dbHelper.addGrade((int) etalonId, minPoints, maxPoints, grade);
            }

            etalonList.clear();
            etalonList.addAll(dbHelper.getAllEtalons());
            etalonsAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Эталон создан!", Toast.LENGTH_SHORT).show();
        }
    }
}