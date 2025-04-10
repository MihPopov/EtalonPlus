package com.example.bigchallengesproject.Activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bigchallengesproject.Common.DatabaseHelper;
import com.example.bigchallengesproject.Common.SimpleCallback;
import com.example.bigchallengesproject.Common.TrOCR;
import com.example.bigchallengesproject.Data.Answer;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.Data.EtalonsUseAdapter;
import com.example.bigchallengesproject.Data.Grade;
import com.example.bigchallengesproject.Data.WorksAdapter;
import com.example.bigchallengesproject.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opencv.android.OpenCVLoader;
//import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CheckActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int PICK_ETALON_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSION_CODE = 100;
    int k = 0;

    CardView uploadCard, cameraCard, tablesPageCard, templateSaveCard, etalonSaveCard;
    RecyclerView worksRecyclerView, etalonsRecyclerView;
    WorksAdapter worksAdapter;
    EtalonsUseAdapter etalonsUseAdapter;
    ScrollView scrollView;
    LinearLayout tablesPage, recognizedTextPage, recognizedTextTablesLayout, checkWorksLayout, etalonCreationLayout, templateCreationLayout, etalonsListLayout;
    androidx.gridlayout.widget.GridLayout answersTable, gradesTable, resultsTable, cheatersTable, recTextTable;
    TextInputEditText tasksCountInput, etalonNameInput;
    ImageView etalonIcon;

    List<Uri> workUris;
    List<Long> workSizes;
    List<Etalon> etalonList;
    private Uri cameraImageUri;
    private TrOCR trocr;
    DatabaseHelper dbHelper;
    SharedPreferences settings;
    Bitmap etalonIconBitmap;
    List<Grade> grades;
    boolean cardGone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        scrollView = findViewById(R.id.scrollview);
        uploadCard = findViewById(R.id.upload_card);
        cameraCard = findViewById(R.id.camera_card);
        worksRecyclerView = findViewById(R.id.recycler_view_works);
        templateCreationLayout = findViewById(R.id.template_creation_layout);
        etalonsListLayout = findViewById(R.id.etalons_list_layout);
        etalonsRecyclerView = findViewById(R.id.recycler_view_etalons_available);
        tablesPageCard = findViewById(R.id.tables_page_card);
        tablesPage = findViewById(R.id.tables_page);
        answersTable = findViewById(R.id.answers_table);
        gradesTable = findViewById(R.id.grades_table);
        tasksCountInput = findViewById(R.id.tasks_count_input);
        recognizedTextPage = findViewById(R.id.recognized_text_page);
        recognizedTextTablesLayout = findViewById(R.id.rec_tables_layout);
        checkWorksLayout = findViewById(R.id.check_works_page);
        resultsTable = findViewById(R.id.results_table);
        cheatersTable = findViewById(R.id.cheaters_table);
        templateSaveCard = findViewById(R.id.template_save_card);
        etalonCreationLayout = findViewById(R.id.etalon_creation_layout);
        etalonNameInput = findViewById(R.id.etalon_check_name_input);
        etalonIcon = findViewById(R.id.icon_preview_check);
        etalonSaveCard = findViewById(R.id.save_etalon_card);

        settings = getSharedPreferences("Preferences", MODE_PRIVATE);

        dbHelper = new DatabaseHelper(this);
        workUris = new ArrayList<>();
        workSizes = new ArrayList<>();
        worksAdapter = new WorksAdapter(this, workUris, workSizes, new WorksAdapter.OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDeleteClick(int position) {
                workUris.remove(position);
                workSizes.remove(position);
                worksAdapter.notifyDataSetChanged();
            }
        });
        worksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        worksRecyclerView.setAdapter(worksAdapter);

        etalonList = dbHelper.getAllEtalons();
        etalonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        etalonsUseAdapter = new EtalonsUseAdapter(this, etalonList, new EtalonsUseAdapter.OnEtalonUseListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onUse(Etalon etalon) {
                onCreateTemplateClick(null);
                tasksCountInput.setText(etalon.getTasksCount() + "");
                tablesPageCard.setVisibility(GONE);
                List<Answer> answers = dbHelper.getAnswersByEtalonId(etalon.getId());
                grades = dbHelper.getGradesByEtalonId(etalon.getId());
                onTablesPageClick(null);
                for (int i = 1; i < answersTable.getChildCount(); i++) {
                    View row = answersTable.getChildAt(i);
                    ((EditText) row.findViewById(R.id.right_answer_input)).setText(answers.get(i - 1).getRightAnswer());
                    ((EditText) row.findViewById(R.id.points_input)).setText(answers.get(i - 1).getPoints() + "");
                }
                cardGone = true;
                grades = null;
            }
        });
        etalonsRecyclerView.setAdapter(etalonsUseAdapter);

        etalonIconBitmap = ((BitmapDrawable) getDrawable(R.drawable.etalon_plus)).getBitmap();

        if (!OpenCVLoader.initLocal()) {
            Log.e("OpenCV", "Ошибка загрузки OpenCV!");
        } else {
            Log.d("OpenCV", "OpenCV загружен успешно!");
        }

        try {
            trocr = new TrOCR(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        uploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGES_REQUEST);
            }
        });

        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGES_REQUEST && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri workUri = data.getClipData().getItemAt(i).getUri();
                        addWorkToList(workUri);
                    }
                } else if (data.getData() != null) addWorkToList(data.getData());
            }
            else if (requestCode == CAPTURE_IMAGE_REQUEST && cameraImageUri != null) addWorkToList(cameraImageUri);
            else if (requestCode == PICK_ETALON_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                try {
                    etalonIconBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    etalonIcon.setImageBitmap(etalonIconBitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor != null ? cursor.getColumnIndex(OpenableColumns.SIZE) : -1;
        long size = 0;
        if (cursor != null && sizeIndex != -1) {
            cursor.moveToFirst();
            size = cursor.getLong(sizeIndex);
            cursor.close();
        }
        return size;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addWorkToList(Uri workUri) {
        long fileSize = getFileSize(workUri);
        workUris.add(workUri);
        workSizes.add(fileSize);
        worksAdapter.notifyDataSetChanged();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else openCamera();
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera();
            else Toast.makeText(this, "Разрешение на использование камеры не предоставлено!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    public void onTablesPageClick(View view) {
        int tasksCount;
        try {
            tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Напишите количество заданий корреткно!", Toast.LENGTH_SHORT).show();
            return;
        }
        tablesPage.setVisibility(VISIBLE);
        answersTable.removeAllViews();
        answersTable.addView(getLayoutInflater().inflate(R.layout.table_answers_header, null));
        for (int i = 1; i <= tasksCount; i++) {
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_answers_row, null);
            ((TextView) row.findViewById(R.id.task_num)).setText(i + "");
            answersTable.addView(row);
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
                    int oldTasksCount = answersTable.getChildCount() - 1;
                    int d = newTasksCount - oldTasksCount;
                    if (d > 0) {
                        while (d != 0) {
                            View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
                            ((TextView) row.findViewById(R.id.task_num)).setText(answersTable.getChildCount() + "");
                            answersTable.addView(row);
                            d--;
                        }
                    }
                    else {
                        while (d != 0 && answersTable.getChildCount() > 2) {
                            answersTable.removeViewAt(answersTable.getChildCount() - 1);
                            d++;
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        });
        gradesTable.removeAllViews();
        gradesTable.addView(getLayoutInflater().inflate(R.layout.table_grades_header, null));
        if (grades != null) {
            for (Grade grade : grades) {
                androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_grades_row, null);
                ((EditText) row.findViewById(R.id.min_points_input)).setText(grade.getMinPoints() + "");
                ((EditText) row.findViewById(R.id.max_points_input)).setText(grade.getMaxPoints() + "");
                ((TextView) row.findViewById(R.id.grade_view)).setText(grade.getGrade());
                gradesTable.addView(row);
            }
        }
        else {
            List<String> gradesSystem = dbHelper.getGradesSystem();
            for (String grade : gradesSystem) {
                androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_grades_row, null);
                ((TextView) row.findViewById(R.id.grade_view)).setText(grade);
                gradesTable.addView(row);
            }
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, tablesPage.getBottom());
            }
        });
    }

    private HashMap<Integer, String> parseRecognizedText(List<String> recognizedText) {
        HashMap<Integer, String> answersMap = new HashMap<>();
        for (String block : recognizedText) {
            block = block.replace(".", " ");
            block = block.replaceAll("\\s+", " ").trim();
            if (!block.isEmpty()) {
                int spaceIndex = block.indexOf(' ');
                if (spaceIndex != -1) {
                    try {
                        int taskNumber = Integer.parseInt(block.substring(0, spaceIndex).trim());
                        String answer = block.substring(spaceIndex + 1).trim();
                        answersMap.put(taskNumber, answer);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return answersMap;
    }

    @SuppressLint("SetTextI18n")
    public void onRecognizedTextPageClick(View view) throws Exception {
        if (workUris.isEmpty()) {
            Toast.makeText(this, "Загрузите работы для проверки!", Toast.LENGTH_SHORT).show();
            return;
        }
        recognizedTextPage.setVisibility(VISIBLE);
        recognizedTextTablesLayout.removeAllViews();
        recTextTable = new androidx.gridlayout.widget.GridLayout(this);
        recTextTable.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        recTextTable.setColumnCount(1);
        recTextTable.setBackgroundColor(getResources().getColor(R.color.dark_green));
        recognizedTextTablesLayout.addView(recTextTable);
        for (int i = 0; i < workUris.size(); i++) {
            Uri imageUri = workUris.get(i);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int finalI = i;
                trocr.recognizeTextBlock(bitmap, new SimpleCallback<List<String>>() {
                    @Override
                    public void onLoad(List<String> data) {
                        recTextTable.addView(getLayoutInflater().inflate(R.layout.table_recognized_text_header, null));
                        if (data != null && !data.isEmpty()) {
                            if (k == 3) {
                                recTextTable = new GridLayout(CheckActivity.this);
                                recTextTable.setLayoutParams(new ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT));
                                recTextTable.setColumnCount(1);
                                recTextTable.setBackgroundColor(getResources().getColor(R.color.dark_green));
                                recTextTable.addView(getLayoutInflater().inflate(R.layout.table_recognized_text_header, null));
                                recognizedTextTablesLayout.addView(recTextTable);
                                k = 0;
                            }
                            GridLayout mergedRow = (GridLayout) getLayoutInflater().inflate(R.layout.table_recognized_text_merged_row, null);
                            ((TextView) mergedRow.findViewById(R.id.work_name)).setText("Работа №" + (finalI + 1));
                            recTextTable.addView(mergedRow);
                            HashMap<Integer, String> answersMap = parseRecognizedText(data);
                            for (int j = 1; j <= Integer.parseInt(tasksCountInput.getText().toString()); j++) {
                                GridLayout row = (GridLayout) getLayoutInflater().inflate(R.layout.table_recognized_text_row, null);
                                ((TextView) row.findViewById(R.id.rec_task_num)).setText(j + "");
                                ((EditText) row.findViewById(R.id.rec_answer_input)).setText((answersMap.get(j) + "").replace("null", ""));
                                recTextTable.addView(row);
                            }
                            k++;
                        }
                        k = 0;
                    }
                });
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, recognizedTextPage.getBottom());
            }
        });
    }

    public HashMap<Integer, Map<String, Object>> detectCheaters(Map<String, Map<Integer, String>> studentAnswers,
            HashMap<Integer, Pair<String, Integer>> answersMap) {
        Map<Integer, Set<String>> cheaterGroups = new HashMap<>();
        HashMap<Integer, Map<String, Object>> cheaterInfoMap = new HashMap<>();
        Map<String, Integer> workToGroup = new HashMap<>();
        int groupNumber = 1;
        List<String> works = new ArrayList<>(studentAnswers.keySet());
        double averageTaskWeight = answersMap.values().stream()
                .mapToInt(pair -> pair.second)
                .average()
                .orElse(0);
        Map<Integer, Map<String, Set<String>>> taskMatches = new HashMap<>();
        for (String work : works) {
            Map<Integer, String> answers = studentAnswers.get(work);
            for (Map.Entry<Integer, Pair<String, Integer>> entry : answersMap.entrySet()) {
                int taskNumber = entry.getKey();
                String correctAnswer = entry.getValue().first;
                String studentAnswer = answers.get(taskNumber);
                if (studentAnswer != null && !studentAnswer.equals(correctAnswer)) {
                    taskMatches.computeIfAbsent(taskNumber, k -> new HashMap<>())
                            .computeIfAbsent(studentAnswer, k -> new HashSet<>())
                            .add(work);
                }
            }
        }
        for (Map.Entry<Integer, Map<String, Set<String>>> taskEntry : taskMatches.entrySet()) {
            int taskNumber = taskEntry.getKey();
            int taskWeight = answersMap.get(taskNumber).second;
            for (Set<String> group : taskEntry.getValue().values()) {
                if (group.size() < 2) continue;
                Integer existingGroup = null;
                for (String work : group) {
                    if (workToGroup.containsKey(work)) {
                        existingGroup = workToGroup.get(work);
                        break;
                    }
                }
                if (existingGroup != null) {
                    cheaterGroups.get(existingGroup).addAll(group);
                    Map<String, Object> cheaterInfo = cheaterInfoMap.get(existingGroup);
                    cheaterInfo.put("works", new ArrayList<>(cheaterGroups.get(existingGroup)));
                    cheaterInfo.put("totalWeight", (int) cheaterInfo.get("totalWeight") + taskWeight);
                    cheaterInfo.put("groupSize", cheaterGroups.get(existingGroup).size());
                    for (String work : group) {
                        workToGroup.put(work, existingGroup);
                    }
                    updateCheatingProbability(cheaterInfo, averageTaskWeight);
                } else {
                    cheaterGroups.put(groupNumber, new HashSet<>(group));
                    Map<String, Object> cheaterInfo = new HashMap<>();
                    cheaterInfo.put("works", new ArrayList<>(group));
                    cheaterInfo.put("totalWeight", taskWeight);
                    cheaterInfo.put("groupSize", group.size());
                    cheaterInfoMap.put(groupNumber, cheaterInfo);
                    for (String work : group) {
                        workToGroup.put(work, groupNumber);
                    }
                    updateCheatingProbability(cheaterInfo, averageTaskWeight);
                    groupNumber++;
                }
            }
        }
        return cheaterInfoMap;
    }

    private void updateCheatingProbability(Map<String, Object> cheaterInfo, double averageTaskWeight) {
        int groupSize = (int) cheaterInfo.get("groupSize");
        int totalWeight = (int) cheaterInfo.get("totalWeight");
        String probability;
        if (groupSize >= 5) probability = "Высокая";
        else if (groupSize == 4) {
            if (totalWeight >= averageTaskWeight * 3) probability = "Высокая";
            else probability = "Средняя";
        }
        else {
            if (totalWeight >= averageTaskWeight * 5) probability = "Высокая";
            else if (totalWeight >= averageTaskWeight * 3) probability = "Средняя";
            else probability = "Низкая";
        }
        cheaterInfo.put("probability", probability);
    }

    @SuppressLint("SetTextI18n")
    public void onCheckWorksPageClick(View view) {
        if (!isTablesCorrect()) {
            Toast.makeText(this, "Проверьте корректность заполнения таблиц!", Toast.LENGTH_SHORT).show();
            return;
        }
        checkWorksLayout.setVisibility(VISIBLE);
        resultsTable.removeAllViews();
        cheatersTable.removeAllViews();
        if (cardGone) templateSaveCard.setVisibility(GONE);

        HashMap<Integer, Pair<String, Integer>> answersMap = new HashMap<>();
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) answersTable.getChildAt(i);
            EditText answerInput = row.findViewById(R.id.right_answer_input);
            EditText pointsInput = row.findViewById(R.id.points_input);
            String answer = answerInput.getText().toString().trim();
            int points = Integer.parseInt(pointsInput.getText().toString().trim());
            answersMap.put(i, new Pair<>(answer, points));
        }

        resultsTable.addView(getLayoutInflater().inflate(R.layout.table_results_header_main, null));
        androidx.gridlayout.widget.GridLayout header = resultsTable.findViewById(R.id.table_results_title_main);
        int tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        header.setColumnCount(tasksCount + 2);
        for (int i = 1; i <= tasksCount; i++) {
            androidx.gridlayout.widget.GridLayout taskElement = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_header_task, null);
            ((TextView) taskElement.findViewById(R.id.res_task_num)).setText("Задание " + i);
            header.addView(taskElement);
        }
        header.addView(getLayoutInflater().inflate(R.layout.table_results_header_end, null));
        Map<String, Map<Integer, String>> studentAnswers = new HashMap<>();
        for (int i = 1; i <= workUris.size(); i++) {
            Map<Integer, String> answers = new HashMap<>();
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_row_main, null);
            row.setColumnCount(tasksCount + 2);
            ((TextView) row.findViewById(R.id.res_work_name)).setText("Работа №" + i);
            int pointsSum = 0;

            int tableIndex = (i - 1) / 3;
            int workOffset = (i - 1) % 3;
            androidx.gridlayout.widget.GridLayout workTable = (androidx.gridlayout.widget.GridLayout) recognizedTextTablesLayout.getChildAt(tableIndex);
            int workRowIndex = 1 + workOffset * (tasksCount + 1);
            for (int t = 1; t <= tasksCount; t++) {
                EditText answerInput = workTable.getChildAt(workRowIndex + t).findViewById(R.id.rec_answer_input);
                String recognizedAnswer = answerInput.getText().toString().trim();
                androidx.gridlayout.widget.GridLayout taskElement = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_row_task, null);
                String rightAnswer = answersMap.get(t).first;
                int points = 0;
                if (recognizedAnswer.equals(rightAnswer)) points = answersMap.get(t).second;
                ((TextView) taskElement.findViewById(R.id.res_answer)).setText(recognizedAnswer);
                ((TextView) taskElement.findViewById(R.id.res_points)).setText(points + "");
                pointsSum += points;
                row.addView(taskElement);
                answers.put(t, recognizedAnswer);
            }
            studentAnswers.put("Работа №" + i, answers);

            androidx.gridlayout.widget.GridLayout rowEnd = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_row_end, null);
            ((TextView) rowEnd.findViewById(R.id.points_sum)).setText(pointsSum + "");
            for (int j = 1; j <= gradesTable.getChildCount(); j++) {
                androidx.gridlayout.widget.GridLayout gradeRow = (androidx.gridlayout.widget.GridLayout) gradesTable.getChildAt(j);
                int min = Integer.parseInt(((EditText) gradeRow.findViewById(R.id.min_points_input)).getText().toString());
                int max = Integer.parseInt(((EditText) gradeRow.findViewById(R.id.max_points_input)).getText().toString());
                if (min <= pointsSum && pointsSum <= max) {
                    String grade = ((TextView) gradeRow.findViewById(R.id.grade_view)).getText().toString();
                    ((TextView) rowEnd.findViewById(R.id.mark)).setText(grade);
                    break;
                }
            }

            row.addView(rowEnd);
            resultsTable.addView(row);
        }
        cheatersTable.addView(getLayoutInflater().inflate(R.layout.table_cheaters_header, null));
        HashMap<Integer, Map<String, Object>> cheatersGroups = detectCheaters(studentAnswers, answersMap);
        for (int i = 1; i <= cheatersGroups.size(); i++) {
            Map<String, Object> group = cheatersGroups.get(i);
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_cheaters_row, null);
            ((TextView) row.findViewById(R.id.group_num)).setText(i + "");
            List<String> works = (List<String>) group.get("works");
            ((TextView) row.findViewById(R.id.works)).setText(String.join(", ", works));
            ((TextView) row.findViewById(R.id.group_size)).setText(group.get("groupSize") + "");
            ((TextView) row.findViewById(R.id.matches_points_sum)).setText(group.get("totalWeight") + "");
            ((TextView) row.findViewById(R.id.probability)).setText(group.get("probability") + "");
            cheatersTable.addView(row);
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, checkWorksLayout.getBottom());
            }
        });
    }

    public boolean isTablesCorrect() {
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            View row = answersTable.getChildAt(i);
            EditText answer = row.findViewById(R.id.right_answer_input);
            EditText points = row.findViewById(R.id.points_input);
            if (answer.getText().toString().isEmpty()) return false;
            try {
                int p = Integer.parseInt(points.getText().toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        for (int i = 1; i < gradesTable.getChildCount(); i++) {
            View row = gradesTable.getChildAt(i);
            EditText minPoints = row.findViewById(R.id.min_points_input);
            EditText maxPoints = row.findViewById(R.id.max_points_input);
            try {
                int p = Integer.parseInt(minPoints.getText().toString());
                p = Integer.parseInt(maxPoints.getText().toString());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public void updateColumnsWidth(Sheet table, int colCount) {
        Map<Integer, Integer> columnWidths = new HashMap<>();
        for (int col = 0; col < colCount; col++) {
            int maxLength = 0;
            for (int i = 0; i < table.getLastRowNum(); i++) {
                String cellContent = String.valueOf(table.getRow(i).getCell(col));
                if (cellContent.length() > maxLength) maxLength = cellContent.length();
            }
            int baseWidth = maxLength * 300 + 300;
            columnWidths.put(col, baseWidth);
        }
        for (int i = 0; i < table.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = table.getMergedRegion(i);
            int firstCol = mergedRegion.getFirstColumn();
            int lastCol = mergedRegion.getLastColumn();
            int numCols = lastCol - firstCol + 1;
            int totalWidth = 0;
            for (int c = firstCol; c <= lastCol; c++) {
                totalWidth += columnWidths.getOrDefault(c, 0);
            }
            int avgWidth = totalWidth / numCols;
            for (int c = firstCol; c <= lastCol; c++) {
                columnWidths.put(c, avgWidth);
            }
        }
        for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet()) {
            table.setColumnWidth(entry.getKey(), entry.getValue());
        }
    }

    public void onDownloadResultsClick(View view) {
        boolean isOneTableEnabled = settings.getBoolean("isOneTableEnabled", false);
        boolean isOneSheetEnabled = isOneTableEnabled && settings.getBoolean("isOneSheetEnabled", false);
        int lastRow = 0;
        String fileName = settings.getString("tableName", "Результаты проверки").replace(".xlsx", "") + ".xlsx";
        Workbook table;
        Uri fileUri = null;
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        if (isOneTableEnabled) {
            String selection = MediaStore.Downloads.DISPLAY_NAME + " = ?";
            String[] selectionArgs = new String[]{fileName};
            Cursor cursor = getContentResolver().query(collection, new String[]{MediaStore.Downloads._ID}, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
                fileUri = ContentUris.withAppendedId(collection, id);
                try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
                    table = new XSSFWorkbook(inputStream);
                } catch (IOException e) {
                    table = new XSSFWorkbook();
                }
            } else {
                table = new XSSFWorkbook();
            }
            if (cursor != null) cursor.close();
        }
        else table = new XSSFWorkbook();
        if (fileUri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            values.put(MediaStore.Downloads.IS_PENDING, 1);
            fileUri = getContentResolver().insert(collection, values);
        }
        Sheet sheet;
        if (isOneSheetEnabled || table.getNumberOfSheets() == 0) {
            sheet = table.getSheet("Результаты");
            if (sheet == null) sheet = table.createSheet("Результаты");
            lastRow = sheet.getLastRowNum() + 2;
            if (lastRow == 1) lastRow -= 1;
        }
        else sheet = table.createSheet("Результаты " + (table.getNumberOfSheets() + 1));

        CellStyle commonStyle = table.createCellStyle();
        commonStyle.setBorderTop(BorderStyle.MEDIUM);
        commonStyle.setBorderBottom(BorderStyle.MEDIUM);
        commonStyle.setBorderLeft(BorderStyle.MEDIUM);
        commonStyle.setBorderRight(BorderStyle.MEDIUM);
        commonStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle greenBackground = table.createCellStyle();
        greenBackground.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        greenBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        greenBackground.setBorderTop(BorderStyle.MEDIUM);
        greenBackground.setBorderBottom(BorderStyle.MEDIUM);
        greenBackground.setBorderLeft(BorderStyle.MEDIUM);
        greenBackground.setBorderRight(BorderStyle.MEDIUM);
        greenBackground.setAlignment(HorizontalAlignment.CENTER);

        CellStyle redBackground = table.createCellStyle();
        redBackground.setFillForegroundColor(IndexedColors.RED.getIndex());
        redBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        redBackground.setBorderTop(BorderStyle.MEDIUM);
        redBackground.setBorderBottom(BorderStyle.MEDIUM);
        redBackground.setBorderLeft(BorderStyle.MEDIUM);
        redBackground.setBorderRight(BorderStyle.MEDIUM);
        redBackground.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow1 = sheet.createRow(lastRow);
        lastRow++;
        Row headerRow2 = sheet.createRow(lastRow);
        Cell cell = headerRow1.createCell(0);
        cell.setCellValue("Работа");
        cell.setCellStyle(commonStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow, 0, 0));
        int tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        int k = 0;
        for (int i = 1; i <= tasksCount * 2 - 1; i+=2) {
            cell = headerRow1.createCell(i);
            cell.setCellValue("Задание " + (i - k));
            cell.setCellStyle(commonStyle);
            sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow - 1, i, i + 1));
            cell = headerRow2.createCell(i);
            cell.setCellValue("Ответ");
            cell.setCellStyle(commonStyle);
            cell = headerRow2.createCell(i + 1);
            cell.setCellValue("Балл");
            cell.setCellStyle(commonStyle);
            k++;
        }
        cell = headerRow1.createCell(tasksCount * 2 + 1);
        cell.setCellValue("Сумма баллов");
        cell.setCellStyle(commonStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow, tasksCount * 2 + 1, tasksCount * 2 + 1));
        cell = headerRow1.createCell(tasksCount * 2 + 2);
        cell.setCellValue("Оценка");
        cell.setCellStyle(commonStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow, tasksCount * 2 + 2, tasksCount * 2 + 2));

        for (int i = 1; i <= workUris.size(); i++) {
            k = 0;
            androidx.gridlayout.widget.GridLayout workRow = (androidx.gridlayout.widget.GridLayout) resultsTable.getChildAt(i);
            String name = ((TextView) workRow.findViewById(R.id.res_work_name)).getText().toString();
            Row row = sheet.createRow(i + lastRow);
            cell = row.createCell(0);
            cell.setCellValue(name);
            cell.setCellStyle(commonStyle);
            for (int j = 1; j <= tasksCount * 2 - 1; j+=2) {
                androidx.gridlayout.widget.GridLayout taskElement = (androidx.gridlayout.widget.GridLayout) workRow.getChildAt(j - k);
                cell = row.createCell(j);
                cell.setCellValue(((TextView) taskElement.findViewById(R.id.res_answer)).getText().toString());
                cell.setCellStyle(commonStyle);
                cell = row.createCell(j + 1);
                int points = Integer.parseInt(((TextView) taskElement.findViewById(R.id.res_points)).getText().toString());
                cell.setCellValue(points);
                if (points == 0) cell.setCellStyle(redBackground);
                else cell.setCellStyle(greenBackground);
                k++;
            }
            androidx.gridlayout.widget.GridLayout end = (androidx.gridlayout.widget.GridLayout) workRow.getChildAt(tasksCount + 1);
            cell = row.createCell(tasksCount * 2 + 1);
            cell.setCellValue(((TextView) end.findViewById(R.id.points_sum)).getText().toString());
            cell.setCellStyle(commonStyle);
            cell = row.createCell(tasksCount * 2 + 2);
            cell.setCellValue(((TextView) end.findViewById(R.id.mark)).getText().toString());
            cell.setCellStyle(commonStyle);
        }

        try {
            updateColumnsWidth(sheet, tasksCount * 2 + 3);
        } catch (Exception e) {}

        try (OutputStream outputStream = this.getContentResolver().openOutputStream(fileUri)) {
            if (outputStream != null) {
                table.write(outputStream);
                outputStream.flush();
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        getContentResolver().update(fileUri, values, null, null);
        Toast.makeText(this, "Результаты проверки сохранены!", Toast.LENGTH_SHORT).show();
    }

    public void onExitFromCheckClick(View view) {
        startActivity(new Intent(CheckActivity.this, HomeActivity.class));
    }

    public void onCreateEtalonClick(View view) {
        etalonCreationLayout.setVisibility(VISIBLE);
        etalonSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tasksCount;
                try {
                    tasksCount = Integer.parseInt(tasksCountInput.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(CheckActivity.this, "Напишите количество заданий корректно!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = etalonNameInput.getText().toString().trim();
                if (name.isEmpty() || etalonIconBitmap == null) {
                    Toast.makeText(CheckActivity.this, "Заполните данные эталона корректно!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                etalonIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] iconBytes = stream.toByteArray();
                long etalonId = dbHelper.addEtalon(name, iconBytes, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()), tasksCount);
                if (etalonId != -1) {
                    for (int i = 1; i < answersTable.getChildCount(); i++) {
                        androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) answersTable.getChildAt(i);
                        TextView taskNumView = row.findViewById(R.id.task_num);
                        EditText rightAnswerInput = row.findViewById(R.id.right_answer_input);
                        EditText pointsInput = row.findViewById(R.id.points_input);
                        int taskNum = Integer.parseInt(taskNumView.getText().toString().trim());
                        String rightAnswer = rightAnswerInput.getText().toString().trim();
                        int points = Integer.parseInt(pointsInput.getText().toString().trim());
                        dbHelper.addAnswer((int) etalonId, taskNum, rightAnswer, points);
                    }
                    for (int i = 1; i < gradesTable.getChildCount(); i++) {
                        androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) gradesTable.getChildAt(i);
                        EditText minPointsInput = row.findViewById(R.id.min_points_input);
                        EditText maxPointsInput = row.findViewById(R.id.max_points_input);
                        TextView gradeView = row.findViewById(R.id.grade_view);
                        int minPoints = Integer.parseInt(minPointsInput.getText().toString().trim());
                        int maxPoints = Integer.parseInt(maxPointsInput.getText().toString().trim());
                        String grade = gradeView.getText().toString().trim();
                        dbHelper.addGrade((int) etalonId, minPoints, maxPoints, grade);
                    }
                    Toast.makeText(CheckActivity.this, "Эталон создан!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, checkWorksLayout.getBottom());
            }
        });
    }

    public void onCreateTemplateClick(View view) {
        templateCreationLayout.setVisibility(VISIBLE);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, templateCreationLayout.getBottom());
            }
        });
    }

    public void onEtalonsPageClick(View view) {
        etalonsListLayout.setVisibility(VISIBLE);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, etalonsListLayout.getBottom());
            }
        });
    }

    @SuppressLint("IntentReset")
    public void onUploadIconClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_ETALON_IMAGE_REQUEST);
    }
}