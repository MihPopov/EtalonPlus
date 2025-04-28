package com.example.bigchallengesproject.Presentation;

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
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
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
import com.example.bigchallengesproject.Data.ComplexCriteria;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.Data.Grade;
import com.example.bigchallengesproject.R;
import com.google.android.material.checkbox.MaterialCheckBox;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int PICK_ETALON_IMAGE_REQUEST = 3;
    private static final int CAMERA_PERMISSION_CODE = 100;
    int k = 0;
    String[] checkMethods = new String[]{"Полное совпадение", "Поэлементное совпадение"};

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
    HashMap<Integer, Pair<String, Double>> answersPointsMap = new HashMap<>();

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

        ArrayAdapter<String> checkMethodsAdapter = new ArrayAdapter<>(CheckActivity.this, R.layout.spinner_item, checkMethods);
        checkMethodsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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
                    Answer answer = answers.get(i - 1);
                    String points = answer.getPoints();
                    ((EditText) row.findViewById(R.id.right_answer_input)).setText(answer.getRightAnswer());
                    ((EditText) row.findViewById(R.id.points_input)).setText(points);
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
                }
                cardGone = true;
                grades = null;
            }
        });
        etalonsRecyclerView.setAdapter(etalonsUseAdapter);

        etalonIconBitmap = ((BitmapDrawable) getDrawable(R.drawable.etalon_plus)).getBitmap();

        trocr = new TrOCR();

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
        ArrayAdapter<String> checkMethodsAdapter = new ArrayAdapter<>(CheckActivity.this, R.layout.spinner_item, checkMethods);
        checkMethodsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        answersTable.removeAllViews();
        answersTable.addView(getLayoutInflater().inflate(R.layout.table_answers_header, null));
        for (int i = 1; i <= tasksCount; i++) {
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_answers_row, null);
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
                ((EditText) row.findViewById(R.id.min_points_input)).setText(grade.getMinPoints());
                ((EditText) row.findViewById(R.id.max_points_input)).setText(grade.getMaxPoints());
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
        String symbolsToIgnore = settings.getString("symbolsToIgnore", "");
        for (String block : recognizedText) {
            if (!block.isEmpty()) {
                int spaceIndex = block.indexOf(" ");
                if (spaceIndex != -1) {
                    try {
                        int taskNumber = Integer.parseInt(block.substring(0, spaceIndex).trim());
                        String answer = block.substring(spaceIndex + 1).trim();
                        StringBuilder cleanedAnswer = new StringBuilder();
                        for (char c : answer.toCharArray()) {
                            if (symbolsToIgnore.indexOf(c) == -1) cleanedAnswer.append(c);
                        }
                        answersMap.put(taskNumber, cleanedAnswer.toString());
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
        recTextTable.addView(getLayoutInflater().inflate(R.layout.table_recognized_text_header, null));
        recognizedTextTablesLayout.addView(recTextTable);
        int tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        boolean isIIEnabled = settings.getBoolean("isIIEnabled", true);
        k = 0;
        for (int i = 0; i < workUris.size(); i++) {
            int finalI = i;
            if (!isIIEnabled) {
                createRecTable(tasksCount, finalI, null);
                continue;
            }
            Uri imageUri = workUris.get(i);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                trocr.recognizeTextBlocks(bitmap, new SimpleCallback<List<String>>() {
                    @Override
                    public void onLoad(List<String> data) {
                        HashMap<Integer, String> answersMap = null;
                        if (data != null && !data.isEmpty()) answersMap = parseRecognizedText(data);
                        createRecTable(tasksCount, finalI, answersMap);
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

    @SuppressLint("SetTextI18n")
    public void createRecTable(int tasksCount, int i, @Nullable HashMap<Integer, String> answersMap) {
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
        ((TextView) mergedRow.findViewById(R.id.work_name)).setText("Работа №" + (i + 1));
        recTextTable.addView(mergedRow);
        for (int j = 1; j <= tasksCount; j++) {
            GridLayout row = (GridLayout) getLayoutInflater().inflate(R.layout.table_recognized_text_row, null);
            ((TextView) row.findViewById(R.id.rec_task_num)).setText(j + "");
            String answer = (answersMap != null) ? answersMap.get(j).replace("null", "") : "";
            ((EditText) row.findViewById(R.id.rec_answer_input)).setText(answer);
            recTextTable.addView(row);
        }
        k++;
    }

    public HashMap<Integer, Map<String, Object>> detectCheaters(Map<String, Map<Integer, String>> studentAnswers,
            HashMap<Integer, Pair<String, Double>> answersMap) {

        class GroupData {
            List<Integer> tasks;
            Map<Integer, String> wrongsKey; // task -> wrongAnswer
            Set<String> works = new HashSet<>();
            double score;

            GroupData(Map<Integer, String> wrongs) {
                this.wrongsKey = new HashMap<>(wrongs);
                this.tasks = new ArrayList<>(wrongs.keySet());
                this.tasks.sort(Integer::compareTo);
                this.score = tasks.stream().mapToDouble(t -> answersMap.get(t).second).sum();
            }

            String key() {
                return wrongsKey.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";"));
            }
        }

        Map<String, Map<Integer, String>> wrongAnswersMap = new HashMap<>();
        for (Map.Entry<String, Map<Integer, String>> entry : studentAnswers.entrySet()) {
            String work = entry.getKey();
            Map<Integer, String> answers = entry.getValue();
            Map<Integer, String> wrongs = new HashMap<>();
            for (Map.Entry<Integer, String> ans : answers.entrySet()) {
                int task = ans.getKey();
                String given = ans.getValue();
                Pair<String, Double> correct = answersMap.get(task);
                if (correct != null && !given.equals(correct.first)) {
                    wrongs.put(task, given);
                }
            }
            wrongAnswersMap.put(work, wrongs);
        }
        Map<String, GroupData> groupMap = new HashMap<>();
        List<String> allWorks = new ArrayList<>(wrongAnswersMap.keySet());
        for (int i = 0; i < allWorks.size(); i++) {
            String w1 = allWorks.get(i);
            Map<Integer, String> wa1 = wrongAnswersMap.get(w1);
            for (int j = i + 1; j < allWorks.size(); j++) {
                String w2 = allWorks.get(j);
                Map<Integer, String> wa2 = wrongAnswersMap.get(w2);
                Map<Integer, String> intersection = new HashMap<>();
                for (Map.Entry<Integer, String> entry : wa1.entrySet()) {
                    int task = entry.getKey();
                    String ans = entry.getValue();
                    if (wa2.containsKey(task) && wa2.get(task).equals(ans)) {
                        intersection.put(task, ans);
                    }
                }
                if (!intersection.isEmpty()) {
                    GroupData group = new GroupData(intersection);
                    String key = group.key();
                    groupMap.computeIfAbsent(key, k -> group).works.addAll(List.of(w1, w2));
                }
            }
        }

        Map<String, GroupData> filteredGroups = groupMap.entrySet().stream()
                .filter(e -> e.getValue().works.size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, List<GroupData>> workToGroups = new HashMap<>();
        for (GroupData group : filteredGroups.values()) {
            for (String w : group.works) {
                workToGroups.computeIfAbsent(w, k -> new ArrayList<>()).add(group);
            }
        }

        Set<GroupData> finalGroups = new HashSet<>();
        for (Map.Entry<String, List<GroupData>> entry : workToGroups.entrySet()) {
            List<GroupData> groups = entry.getValue();
            int maxTasksSize = 0;
            int maxGroupSize = 0;
            double maxScore = 0;
            for (GroupData g : groups) {
                if (g.tasks.size() > maxTasksSize) {
                    maxTasksSize = g.tasks.size();
                    maxGroupSize = g.works.size();
                    maxScore = g.score;
                } else if (g.tasks.size() == maxTasksSize) {
                    if (g.works.size() > maxGroupSize) {
                        maxGroupSize = g.works.size();
                        maxScore = g.score;
                    } else if (g.works.size() == maxGroupSize) {
                        if (g.score > maxScore) {
                            maxScore = g.score;
                        }
                    }
                }
            }
            for (GroupData g : groups) {
                if (g.tasks.size() == maxTasksSize &&
                        g.works.size() == maxGroupSize &&
                        g.score == maxScore) {
                    finalGroups.add(g);
                }
            }
        }
        HashMap<Integer, Map<String, Object>> result = new HashMap<>();
        int groupId = 1;
        double maxTotalWeight = answersMap.values().stream()
                .mapToDouble(p -> p.second)
                .sum();
        double averageTaskWeight = maxTotalWeight / answersMap.size();
        for (GroupData group : finalGroups) {
            Map<String, Object> groupInfo = new HashMap<>();
            List<String> works = new ArrayList<>(group.works);
            works.sort(Comparator.naturalOrder());
            groupInfo.put("works", works);
            groupInfo.put("size", works.size());
            groupInfo.put("tasks", group.tasks);
            String probability = getProbability(works.size(), group.score, averageTaskWeight);
            groupInfo.put("probability", probability);

            result.put(groupId++, groupInfo);
        }
        return result;
    }

    public String getProbability(int groupSize, double totalWeight, double averageTaskWeight) {
        String probability;
        if (groupSize >= 5) {
            probability = "Высокая";
        } else if (groupSize == 4) {
            if (totalWeight >= averageTaskWeight * 3) probability = "Высокая";
            else probability = "Средняя";
        } else {
            if (totalWeight >= averageTaskWeight * 5) probability = "Высокая";
            else if (totalWeight >= averageTaskWeight * 3) probability = "Средняя";
            else probability = "Низкая";
        }
        return probability;
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

        HashMap<Integer, List<Object>> answersMapForCheck = new HashMap<>();
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            View row = answersTable.getChildAt(i);
            EditText answerInput = row.findViewById(R.id.right_answer_input);
            EditText pointsInput = row.findViewById(R.id.points_input);
            MaterialCheckBox checkBox = row.findViewById(R.id.order_matters_checkbox);
            Spinner checkMethodDropdown = row.findViewById(R.id.check_method_dropdown);
            String answer = answerInput.getText().toString().trim();
            String points = pointsInput.getText().toString().trim();
            int orderMatters = checkBox.isChecked() ? 1 : 0;
            String checkMethod = (String) checkMethodDropdown.getSelectedItem();
            List<ComplexCriteria> gradingList = new ArrayList<>();
            if (checkMethod.equals("Поэлементное совпадение")) {
                androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                for (int j = 1; j < complexGradingTable.getChildCount() - 1; j++) {
                    View row2 = complexGradingTable.getChildAt(j);
                    EditText minMistakesInput = row2.findViewById(R.id.min_mistakes_input);
                    EditText maxMistakesInput = row2.findViewById(R.id.max_mistakes_input);
                    EditText complexPointsInput = row2.findViewById(R.id.complex_points_input);
                    int minMistakes = Integer.parseInt(minMistakesInput.getText().toString().trim());
                    int maxMistakes = Integer.parseInt(maxMistakesInput.getText().toString().trim());
                    String complexPoints = complexPointsInput.getText().toString().trim();
                    gradingList.add(new ComplexCriteria(0, 0, minMistakes, maxMistakes, complexPoints));
                }
            }

            answersMapForCheck.put(i, List.of(new Answer(0, 0, 0, answer, points, orderMatters, checkMethod), gradingList));
            answersPointsMap.put(i, new Pair<>(answer, Double.parseDouble(points)));
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
            double pointsSum = 0.0;

            int tableIndex = (i - 1) / 3;
            int workOffset = (i - 1) % 3;
            androidx.gridlayout.widget.GridLayout workTable = (androidx.gridlayout.widget.GridLayout) recognizedTextTablesLayout.getChildAt(tableIndex);
            int workRowIndex = 1 + workOffset * (tasksCount + 1);
            for (int t = 1; t <= tasksCount; t++) {
                EditText answerInput = workTable.getChildAt(workRowIndex + t).findViewById(R.id.rec_answer_input);
                String recognizedAnswer = answerInput.getText().toString().trim();
                String recAnswerForCheating = answerInput.getText().toString().trim();
                androidx.gridlayout.widget.GridLayout taskElement = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_row_task, null);
                Answer answer = (Answer) answersMapForCheck.get(t).get(0);
                String rightAnswer = answer.getRightAnswer();
                boolean orderMatters = answer.getOrderMatters() == 1;
                double points = 0.0;
                if (answer.getCheckMethod().equals("Полное совпадение")) {
                    if (!orderMatters) {
                        recognizedAnswer = sortAnswer(recognizedAnswer);
                        rightAnswer = sortAnswer(rightAnswer);
                    }
                    if (recognizedAnswer.equals(rightAnswer)) points = Double.parseDouble(answer.getPoints());
                }
                else {
                    int mistakes = getMistakes(orderMatters, recognizedAnswer, rightAnswer);
                    List<ComplexCriteria> gradingList = (List<ComplexCriteria>) answersMapForCheck.get(t).get(1);
                    for (ComplexCriteria complexCriteria : gradingList) {
                        if (complexCriteria.getMinMistakes() <= mistakes && complexCriteria.getMaxMistakes() >= mistakes)
                            points = Double.parseDouble(complexCriteria.getPoints());
                    }
                }
                ((TextView) taskElement.findViewById(R.id.res_answer)).setText(recAnswerForCheating);
                ((TextView) taskElement.findViewById(R.id.res_points)).setText(points + "");
                pointsSum += points;
                row.addView(taskElement);
                answers.put(t, recAnswerForCheating);
            }
            studentAnswers.put("Работа №" + i, answers);

            androidx.gridlayout.widget.GridLayout rowEnd = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_row_end, null);
            ((TextView) rowEnd.findViewById(R.id.points_sum)).setText(pointsSum + "");
            for (int j = 1; j <= gradesTable.getChildCount(); j++) {
                androidx.gridlayout.widget.GridLayout gradeRow = (androidx.gridlayout.widget.GridLayout) gradesTable.getChildAt(j);
                double min = Double.parseDouble(((EditText) gradeRow.findViewById(R.id.min_points_input)).getText().toString());
                double max = Double.parseDouble(((EditText) gradeRow.findViewById(R.id.max_points_input)).getText().toString());
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
        HashMap<Integer, Map<String, Object>> cheatersGroups = detectCheaters(studentAnswers, answersPointsMap);
        if (cheatersGroups.isEmpty()) cheatersTable.addView(getLayoutInflater().inflate(R.layout.table_cheaters_merged_row, null));
        else {
            for (int i = 1; i <= cheatersGroups.size(); i++) {
                Map<String, Object> group = cheatersGroups.get(i);
                androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_cheaters_row, null);
                ((TextView) row.findViewById(R.id.group_num)).setText(i + "");
                List<String> works = (List<String>) group.get("works");
                List<String> tasks = ((List<Integer>) group.get("tasks")).stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                ((TextView) row.findViewById(R.id.works)).setText(String.join(", ", works));
                ((TextView) row.findViewById(R.id.group_size)).setText(group.get("size") + "");
                ((TextView) row.findViewById(R.id.tasks_list)).setText(String.join(", ", tasks));
                ((TextView) row.findViewById(R.id.probability)).setText(group.get("probability") + "");
                cheatersTable.addView(row);
            }
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, checkWorksLayout.getBottom());
            }
        });
    }

    public int getMistakes(boolean orderMatters, String recognizedAnswer, String rightAnswer) {
        int mistakes = 0;
        if (!orderMatters) {
            Set<Character> set1 = new HashSet<>();
            for (char c : recognizedAnswer.toCharArray()) {
                set1.add(c);
            }
            Set<Character> set2 = new HashSet<>();
            for (char c : rightAnswer.toCharArray()) {
                set2.add(c);
            }
            Set<Character> res = new HashSet<>(set1);
            res.addAll(set2);
            Set<Character> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);
            res.removeAll(intersection);
            mistakes = res.size();
        }
        else {
            int minLength = Math.min(recognizedAnswer.length(), rightAnswer.length());
            int maxLength = Math.max(recognizedAnswer.length(), rightAnswer.length());
            for (int j = 0; j < minLength; j++) {
                if (recognizedAnswer.charAt(j) != rightAnswer.charAt(j)) {
                    mistakes++;
                }
            }
            mistakes += (maxLength - minLength);
        }
        return mistakes;
    }

    public boolean isTablesCorrect() {
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            View row = answersTable.getChildAt(i);
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
        for (int i = 1; i < gradesTable.getChildCount(); i++) {
            View row = gradesTable.getChildAt(i);
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

    public String sortAnswer(String answer) {
        StringBuilder letters = new StringBuilder();
        StringBuilder digits = new StringBuilder();

        for (char c : answer.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else if (Character.isLetter(c)) letters.append(c);
        }

        char[] digitArray = digits.toString().toCharArray();
        Arrays.sort(digitArray);
        char[] letterArray = letters.toString().toCharArray();
        Arrays.sort(letterArray);

        return new String(digitArray) + new String(letterArray);
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

        CellStyle yellowBackground = table.createCellStyle();
        yellowBackground.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellowBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        yellowBackground.setBorderTop(BorderStyle.MEDIUM);
        yellowBackground.setBorderBottom(BorderStyle.MEDIUM);
        yellowBackground.setBorderLeft(BorderStyle.MEDIUM);
        yellowBackground.setBorderRight(BorderStyle.MEDIUM);
        yellowBackground.setAlignment(HorizontalAlignment.CENTER);

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
                double points = Double.parseDouble(((TextView) taskElement.findViewById(R.id.res_points)).getText().toString());
                double maxPoints = answersPointsMap.get(j - k).second;
                cell.setCellValue(points);
                if (points == 0) cell.setCellStyle(redBackground);
                else if (points < maxPoints) cell.setCellStyle(yellowBackground);
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
                    for (int i = 1; i < gradesTable.getChildCount(); i++) {
                        androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) gradesTable.getChildAt(i);
                        EditText minPointsInput = row.findViewById(R.id.min_points_input);
                        EditText maxPointsInput = row.findViewById(R.id.max_points_input);
                        TextView gradeView = row.findViewById(R.id.grade_view);
                        String minPoints = minPointsInput.getText().toString().trim();
                        String maxPoints = maxPointsInput.getText().toString().trim();
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