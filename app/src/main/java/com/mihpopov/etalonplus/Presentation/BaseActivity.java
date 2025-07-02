package com.mihpopov.etalonplus.Presentation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mihpopov.etalonplus.Common.DatabaseHelper;
import com.mihpopov.etalonplus.Data.Answer;
import com.mihpopov.etalonplus.Data.ComplexCriteria;
import com.mihpopov.etalonplus.Data.Etalon;
import com.mihpopov.etalonplus.Data.Grade;
import com.mihpopov.etalonplus.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Базовый класс для активности, содержащий общую логику для работы с эталонами, ответами и изображениями.
 * Предоставляет методы для инициализации адаптеров, работы с камерой, обработки изображений и валидации таблиц.
 */
public class BaseActivity extends AppCompatActivity {

    protected static final int PICK_WORKS_REQUEST = 1;
    protected static final int CAPTURE_WORK_REQUEST = 2;
    protected static final int PICK_PAGES_REQUEST = 3;
    protected static final int CAPTURE_PAGE_REQUEST = 4;
    protected static final int PICK_ETALON_IMAGE_REQUEST = 5;
    protected static final int PICK_CRITERIA_PAGES_REQUEST = 6;
    protected static final int CAPTURE_CRITERIA_PAGE_REQUEST = 7;
    protected static final int CAMERA_PERMISSION_CODE = 100;
    protected String[] ANSWER_TYPES = new String[]{"Краткий ответ", "Развёрнутый ответ"};
    protected String[] CHECK_METHODS = new String[]{"Полное совпадение", "Поэлементное совпадение"};
    protected enum CameraOperationType {WORK, PAGE, CRITERIA}
    protected CameraOperationType pendingCameraOperation;
    int currentWorkPosition = -1;
    int currentCriteriaTaskNum = -1;

    ImageView etalonIcon;

    HashMap<Integer, List<WorkAdapter.PageItem>> detailedTaskPagesMap = new HashMap<>();
    Bitmap etalonIconBitmap;
    Uri cameraImageUri;
    DatabaseHelper dbHelper;
    List<Etalon> etalonList;
    ArrayAdapter<String> checkMethodsAdapter;
    ArrayAdapter<String> answerTypesAdapter;

    // Инициализация адаптеров
    public void initAdapters() {
        checkMethodsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, CHECK_METHODS);
        checkMethodsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        answerTypesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, ANSWER_TYPES);
        answerTypesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
    }

    // Методы для работы с камерой и галереей
    protected void setPickImagesRequest(int code) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, code);
    }

    @SuppressLint("IntentReset")
    protected void setPickEtalonIconRequest() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_ETALON_IMAGE_REQUEST);
    }

    protected void startCameraForOperation(CameraOperationType operationType) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image for " + operationType.name());
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        int requestCode = CAPTURE_WORK_REQUEST;
        switch (operationType) {
            case WORK:
                requestCode = CAPTURE_WORK_REQUEST;
                break;
            case PAGE:
                requestCode = CAPTURE_PAGE_REQUEST;
                break;
            case CRITERIA:
                requestCode = CAPTURE_CRITERIA_PAGE_REQUEST;
                break;
        }
        startActivityForResult(intent, requestCode);
    }

    protected void openCamera() {
        CameraOperationType operationType = currentWorkPosition == -1 ? CameraOperationType.WORK : CameraOperationType.PAGE;
        openCameraForOperation(operationType);
    }

    protected void openCameraForOperation(CameraOperationType operationType) {
        pendingCameraOperation = operationType;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else startCameraForOperation(operationType);
    }

    protected void openCameraForCriteria() {
        openCameraForOperation(CameraOperationType.CRITERIA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCameraForOperation(pendingCameraOperation);
            else Toast.makeText(this, "Разрешение на использование камеры не предоставлено!", Toast.LENGTH_SHORT).show();
        }
    }

    //Декодировка страниц
    public List<WorkAdapter.PageItem> convertBytesToPageItems(Context context, List<byte[]> byteArrays) {
        List<WorkAdapter.PageItem> pageItems = new ArrayList<>();
        File tempDir = new File(context.getCacheDir(), "temp_images");
        if (!tempDir.exists()) tempDir.mkdirs();
        for (int i = 0; i < byteArrays.size(); i++) {
            byte[] imageData = byteArrays.get(i);
            if (imageData != null && imageData.length > 0) {
                try {
                    String fileName = "page_" + i + "_" + System.currentTimeMillis() + ".jpg";
                    File tempFile = new File(tempDir, fileName);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(imageData);
                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", tempFile);
                    pageItems.add(new WorkAdapter.PageItem(fileUri, tempFile.length()));
                } catch (IOException e) {}
            }
        }
        return pageItems;
    }

    //Инициализация выпадающего списка с типами ответа на задание
    @SuppressLint("SetTextI18n")
    protected void setupAnswerTypesDropdown(Spinner answerTypesDropdown, View row, int finalI, Context context,
                                            androidx.gridlayout.widget.GridLayout detailedTable, List<byte[]> criteria, Answer answer) {
        TextView detailedAnswerText = row.findViewById(R.id.detailed_answer_text);
        LinearLayout shortAnswerColumns = row.findViewById(R.id.short_answer_columns);

        answerTypesDropdown.setAdapter(answerTypesAdapter);

        if (answer != null) {
            Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
            String answerType = answer.getAnswerType();
            int selection = answerType.equals("Краткий ответ") ? 0 : 1;
            answerTypesDropdown.setSelection(selection);
            if (answerType.equals("Краткий ответ")) {
                ((EditText) row.findViewById(R.id.right_answer_input)).setText(answer.getRightAnswer());
                ((EditText) row.findViewById(R.id.points_input)).setText(answer.getPoints() + "");
                ((MaterialCheckBox) row.findViewById(R.id.order_matters_checkbox)).setChecked(answer.getOrderMatters() == 1);

                selection = answer.getCheckMethod().equals("Полное совпадение") ? 0 : 1;
                checkMethodsDropdown.setSelection(selection);
            }
            else {
                shortAnswerColumns.setVisibility(GONE);
                detailedAnswerText.setVisibility(VISIBLE);
            }
        }

        answerTypesDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    shortAnswerColumns.setVisibility(VISIBLE);
                    detailedAnswerText.setVisibility(GONE);
                    for (int j = 1; j < detailedTable.getChildCount(); j++) {
                        View task = detailedTable.getChildAt(j);
                        try {
                            if (((TextView) task.findViewById(R.id.detailed_task_num)).getText().toString().equals(finalI + "")) {
                                detailedTaskPagesMap.remove(finalI);
                                detailedTable.removeView(task);
                                if (detailedTable.getChildCount() == 1)
                                    detailedTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_merged_row, null));
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                }
                else {
                    shortAnswerColumns.setVisibility(GONE);
                    detailedAnswerText.setVisibility(VISIBLE);

                    if (detailedTable.getChildCount() == 2 && detailedTaskPagesMap.isEmpty()) detailedTable.removeViewAt(1);

                    View detailedTask = getLayoutInflater().inflate(R.layout.table_detailed_answers_row, null);
                    ((TextView) detailedTask.findViewById(R.id.detailed_task_num)).setText(finalI + "");

                    List<WorkAdapter.PageItem> criteriaPages;
                    if (criteria == null) criteriaPages = new ArrayList<>();
                    else criteriaPages = convertBytesToPageItems(context, criteria);
                    detailedTaskPagesMap.put(finalI, criteriaPages);

                    RecyclerView criteriaPagesRecycler = detailedTask.findViewById(R.id.criteria_pages_recycler);
                    PageAdapter criteriaPagesAdapter = new PageAdapter(criteriaPages, new PageAdapter.OnPageDeleteListener() {
                        @Override
                        public void onPageDeleted(int pagePosition, boolean isNowEmpty) {
                            detailedTaskPagesMap.put(finalI, criteriaPages);
                            criteriaPagesRecycler.post(() -> {
                                ViewGroup.LayoutParams params = criteriaPagesRecycler.getLayoutParams();
                                params.height -= 264;
                                criteriaPagesRecycler.setLayoutParams(params);
                            });
                        }
                    });
                    criteriaPagesRecycler.setLayoutManager(new LinearLayoutManager(context));
                    criteriaPagesRecycler.setAdapter(criteriaPagesAdapter);

                    MaterialCardView criteriaAddFromGallery = detailedTask.findViewById(R.id.criteria_page_upload_card);
                    MaterialCardView criteriaAddFromCamera = detailedTask.findViewById(R.id.criteria_page_camera_card);

                    criteriaAddFromGallery.setOnClickListener(v -> {
                        currentCriteriaTaskNum = finalI;
                        setPickImagesRequest(PICK_CRITERIA_PAGES_REQUEST);
                    });

                    criteriaAddFromCamera.setOnClickListener(v -> {
                        currentCriteriaTaskNum = finalI;
                        openCameraForCriteria();
                    });

                    detailedTable.addView(detailedTask);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    //Инициализация выпадающего списка с выбором метода проверки
    protected void setupCheckMethodsDropdown(Spinner checkMethodsDropdown, View row, Answer answer) {
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

        if (answer == null) {
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
                    } else {
                        complexGradingTable.setVisibility(GONE);
                        editComplexGradingTableCard.setVisibility(GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
        else {
            checkMethodsDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint({"InflateParams", "SetTextI18n"})
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
                            ((EditText) row2.findViewById(R.id.complex_points_input)).setText(complexCriteria.getPoints() + "");
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
    }

    //Инициализация слушателя изменений поля ввода
    protected void setupTextChangedListener(TextInputEditText tasksCountInput, Context context, androidx.gridlayout.widget.GridLayout answersTable,
                                            androidx.gridlayout.widget.GridLayout detailedTable) {
        tasksCountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int newTasksCount = Integer.parseInt(s.toString().trim());
                    int oldTasksCount = answersTable.getChildCount() - 1;
                    int d = newTasksCount - oldTasksCount;
                    if (d > 0) {
                        while (d != 0) {
                            final int taskNum = answersTable.getChildCount();
                            View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
                            ((TextView) row.findViewById(R.id.task_num)).setText(taskNum + "");
                            Spinner answerTypesDropdown = row.findViewById(R.id.answer_type_dropdown);
                            Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
                            setupCheckMethodsDropdown(checkMethodsDropdown, row, null);
                            setupAnswerTypesDropdown(answerTypesDropdown, row, taskNum, context, detailedTable, null, null);

                            answersTable.addView(row);
                            d--;
                        }
                    }
                    else {
                        while (d != 0 && answersTable.getChildCount() > 2) {
                            int taskNum = answersTable.getChildCount() - 1;
                            for (int j = 1; j < detailedTable.getChildCount(); j++) {
                                View task = detailedTable.getChildAt(j);
                                try {
                                    if (((TextView) task.findViewById(R.id.detailed_task_num)).getText().toString().equals(taskNum + "")) {
                                        detailedTaskPagesMap.remove(taskNum);
                                        detailedTable.removeView(task);
                                        if (detailedTable.getChildCount() == 1)
                                            detailedTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_merged_row, null));
                                    }
                                } catch (Exception e) {
                                    break;
                                }
                            }
                            answersTable.removeViewAt(answersTable.getChildCount() - 1);
                            d++;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });
    }

    //Методы для работы с адаптерами
    public void handleEtalonImageSelection(@Nullable Intent data) {
        if (data == null || data.getData() == null) return;
        Uri imageUri = data.getData();
        try {
            etalonIconBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            etalonIcon.setImageBitmap(etalonIconBitmap);
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleCriteriaImageSelection(@Nullable Intent data, androidx.gridlayout.widget.GridLayout detailedTable) {
        if (data == null) return;
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                addPageToCriteria(imageUri, detailedTable);
            }
        } else if (data.getData() != null) addPageToCriteria(data.getData(), detailedTable);
        currentCriteriaTaskNum = -1;
    }

    public long getFileSize(Uri uri) {
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

    public void addPageToCriteria(Uri imageUri, androidx.gridlayout.widget.GridLayout detailedTable) {
        if (currentCriteriaTaskNum == -1) return;
        long fileSize = getFileSize(imageUri);
        List<WorkAdapter.PageItem> pages = detailedTaskPagesMap.get(currentCriteriaTaskNum);
        if (pages != null) {
            pages.add(new WorkAdapter.PageItem(imageUri, fileSize));
            updateCriteriaAdapter(currentCriteriaTaskNum, detailedTable);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateCriteriaAdapter(int taskNum, androidx.gridlayout.widget.GridLayout detailedTable) {
        for (int i = 1; i < detailedTable.getChildCount(); i++) {
            View child = detailedTable.getChildAt(i);
            TextView taskNumView = child.findViewById(R.id.detailed_task_num);
            if (taskNumView != null && taskNumView.getText().toString().equals(String.valueOf(taskNum))) {
                RecyclerView criteriaRecycler = child.findViewById(R.id.criteria_pages_recycler);
                if (criteriaRecycler != null && criteriaRecycler.getAdapter() != null) {
                    criteriaRecycler.getAdapter().notifyDataSetChanged();
                    criteriaRecycler.post(() -> {
                        ViewGroup.LayoutParams params = criteriaRecycler.getLayoutParams();
                        params.height = 264 * criteriaRecycler.getAdapter().getItemCount();
                        criteriaRecycler.setLayoutParams(params);
                    });
                }
                break;
            }
        }
    }

    // Валидация таблиц с ответами и оценками
    public boolean isTablesCorrect(androidx.gridlayout.widget.GridLayout answersTable, androidx.gridlayout.widget.GridLayout gradesTable) {
        try {
            if (gradesTable.getChildAt(1) == null) return false;
            List<Integer> detailedTasks = new ArrayList<>(detailedTaskPagesMap.keySet());
            for (int i = 1; i < answersTable.getChildCount(); i++) {
                if (detailedTasks.contains(i)) continue;
                View row = answersTable.getChildAt(i);
                EditText answer = row.findViewById(R.id.right_answer_input);
                EditText points = row.findViewById(R.id.points_input);
                Spinner checkMethodDropdown = row.findViewById(R.id.check_method_dropdown);
                if (answer.getText().toString().isEmpty()) return false;
                Double.parseDouble(points.getText().toString().trim());
                if (checkMethodDropdown.getSelectedItem().equals("Поэлементное совпадение")) {
                    androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                    for (int j = 1; j < complexGradingTable.getChildCount() - 1; j++) {
                        View row2 = complexGradingTable.getChildAt(j);
                        EditText minMistakesInput = row2.findViewById(R.id.min_mistakes_input);
                        EditText maxMistakesInput = row2.findViewById(R.id.max_mistakes_input);
                        EditText complexPointsInput = row2.findViewById(R.id.complex_points_input);
                        Integer.parseInt(minMistakesInput.getText().toString().trim());
                        Integer.parseInt(maxMistakesInput.getText().toString().trim());
                        Double.parseDouble(complexPointsInput.getText().toString().trim());
                    }
                }
            }
            for (int i = 0; i < detailedTasks.size(); i++) {
                List<WorkAdapter.PageItem> data = detailedTaskPagesMap.get(detailedTasks.get(i));
                if (data.isEmpty()) return false;
            }
            for (int i = 1; i < gradesTable.getChildCount(); i++) {
                View row = gradesTable.getChildAt(i);
                EditText minPoints = row.findViewById(R.id.min_points_input);
                EditText maxPoints = row.findViewById(R.id.max_points_input);
                Double.parseDouble(minPoints.getText().toString().trim());
                Double.parseDouble(maxPoints.getText().toString().trim());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("SetTextI18n")
    protected void loadGrades(List<Grade> grades, androidx.gridlayout.widget.GridLayout gradesTable) {
        for (Grade grade : grades) {
            View row = getLayoutInflater().inflate(R.layout.table_grades_row, null);
            ((EditText) row.findViewById(R.id.min_points_input)).setText(grade.getMinPoints() + "");
            ((EditText) row.findViewById(R.id.max_points_input)).setText(grade.getMaxPoints() + "");
            ((TextView) row.findViewById(R.id.grade_view)).setText(grade.getGrade());
            gradesTable.addView(row);
        }
    }

    protected void loadGradesSystem(androidx.gridlayout.widget.GridLayout gradesTable) {
        List<String> gradesSystem = dbHelper.getGradesSystem();
        for (String grade : gradesSystem) {
            View row = getLayoutInflater().inflate(R.layout.table_grades_row, null);
            ((TextView) row.findViewById(R.id.grade_view)).setText(grade);
            gradesTable.addView(row);
        }
    }

    // Сохранение ответов и критериев
    protected void saveAnswersAndCriteria(long etalonId, androidx.gridlayout.widget.GridLayout answersTable, Context context) {
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            View row = answersTable.getChildAt(i);
            TextView taskNumView = row.findViewById(R.id.task_num);
            Spinner answerTypeDropdown = row.findViewById(R.id.answer_type_dropdown);
            EditText rightAnswerInput = row.findViewById(R.id.right_answer_input);
            EditText pointsInput = row.findViewById(R.id.points_input);
            MaterialCheckBox checkBox = row.findViewById(R.id.order_matters_checkbox);
            Spinner checkMethodDropdown = row.findViewById(R.id.check_method_dropdown);
            int taskNum = Integer.parseInt(taskNumView.getText().toString().trim());
            String answerType = (String) answerTypeDropdown.getSelectedItem();

            if (answerType.equals("Краткий ответ")) {
                String rightAnswer = rightAnswerInput.getText().toString().trim();
                double points = Double.parseDouble(pointsInput.getText().toString().trim());
                int orderMatters = checkBox.isChecked() ? 1 : 0;
                String checkMethod = (String) checkMethodDropdown.getSelectedItem();
                long answerId = dbHelper.addAnswer((int) etalonId, taskNum, answerType, rightAnswer, points, orderMatters, checkMethod);
                if (answerId != -1 && checkMethod.equals("Поэлементное совпадение")) {
                    androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                    for (int j = 1; j < complexGradingTable.getChildCount() - 1; j++) {
                        View row2 = complexGradingTable.getChildAt(j);
                        EditText minMistakesInput = row2.findViewById(R.id.min_mistakes_input);
                        EditText maxMistakesInput = row2.findViewById(R.id.max_mistakes_input);
                        EditText complexPointsInput = row2.findViewById(R.id.complex_points_input);
                        int minMistakes = Integer.parseInt(minMistakesInput.getText().toString().trim());
                        int maxMistakes = Integer.parseInt(maxMistakesInput.getText().toString().trim());
                        double complexPoints = Double.parseDouble(complexPointsInput.getText().toString().trim());
                        dbHelper.addComplexCriteria((int) answerId, minMistakes, maxMistakes, complexPoints);
                    }
                }
            }
            else {
                long answerId = dbHelper.addAnswer((int) etalonId, taskNum, answerType);
                if (answerId != -1) {
                    List<WorkAdapter.PageItem> pages = detailedTaskPagesMap.get(taskNum);
                    for (WorkAdapter.PageItem page : pages) {
                        Uri imageUri = page.getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] criteria = stream.toByteArray();
                            dbHelper.addCriteria((int) answerId, criteria);
                        } catch (IOException ignored) {}
                    }
                }
            }
        }
    }

    protected void saveGrades(long etalonId, androidx.gridlayout.widget.GridLayout gradesTable) {
        for (int i = 1; i < gradesTable.getChildCount(); i++) {
            View row = gradesTable.getChildAt(i);
            EditText minPointsInput = row.findViewById(R.id.min_points_input);
            EditText maxPointsInput = row.findViewById(R.id.max_points_input);
            TextView gradeView = row.findViewById(R.id.grade_view);
            double minPoints = Double.parseDouble(minPointsInput.getText().toString().trim());
            double maxPoints = Double.parseDouble(maxPointsInput.getText().toString().trim());
            String grade = gradeView.getText().toString().trim();
            dbHelper.addGrade((int) etalonId, minPoints, maxPoints, grade);
        }
    }

    // Создание нового эталона
    protected void createEtalon(Context context, TextInputEditText tasksCountInput, TextInputEditText etalonNameInput,
                                        androidx.gridlayout.widget.GridLayout answersTable, androidx.gridlayout.widget.GridLayout gradesTable) {
        if (!isTablesCorrect(answersTable, gradesTable)) {
            Toast.makeText(this, "Проверьте корректность заполнения таблиц!", Toast.LENGTH_SHORT).show();
            return;
        }
        int tasksCount;
        try {
            tasksCount = Integer.parseInt(tasksCountInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Напишите количество заданий корректно!", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = etalonNameInput.getText().toString().trim();
        if (name.isEmpty() || etalonIconBitmap == null) {
            Toast.makeText(context, "Заполните данные эталона корректно!", Toast.LENGTH_SHORT).show();
            return;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        etalonIconBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] iconBytes = stream.toByteArray();
        long etalonId = dbHelper.addEtalon(name, iconBytes, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()), tasksCount);
        if (etalonId != -1) {
            saveAnswersAndCriteria(etalonId, answersTable, context);
            saveGrades(etalonId, gradesTable);
            Toast.makeText(context, "Эталон создан!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File tempDir = new File(this.getCacheDir(), "temp_images");
        if (tempDir.exists() && tempDir.isDirectory()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }
}