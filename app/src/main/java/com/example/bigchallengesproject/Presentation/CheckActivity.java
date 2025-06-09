package com.example.bigchallengesproject.Presentation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bigchallengesproject.Common.DatabaseHelper;
import com.example.bigchallengesproject.Common.SimpleCallback;
import com.example.bigchallengesproject.Common.AIService;
import com.example.bigchallengesproject.Data.Answer;
import com.example.bigchallengesproject.Data.ComplexCriteria;
import com.example.bigchallengesproject.Data.Etalon;
import com.example.bigchallengesproject.Data.Grade;
import com.example.bigchallengesproject.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CheckActivity extends BaseActivity {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+([.,]\\d+)?$");
    private static final Pattern RU_LETTERS = Pattern.compile("^[а-яА-ЯёЁ]+$");
    private static final Pattern EN_LETTERS = Pattern.compile("^[a-zA-Z]+$");
    int k = 0;
    boolean cardGone = false;

    CardView uploadCard, cameraCard, tablesPageCard, templateSaveCard, etalonSaveCard;
    RecyclerView worksRecyclerView, etalonsRecyclerView;
    WorkAdapter workAdapter;
    EtalonsUseAdapter etalonsUseAdapter;
    ScrollView scrollView;
    LinearLayout tablesPage, recognizedTextPage, recognizedTextTablesLayout, checkWorksLayout, resultsDetailedLayout,
            resultsDetailedTablesLayout, etalonCreationLayout, templateCreationLayout, etalonsListLayout;
    androidx.gridlayout.widget.GridLayout answersTable, detailedAnswersTable, gradesTable, recTextTable, resultsTable, resultsDetailedTable,
            gradesDistributionTable, tasksCompletingTable, cheatersTable;
    PieChart gradesDistributionChart;
    LineChart tasksCompletingChart;
    TextInputEditText tasksCountInput, etalonNameInput;
    AppBarLayout waitAppBarLayout;
    TextView remainingTime, waitingText;

    List<WorkAdapter.WorkItem> works = new ArrayList<>();
    SharedPreferences settings;
    List<Grade> grades;
    HashMap<Integer, Pair<String, Double>> answerPointsMap = new HashMap<>();
    CountDownTimer timer;
    List<Pair<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>>> allDetailedResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        waitAppBarLayout = findViewById(R.id.wait_app_bar_layout);
        waitingText = findViewById(R.id.waiting_text);
        remainingTime = findViewById(R.id.remaining_time);
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
        detailedAnswersTable = findViewById(R.id.detailed_answers_table);
        gradesTable = findViewById(R.id.grades_table);
        tasksCountInput = findViewById(R.id.tasks_count_input);
        recognizedTextPage = findViewById(R.id.recognized_text_page);
        recognizedTextTablesLayout = findViewById(R.id.rec_tables_layout);
        checkWorksLayout = findViewById(R.id.check_works_page);
        resultsTable = findViewById(R.id.results_table);
        resultsDetailedLayout = findViewById(R.id.results_detailed_layout);
        resultsDetailedTablesLayout = findViewById(R.id.res_detailed_tables_layout);
        gradesDistributionTable = findViewById(R.id.grades_distribution_table);
        gradesDistributionChart = findViewById(R.id.grades_distribution_chart);
        tasksCompletingTable = findViewById(R.id.tasks_completing_table);
        tasksCompletingChart = findViewById(R.id.tasks_completing_chart);
        cheatersTable = findViewById(R.id.cheaters_table);
        templateSaveCard = findViewById(R.id.template_save_card);
        etalonCreationLayout = findViewById(R.id.etalon_creation_layout);
        etalonNameInput = findViewById(R.id.etalon_check_name_input);
        etalonIcon = findViewById(R.id.icon_preview_check);
        etalonSaveCard = findViewById(R.id.save_etalon_card);

        settings = getSharedPreferences("Preferences", MODE_PRIVATE);

        dbHelper = new DatabaseHelper(this);
        workAdapter = new WorkAdapter(works, new WorkAdapter.OnPageAddListener() {
            @Override
            public void onAddFromGallery(int workPosition) {
                currentWorkPosition = workPosition;
                setPickImagesRequest(PICK_PAGES_REQUEST);
            }

            @Override
            public void onAddFromCamera(int workPosition) {
                currentWorkPosition = workPosition;
                openCamera();
            }
        });
        worksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        worksRecyclerView.setAdapter(workAdapter);

        initAdapters();
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
                    Spinner answerTypesDropdown = row.findViewById(R.id.answer_type_dropdown);
                    Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
                    List<byte[]> criteria = dbHelper.getCriteriaByAnswerId(answer.getId());
                    setupCheckMethodsDropdown(checkMethodsDropdown, row, answer);
                    setupAnswerTypesDropdown(answerTypesDropdown, row, i, CheckActivity.this, detailedAnswersTable, criteria, answer);
                }
                cardGone = true;
                grades = null;
            }
        });
        etalonsRecyclerView.setAdapter(etalonsUseAdapter);

        etalonIconBitmap = ((BitmapDrawable) getDrawable(R.drawable.etalon_plus)).getBitmap();

        uploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPickImagesRequest(PICK_WORKS_REQUEST);
            }
        });

        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_WORKS_REQUEST:
                    handleWorksSelection(data);
                    break;

                case CAPTURE_WORK_REQUEST:
                    if (cameraImageUri != null) addWorkToList(cameraImageUri);
                    break;

                case PICK_PAGES_REQUEST:
                    handlePagesSelection(data);
                    break;

                case CAPTURE_PAGE_REQUEST:
                    if (cameraImageUri != null && currentWorkPosition != -1) {
                        addPageToWork(currentWorkPosition, cameraImageUri);
                        currentWorkPosition = -1;
                    }
                    break;

                case PICK_CRITERIA_PAGES_REQUEST:
                    handleCriteriaImageSelection(data, detailedAnswersTable);
                    break;

                case CAPTURE_CRITERIA_PAGE_REQUEST:
                    if (cameraImageUri != null) addPageToCriteria(cameraImageUri, detailedAnswersTable);
                    break;

                case PICK_ETALON_IMAGE_REQUEST:
                    handleEtalonImageSelection(data);
                    break;
            }
        }
    }

    private void handleWorksSelection(@Nullable Intent data) {
        if (data == null) return;
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri workUri = data.getClipData().getItemAt(i).getUri();
                addWorkToList(workUri);
            }
        }
        else if (data.getData() != null) addWorkToList(data.getData());
    }

    private void handlePagesSelection(@Nullable Intent data) {
        if (data == null || currentWorkPosition == -1) return;
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri pageUri = data.getClipData().getItemAt(i).getUri();
                addPageToWork(currentWorkPosition, pageUri);
            }
        }
        else if (data.getData() != null) addPageToWork(currentWorkPosition, data.getData());
        currentWorkPosition = -1;
    }

    private void addPageToWork(int workPosition, Uri pageUri) {
        long fileSize = getFileSize(pageUri);
        WorkAdapter.WorkItem workItem = works.get(workPosition);
        workItem.addPage(pageUri, fileSize);
        workAdapter.notifyItemChanged(workPosition);
    }

    private void addWorkToList(Uri workUri) {
        long fileSize = getFileSize(workUri);
        WorkAdapter.WorkItem newWork = new WorkAdapter.WorkItem();
        newWork.addPage(workUri, fileSize);
        works.add(newWork);
        workAdapter.notifyItemInserted(works.size() - 1);
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
            View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
            ((TextView) row.findViewById(R.id.task_num)).setText(i + "");
            Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
            Spinner answerTypesDropdown = row.findViewById(R.id.answer_type_dropdown);
            setupCheckMethodsDropdown(checkMethodsDropdown, row, null);
            setupAnswerTypesDropdown(answerTypesDropdown, row, i, this, detailedAnswersTable, null, null);

            answersTable.addView(row);
        }
        setupTextChangedListener(tasksCountInput, this, answersTable, detailedAnswersTable);
        detailedAnswersTable.removeAllViews();
        detailedAnswersTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_header, null));
        detailedAnswersTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_merged_row, null));
        gradesTable.removeAllViews();
        gradesTable.addView(getLayoutInflater().inflate(R.layout.table_grades_header, null));
        if (grades != null) loadGrades(grades, gradesTable);
        else loadGradesSystem(gradesTable);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, tablesPage.getBottom());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void onRecognizedTextPageClick(View view) throws Exception {
        if (works.isEmpty()) {
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
        String taskTypes = getTaskTypes();
        k = 0;
        waitingText.setText("Пожалуйста, подождите, пока текст работ не будет распознан");
        waitAppBarLayout.setVisibility(View.VISIBLE);
        waitAppBarLayout.animate()
                .translationY(0)
                .setDuration(300)
                .start();
        int waitTime = 2 * tasksCount * works.size();
        AIService aiService = new AIService(this);
        initTimer(waitTime * 1000L);
        for (int i = 0; i < works.size(); i++) {
            int finalI = i;
            List<Bitmap> pages = getBitmapsFromPages(this, works.get(i).getPages());
            aiService.recognizeTestAnswers(pages, new SimpleCallback<HashMap<Integer, String>>() {
                @Override
                public void onLoad(HashMap<Integer, String> answersMap) {
                    if (finalI == works.size() - 1) {
                        timer.cancel();
                        waitAppBarLayout.animate()
                                .translationY(-waitAppBarLayout.getHeight())
                                .setDuration(300)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        waitAppBarLayout.setVisibility(View.GONE);
                                    }
                                })
                                .start();
                    }
                    createRecTable(tasksCount, finalI, answersMap);
                }
            }, taskTypes, settings.getString("symbolsToIgnore", "()[].=;"));
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, recognizedTextPage.getBottom());
            }
        });
    }

    public String getTaskTypes() {
        StringBuilder res = new StringBuilder();
        Set<Integer> detailedTasks = detailedTaskPagesMap.keySet();
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            if (detailedTasks.contains(i)) continue;
            View row = answersTable.getChildAt(i);
            String rightAnswer = ((EditText) row.findViewById(R.id.right_answer_input)).getText().toString();
            res.append(i).append(" ");
            if (NUMBER_PATTERN.matcher(rightAnswer).matches()) res.append("число");
            else if (RU_LETTERS.matcher(rightAnswer).matches()) res.append("русские буквы");
            else if (EN_LETTERS.matcher(rightAnswer).matches()) res.append("английские буквы");
            res.append("\n");
        }
        return res.toString();
    }

    public void initTimer(long time) {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(time, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String timeText = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
                remainingTime.setText("Примерное время ожидания: " + timeText);
            }

            @Override
            public void onFinish() {
                initTimer(10000L);
            }
        };

        timer.start();
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
        View mergedRow = getLayoutInflater().inflate(R.layout.table_recognized_text_merged_row, null);
        ((EditText) mergedRow.findViewById(R.id.work_name_input)).setText("Работа №" + (i + 1));
        recTextTable.addView(mergedRow);
        Set<Integer> detailedTasks = detailedTaskPagesMap.keySet();
        for (int j = 1; j <= tasksCount; j++) {
            if (detailedTasks.contains(j)) continue;
            View row = getLayoutInflater().inflate(R.layout.table_recognized_text_row, null);
            ((TextView) row.findViewById(R.id.rec_task_num)).setText(j + "");
            String answer = (answersMap != null && answersMap.get(j) != null) ? answersMap.get(j).replace("null", "") : "";
            ((EditText) row.findViewById(R.id.rec_answer_input)).setText(answer);
            recTextTable.addView(row);
        }
        k++;
    }

    public HashMap<Integer, Map<String, Object>> detectCheaters(Map<String, Map<Integer, String>> studentAnswers,
            HashMap<Integer, Pair<String, Double>> answersMap) {

        class GroupData {
            List<Integer> tasks;
            Map<Integer, String> wrongsKey;
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
        for (GroupData group : finalGroups) {
            Map<String, Object> groupInfo = new HashMap<>();
            List<String> works = new ArrayList<>(group.works);
            works.sort(Comparator.naturalOrder());
            groupInfo.put("works", works);
            groupInfo.put("size", works.size());
            groupInfo.put("tasks", group.tasks);
            int probability = getProbability(works.size(), group.score, maxTotalWeight);
            groupInfo.put("probability", probability);

            result.put(groupId++, groupInfo);
        }
        return result;
    }

    public int getProbability(int groupSize, double totalWeight, double maxTotalWeight) {
        return (int) Math.min(Math.round((totalWeight / maxTotalWeight + 0.07 * (groupSize - 2)) * 100), 100);
    }

    public List<Bitmap> getBitmapsFromPages(Context context, List<WorkAdapter.PageItem> pages) {
        List<Bitmap> bitmaps = new ArrayList<>();
        for (WorkAdapter.PageItem pageItem : pages) {
            Uri imageUri = pageItem.getUri();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                bitmaps.add(bitmap);
            } catch (IOException e) {
                bitmaps.add(null);
            }
        }
        return bitmaps;
    }

    @SuppressLint("SetTextI18n")
    public void onCheckWorksPageClick(View view) {
        if (!isTablesCorrect(answersTable, gradesTable)) {
            Toast.makeText(this, "Проверьте корректность заполнения таблиц!", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<Integer> detailedTasks = detailedTaskPagesMap.keySet();
        if (detailedTasks.isEmpty()) {
            dropCheckResults(null);
            return;
        }
        waitingText.setText("Пожалуйста, подождите, пока работы не будут проверены");
        waitAppBarLayout.setVisibility(View.VISIBLE);
        waitAppBarLayout.animate()
                .translationY(0)
                .setDuration(300)
                .start();
        initTimer(20000L * detailedTasks.size() * works.size());

        List<Pair<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>>> allResults = new ArrayList<>();
        AtomicInteger completedRequests = new AtomicInteger(0);
        int totalDetailedTasks = works.size() * detailedTasks.size();
        AIService aiService = new AIService(this);
        for (int i = 1; i <= works.size(); i++) {
            HashMap<Integer, HashMap<String, Pair<Integer, String>>> workResults = new HashMap<>();
            allResults.add(new Pair<>(i, workResults));
            final int workIndex = i;
            for (int t : detailedTasks) {
                List<Bitmap> workPages = getBitmapsFromPages(this, works.get(i - 1).getPages());
                List<Bitmap> criteria = getBitmapsFromPages(this, detailedTaskPagesMap.get(t));
                aiService.evaluateDetailedTask(workPages, t, criteria, new SimpleCallback<HashMap<String, Pair<Integer, String>>>() {
                    @Override
                    public void onLoad(HashMap<String, Pair<Integer, String>> data) {
                        synchronized (allResults) {
                            for (Pair<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>> result : allResults) {
                                if (result.first == workIndex) {
                                    result.second.put(t, data);
                                    break;
                                }
                            }
                            if (completedRequests.incrementAndGet() == totalDetailedTasks) {
                                runOnUiThread(() -> {
                                    Collections.sort(allResults, (o1, o2) -> o1.first.compareTo(o2.first));
                                    allDetailedResults = allResults;
                                    dropCheckResults(allResults);
                                });
                            }
                        }
                    }
                });
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void dropCheckResults(List<Pair<Integer, HashMap<Integer, HashMap<String, Pair<Integer, String>>>>> allResults) {
        checkWorksLayout.setVisibility(VISIBLE);
        resultsTable.removeAllViews();
        gradesDistributionTable.removeAllViews();
        tasksCompletingTable.removeAllViews();
        cheatersTable.removeAllViews();
        if (cardGone) templateSaveCard.setVisibility(GONE);

        Set<Integer> detailedTasks = detailedTaskPagesMap.keySet();
        HashMap<Integer, List<Object>> answersMapForCheck = new HashMap<>();
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            if (detailedTasks.contains(i)) continue;
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
            answersMapForCheck.put(i, List.of(new Answer(0, 0, 0, "Краткий ответ", answer, points, orderMatters, checkMethod), gradingList));
            answerPointsMap.put(i, new Pair<>(answer, Double.parseDouble(points)));
        }
        resultsTable.addView(getLayoutInflater().inflate(R.layout.table_results_header_main, null));
        androidx.gridlayout.widget.GridLayout header = resultsTable.findViewById(R.id.table_results_title_main);
        int tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        header.setColumnCount(tasksCount + 2);
        for (int i = 1; i <= tasksCount; i++) {
            View taskElement;
            if (!detailedTasks.contains(i)) {
                taskElement = getLayoutInflater().inflate(R.layout.table_results_header_short_task, null);
                ((TextView) taskElement.findViewById(R.id.res_task_num)).setText("Задание " + i);
            }
            else {
                taskElement = getLayoutInflater().inflate(R.layout.table_results_header_detailed_task, null);
                ((TextView) taskElement.findViewById(R.id.res_detailed_task_num)).setText("Задание " + i);
            }
            header.addView(taskElement);
        }
        header.addView(getLayoutInflater().inflate(R.layout.table_results_header_end, null));
        Map<String, Map<Integer, String>> studentAnswers = new HashMap<>();
        HashMap<String, Integer> gradesMap = new HashMap<>();
        HashMap<Pair<Integer, Double>, Integer> tasksMap = new HashMap<>();
        for (int i = 1; i <= works.size(); i++) {
            Map<Integer, String> answers = new HashMap<>();
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_results_row_main, null);
            row.setColumnCount(tasksCount + 2);
            final double[] pointsSum = {0.0};

            int tableIndex = (i - 1) / 3;
            int workOffset = (i - 1) % 3;
            androidx.gridlayout.widget.GridLayout workTable = (androidx.gridlayout.widget.GridLayout) recognizedTextTablesLayout.getChildAt(tableIndex);
            int workRowIndex = 1 + workOffset * (tasksCount - detailedTasks.size() + 1);
            String workName = ((EditText) workTable.getChildAt(workRowIndex).findViewById(R.id.work_name_input)).getText().toString();
            ((TextView) row.findViewById(R.id.res_work_name)).setText(workName);
            for (int t = 1; t <= tasksCount; t++) {
                if (!detailedTasks.contains(t)) {
                    EditText answerInput = workTable.getChildAt(workRowIndex + t).findViewById(R.id.rec_answer_input);
                    String recognizedAnswer = answerInput.getText().toString().trim();
                    String recAnswerForCheating = answerInput.getText().toString().trim();
                    View taskElement = getLayoutInflater().inflate(R.layout.table_results_row_short_task, null);
                    Answer answer = (Answer) answersMapForCheck.get(t).get(0);
                    String rightAnswer = answer.getRightAnswer();
                    boolean orderMatters = answer.getOrderMatters() == 1;
                    double points = 0.0;
                    if (answer.getCheckMethod().equals("Полное совпадение")) {
                        if (!orderMatters) {
                            recognizedAnswer = sortAnswer(recognizedAnswer);
                            rightAnswer = sortAnswer(rightAnswer);
                        }
                        if (recognizedAnswer.equals(rightAnswer))
                            points = Double.parseDouble(answer.getPoints());
                    } else {
                        int mistakes = getMistakes(orderMatters, recognizedAnswer, rightAnswer);
                        List<ComplexCriteria> gradingList = (List<ComplexCriteria>) answersMapForCheck.get(t).get(1);
                        for (ComplexCriteria complexCriteria : gradingList) {
                            if (complexCriteria.getMinMistakes() <= mistakes && complexCriteria.getMaxMistakes() >= mistakes)
                                points = Double.parseDouble(complexCriteria.getPoints());
                        }
                    }
                    ((TextView) taskElement.findViewById(R.id.res_answer)).setText(recAnswerForCheating);
                    ((TextView) taskElement.findViewById(R.id.res_points)).setText(points + "");
                    pointsSum[0] += points;
                    row.addView(taskElement);
                    Pair<Integer, Double> key = new Pair<>(t, points);
                    if (points != 0) tasksMap.put(key, tasksMap.getOrDefault(key, 0) + 1);
                    answers.put(t, recAnswerForCheating);
                }
                else {
                    try {
                        HashMap<String, Pair<Integer, String>> results = allResults.get(i - 1).second.get(t);
                        View taskElement = getLayoutInflater().inflate(R.layout.table_results_row_detailed_task, null);
                        List<String> criteria = results.keySet().stream().collect(Collectors.toList());
                        int taskPoints = 0;
                        for (String c : criteria) {
                            taskPoints += results.get(c).first;
                        }
                        ((TextView) taskElement.findViewById(R.id.res_detailed_points_sum)).setText(taskPoints + "");
                        pointsSum[0] += taskPoints;
                        row.addView(taskElement);
                    } catch (Exception ignored) {
                        Toast.makeText(this, "Произошла ошибка при проверке заданий с развёрнутым ответом. Попробуйте ещё раз!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            studentAnswers.put(workName, answers);

            View rowEnd = getLayoutInflater().inflate(R.layout.table_results_row_end, null);
            ((TextView) rowEnd.findViewById(R.id.points_sum)).setText(pointsSum[0] + "");
            for (int j = 1; j < gradesTable.getChildCount(); j++) {
                View gradeRow = gradesTable.getChildAt(j);
                double min = Double.parseDouble(((EditText) gradeRow.findViewById(R.id.min_points_input)).getText().toString());
                double max = Double.parseDouble(((EditText) gradeRow.findViewById(R.id.max_points_input)).getText().toString());
                if (min <= pointsSum[0] && pointsSum[0] <= max) {
                    String grade = ((TextView) gradeRow.findViewById(R.id.grade_view)).getText().toString();
                    gradesMap.put(grade, gradesMap.getOrDefault(grade, 0) + 1);
                    ((TextView) rowEnd.findViewById(R.id.mark)).setText(grade);
                    break;
                }
            }

            row.addView(rowEnd);
            resultsTable.addView(row);
        }
        if (!detailedTasks.isEmpty()) {
            resultsDetailedLayout.setVisibility(VISIBLE);
            resultsDetailedTablesLayout.removeAllViews();
            resultsDetailedTable = new androidx.gridlayout.widget.GridLayout(this);
            resultsDetailedTable.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            resultsDetailedTable.setColumnCount(1);
            resultsDetailedTable.setBackgroundColor(getResources().getColor(R.color.dark_green));
            resultsDetailedTable.addView(getLayoutInflater().inflate(R.layout.table_results_detailed_header, null));
            resultsDetailedTablesLayout.addView(resultsDetailedTable);
            k = 0;
            for (int i = 1; i <= works.size(); i++) {
                if (k == 3) {
                    resultsDetailedTable = new GridLayout(this);
                    resultsDetailedTable.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    resultsDetailedTable.setColumnCount(1);
                    resultsDetailedTable.setBackgroundColor(getResources().getColor(R.color.dark_green));
                    resultsDetailedTable.addView(getLayoutInflater().inflate(R.layout.table_results_detailed_header, null));
                    resultsDetailedTablesLayout.addView(resultsDetailedTable);
                    k = 0;
                }
                View mergedRow = getLayoutInflater().inflate(R.layout.table_results_detailed_merged_row, null);
                String workName = ((TextView) resultsTable.getChildAt(i).findViewById(R.id.res_work_name)).getText().toString();;
                ((TextView) mergedRow.findViewById(R.id.work_name)).setText(workName);
                resultsDetailedTable.addView(mergedRow);
                for (int j = 1; j <= tasksCount; j++) {
                    if (!detailedTasks.contains(j)) continue;
                    View rowMain = getLayoutInflater().inflate(R.layout.table_results_detailed_row_main, null);
                    ((TextView) rowMain.findViewById(R.id.res_det_task_num)).setText(j + "");
                    try {
                        androidx.gridlayout.widget.GridLayout criteriaTable = rowMain.findViewById(R.id.criteria_table);
                        HashMap<String, Pair<Integer, String>> results = allResults.get(i - 1).second.get(j);
                        List<String> criteria = results.keySet().stream().collect(Collectors.toList());
                        for (String c : criteria) {
                            View rowCriteria = getLayoutInflater().inflate(R.layout.table_results_detailed_row_criteria, null);
                            ((TextView) rowCriteria.findViewById(R.id.res_criteria)).setText(c);
                            ((TextView) rowCriteria.findViewById(R.id.res_detailed_points)).setText(results.get(c).first + "");
                            ((TextView) rowCriteria.findViewById(R.id.res_detailed_comment)).setText(results.get(c).second);
                            criteriaTable.addView(rowCriteria);
                        }
                    } catch (Exception ignored) {}
                    resultsDetailedTable.addView(rowMain);
                }
                k++;
            }
        }
        else resultsDetailedLayout.setVisibility(GONE);
        gradesDistributionTable.addView(getLayoutInflater().inflate(R.layout.table_grades_distribution_header, null));
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        int percentagesSum = 0;
        for (int i = 1; i < gradesTable.getChildCount(); i++) {
            View gradeRow = getLayoutInflater().inflate(R.layout.table_grades_distribution_row, null);
            String grade = ((TextView) gradesTable.getChildAt(i).findViewById(R.id.grade_view)).getText().toString();
            ((TextView) gradeRow.findViewById(R.id.grade_dist)).setText(grade);
            Integer pupilsGraded = gradesMap.getOrDefault(grade, 0);
            pieEntries.add(new PieEntry(pupilsGraded, grade));
            ((TextView) gradeRow.findViewById(R.id.pupils_graded)).setText(pupilsGraded + "");
            if (i != gradesTable.getChildCount() - 1) {
                int pupilsPercentage = Math.round((float) pupilsGraded / works.size() * 100);
                percentagesSum += pupilsPercentage;
                ((TextView) gradeRow.findViewById(R.id.pupils_percentage)).setText(pupilsPercentage + "%");
            }
            else ((TextView) gradeRow.findViewById(R.id.pupils_percentage)).setText((100 - percentagesSum) + "%");
            gradesDistributionTable.addView(gradeRow);
        }
        Typeface typeface = ResourcesCompat.getFont(this, R.font.roboto_bold);
        PieDataSet pieDataSet = new PieDataSet(pieEntries, null);
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(16);
        pieDataSet.setValueTypeface(typeface);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        Legend legend = gradesDistributionChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(13);
        legend.setTypeface(typeface);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        gradesDistributionChart.setData(pieData);
        gradesDistributionChart.setExtraOffsets(0, 0, 0, -8);
        gradesDistributionChart.getDescription().setEnabled(false);
        gradesDistributionChart.setDrawHoleEnabled(false);
        gradesDistributionChart.setRotationEnabled(false);
        gradesDistributionChart.invalidate();

        tasksCompletingTable.addView(getLayoutInflater().inflate(R.layout.table_tasks_comleting_header, null));
        List<Pair<Integer, Double>> keys = new ArrayList<>();
        for (int i = 1; i < answersTable.getChildCount(); i++) {
            View row = answersTable.getChildAt(i);
            if (detailedTasks.contains(i)) continue;
            if (((Spinner) row.findViewById(R.id.check_method_dropdown)).getSelectedItemPosition() == 0) {
                double points = Double.parseDouble(((EditText) row.findViewById(R.id.points_input)).getText().toString());
                keys.add(new Pair<>(i, points));
            }
            else {
                androidx.gridlayout.widget.GridLayout complexGradingTable = row.findViewById(R.id.complex_grading_table);
                for (int j = complexGradingTable.getChildCount() - 2; j >= 1; j--) {
                    double points = Double.parseDouble(((EditText) complexGradingTable.getChildAt(j).findViewById(R.id.complex_points_input)).getText().toString());
                    keys.add(new Pair<>(i, points));
                }
            }
        }
        int prevTask = 0;
        ArrayList<Entry> entries = new ArrayList<>();
        int prevPercentage = 0;
        for (int i = 0; i < keys.size(); i++) {
            View taskRow = getLayoutInflater().inflate(R.layout.table_tasks_comleting_row, null);
            Pair<Integer, Double> key = keys.get(i);
            ((TextView) taskRow.findViewById(R.id.points)).setText(key.second + "");
            int pupilsCompleted = tasksMap.getOrDefault(key, 0);
            ((TextView) taskRow.findViewById(R.id.pupils_completed)).setText(pupilsCompleted + "");
            int pupilsPercentage = Math.round((float) pupilsCompleted / works.size() * 100);
            ((TextView) taskRow.findViewById(R.id.pupils_comp_percentage)).setText(pupilsPercentage + "%");
            if (prevTask == 0) ((TextView) taskRow.findViewById(R.id.comp_task_num)).setText(key.first + "");
            else if (key.first != prevTask) {
                entries.add(new Entry(prevTask, prevPercentage));
                ((TextView) taskRow.findViewById(R.id.comp_task_num)).setText(key.first + "");
            }
            tasksCompletingTable.addView(taskRow);
            prevTask = key.first;
            prevPercentage = pupilsPercentage;
        }
        entries.add(new Entry(prevTask, prevPercentage));

        LineDataSet lineDataSet = new LineDataSet(entries, "Процент выполнения заданий");
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setCircleRadius(0);
        lineDataSet.setColor(getColor(R.color.light_green));
        lineDataSet.setDrawValues(false);
        XAxis xAxis = tasksCompletingChart.getXAxis();
        xAxis.setAxisMinimum(1);
        xAxis.setAxisMaximum(tasksCount);
        xAxis.setLabelCount(tasksCount);
        xAxis.setGranularity(1);
        xAxis.setGridColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(13);
        xAxis.setTypeface(typeface);
        YAxis yAxis = tasksCompletingChart.getAxisLeft();
        yAxis.setXOffset(10);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setLabelCount(11, true);
        yAxis.setGranularity(10);
        yAxis.setGridColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(13);
        yAxis.setTypeface(typeface);
        legend = tasksCompletingChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(13);
        legend.setTypeface(typeface);
        SquarePointRenderer squarePointRenderer = new SquarePointRenderer(tasksCompletingChart, tasksCompletingChart.getAnimator(), tasksCompletingChart.getViewPortHandler());
        squarePointRenderer.setSquareSize(24);
        ViewGroup.LayoutParams params = tasksCompletingChart.getLayoutParams();
        params.width = (int) (60 * tasksCount * getResources().getDisplayMetrics().density);
        tasksCompletingChart.setLayoutParams(params);
        LineData lineData = new LineData(lineDataSet);
        tasksCompletingChart.setData(lineData);
        tasksCompletingChart.getAxisRight().setEnabled(false);
        tasksCompletingChart.getDescription().setEnabled(false);
        tasksCompletingChart.setExtraOffsets(0, 0, 0, 8);
        tasksCompletingChart.setTouchEnabled(false);
        tasksCompletingChart.setRenderer(squarePointRenderer);
        tasksCompletingChart.invalidate();

        cheatersTable.addView(getLayoutInflater().inflate(R.layout.table_cheaters_header, null));
        HashMap<Integer, Map<String, Object>> cheatersGroups = detectCheaters(studentAnswers, answerPointsMap);
        if (cheatersGroups.isEmpty()) cheatersTable.addView(getLayoutInflater().inflate(R.layout.table_cheaters_merged_row, null));
        else {
            for (int i = 1; i <= cheatersGroups.size(); i++) {
                Map<String, Object> group = cheatersGroups.get(i);
                View row = getLayoutInflater().inflate(R.layout.table_cheaters_row, null);
                ((TextView) row.findViewById(R.id.group_num)).setText(i + "");
                List<String> works = (List<String>) group.get("works");
                List<String> tasks = ((List<Integer>) group.get("tasks")).stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                ((TextView) row.findViewById(R.id.works)).setText(String.join(", ", works));
                ((TextView) row.findViewById(R.id.group_size)).setText(group.get("size") + "");
                ((TextView) row.findViewById(R.id.tasks_list)).setText(String.join(", ", tasks));
                ((TextView) row.findViewById(R.id.probability)).setText(group.get("probability") + "%");
                cheatersTable.addView(row);
            }
        }
        timer.cancel();
        waitAppBarLayout.animate()
                .translationY(-waitAppBarLayout.getHeight())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        waitAppBarLayout.setVisibility(View.GONE);
                    }
                })
                .start();
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
                String cellContent;
                try {
                    cellContent = String.valueOf(table.getRow(i).getCell(col));
                } catch (Exception e) { continue; }
                if (cellContent.length() > maxLength) maxLength = cellContent.length();
            }
            int baseWidth = Math.min(maxLength * 300 + 300, 9000);
            columnWidths.put(col, baseWidth);
        }
        for (Map.Entry<Integer, Integer> entry : columnWidths.entrySet()) {
            table.setColumnWidth(entry.getKey(), entry.getValue());
        }
    }

    public void createStyles(Workbook table) {
        Font whiteFont = table.createFont();
        whiteFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle commonStyle = table.createCellStyle();
        commonStyle.setBorderTop(BorderStyle.MEDIUM);
        commonStyle.setBorderBottom(BorderStyle.MEDIUM);
        commonStyle.setBorderLeft(BorderStyle.MEDIUM);
        commonStyle.setBorderRight(BorderStyle.MEDIUM);
        commonStyle.setAlignment(HorizontalAlignment.CENTER);
        commonStyle.setWrapText(true);

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
        redBackground.setFont(whiteFont);
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
                    createStyles(table);
                }
            } else {
                table = new XSSFWorkbook();
                createStyles(table);
            }
            if (cursor != null) cursor.close();
        }
        else {
            table = new XSSFWorkbook();
            createStyles(table);
        }
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
            lastRow = sheet.getLastRowNum() + 3;
            if (lastRow == 2) lastRow -= 2;
        }
        else sheet = table.createSheet("Результаты " + (table.getNumberOfSheets() + 1));
        boolean isColoringEnabled = settings.getBoolean("isColoringEnabled", true);
        boolean isAnalysisEnabled = settings.getBoolean("isAnalysisEnabled", false);

        CellStyle commonStyle = table.getCellStyleAt(1);
        CellStyle greenBackground = table.getCellStyleAt(2);
        CellStyle yellowBackground = table.getCellStyleAt(3);
        CellStyle redBackground = table.getCellStyleAt(4);

        Row headerRow1 = sheet.createRow(lastRow);
        lastRow++;
        Row headerRow2 = sheet.createRow(lastRow);
        Cell cell = headerRow1.createCell(0);
        cell.setCellValue("Работа");
        cell.setCellStyle(commonStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow, 0, 0));
        int tasksCount = Integer.parseInt(tasksCountInput.getText().toString());
        Set<Integer> detailedTasks = detailedTaskPagesMap.keySet();
        int k = 0;
        for (int i = 1; i <= tasksCount * 2 - 1; i+=2) {
            cell = headerRow1.createCell(i);
            cell.setCellValue("Задание " + (i - k));
            cell.setCellStyle(commonStyle);
            cell = headerRow2.createCell(i);
            if (!detailedTasks.contains(i - k)) {
                cell.setCellValue("Ответ");
                cell.setCellStyle(commonStyle);
                cell = headerRow2.createCell(i + 1);
                cell.setCellValue("Балл");
                cell.setCellStyle(commonStyle);
                sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow - 1, i, i + 1));
            }
            else {
                cell.setCellValue("Сумма баллов");
                cell.setCellStyle(commonStyle);
                i -= 1;
            }
            k++;
        }
        cell = headerRow1.createCell(tasksCount * 2 - detailedTasks.size() + 1);
        cell.setCellValue("Сумма баллов");
        cell.setCellStyle(commonStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow, tasksCount * 2 - detailedTasks.size() + 1, tasksCount * 2 - detailedTasks.size() + 1));
        cell = headerRow1.createCell(tasksCount * 2 - detailedTasks.size() + 2);
        cell.setCellValue("Оценка");
        cell.setCellStyle(commonStyle);
        sheet.addMergedRegion(new CellRangeAddress(lastRow - 1, lastRow, tasksCount * 2 - detailedTasks.size() + 2, tasksCount * 2 - detailedTasks.size() + 2));

        for (int i = 1; i <= works.size(); i++) {
            k = 0;
            lastRow++;
            androidx.gridlayout.widget.GridLayout workRow = (androidx.gridlayout.widget.GridLayout) resultsTable.getChildAt(i);
            String name = ((TextView) workRow.findViewById(R.id.res_work_name)).getText().toString();
            Row row = sheet.createRow(lastRow);
            cell = row.createCell(0);
            cell.setCellValue(name);
            cell.setCellStyle(commonStyle);
            for (int j = 1; j <= tasksCount * 2 - 1; j+=2) {
                View taskElement = workRow.getChildAt(j - k);
                cell = row.createCell(j);
                if (!detailedTasks.contains(j - k)) {
                    cell.setCellValue(((TextView) taskElement.findViewById(R.id.res_answer)).getText().toString());
                    cell.setCellStyle(commonStyle);
                    cell = row.createCell(j + 1);
                    double points = Double.parseDouble(((TextView) taskElement.findViewById(R.id.res_points)).getText().toString());
                    double maxPoints = answerPointsMap.get(j - k).second;
                    cell.setCellValue(points);
                    if (isColoringEnabled) {
                        if (points == 0) cell.setCellStyle(redBackground);
                        else if (points < maxPoints) cell.setCellStyle(yellowBackground);
                        else cell.setCellStyle(greenBackground);
                    } else cell.setCellStyle(commonStyle);
                    k++;
                }
                else {
                    cell.setCellValue(((TextView) taskElement.findViewById(R.id.res_detailed_points_sum)).getText().toString());
                    cell.setCellStyle(commonStyle);
                    j -= 1;
                }
            }
            View end = workRow.getChildAt(tasksCount + 1);
            cell = row.createCell(tasksCount * 2 - detailedTasks.size() + 1);
            cell.setCellValue(((TextView) end.findViewById(R.id.points_sum)).getText().toString());
            cell.setCellStyle(commonStyle);
            cell = row.createCell(tasksCount * 2 - detailedTasks.size() + 2);
            cell.setCellValue(((TextView) end.findViewById(R.id.mark)).getText().toString());
            cell.setCellStyle(commonStyle);
        }

        if (!detailedTasks.isEmpty()) {
            lastRow += 2;
            Row row = sheet.createRow(lastRow);
            cell = row.createCell(0);
            cell.setCellValue("Номер задания");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(1);
            cell.setCellValue("Критерий");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(2);
            cell.setCellValue("Балл");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(3);
            cell.setCellValue("Комментарий");
            cell.setCellStyle(commonStyle);
            for (int i = 1; i <= works.size(); i++) {
                lastRow++;
                row = sheet.createRow(lastRow);
                String name = ((TextView) resultsTable.getChildAt(i).findViewById(R.id.res_work_name)).getText().toString();
                cell = row.createCell(0);
                cell.setCellValue(name);
                cell.setCellStyle(commonStyle);
                sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow, 0, 3));
                for (int t = 1; t <= tasksCount; t++) {
                    if (detailedTasks.contains(t)) {
                        HashMap<String, Pair<Integer, String>> results = allDetailedResults.get(i - 1).second.get(t);
                        List<String> criteria = results.keySet().stream().collect(Collectors.toList());
                        for (String c : criteria) {
                            lastRow++;
                            row = sheet.createRow(lastRow);
                            cell = row.createCell(0);
                            cell.setCellValue(t);
                            cell.setCellStyle(commonStyle);
                            cell = row.createCell(1);
                            cell.setCellValue(c);
                            cell.setCellStyle(commonStyle);
                            cell = row.createCell(2);
                            cell.setCellValue(results.get(c).first);
                            cell.setCellStyle(commonStyle);
                            cell = row.createCell(3);
                            cell.setCellValue(results.get(c).second);
                            cell.setCellStyle(commonStyle);
                        }
                    }
                }
            }
        }

        if (isAnalysisEnabled) {
            lastRow += 2;
            int chartStartRow = lastRow;
            Row row = sheet.createRow(lastRow);
            cell = row.createCell(0);
            cell.setCellValue("Распределение оценок");
            sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow, 0, 2));
            cell.setCellStyle(commonStyle);
            lastRow++;
            row = sheet.createRow(lastRow);
            cell = row.createCell(0);
            cell.setCellValue("Оценка");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(1);
            cell.setCellValue("Кол-во учеников, получивших оценку");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(2);
            cell.setCellValue("Процент от общего числа учеников");
            cell.setCellStyle(commonStyle);
            for (int i = 1; i < gradesDistributionTable.getChildCount(); i++) {
                View tableRow = gradesDistributionTable.getChildAt(i);
                lastRow++;
                row = sheet.createRow(lastRow);
                cell = row.createCell(0);
                cell.setCellValue(((TextView) tableRow.findViewById(R.id.grade_dist)).getText().toString());
                cell.setCellStyle(commonStyle);
                cell = row.createCell(1);
                cell.setCellValue(Integer.parseInt(((TextView) tableRow.findViewById(R.id.pupils_graded)).getText().toString()));
                cell.setCellStyle(commonStyle);
                cell = row.createCell(2);
                cell.setCellValue(((TextView) tableRow.findViewById(R.id.pupils_percentage)).getText().toString());
                cell.setCellStyle(commonStyle);
            }
            XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, chartStartRow, 11, chartStartRow + 10);
            XSSFChart pieChart = drawing.createChart(anchor);
            pieChart.setTitleText("Распределение оценок");
            XDDFDataSource<String> grades = XDDFDataSourcesFactory.fromStringCellRange(
                    (XSSFSheet) sheet, new CellRangeAddress(chartStartRow + 1, lastRow, 0, 0)
            );
            XDDFNumericalDataSource<Double> pupilsGraded = XDDFDataSourcesFactory.fromNumericCellRange(
                    (XSSFSheet) sheet, new CellRangeAddress(chartStartRow + 1, lastRow, 1, 1)
            );
            XDDFChartData pieData = pieChart.createData(ChartTypes.PIE, null, null);
            pieData.addSeries(grades, pupilsGraded);
            XDDFChartLegend pieLegend = pieChart.getOrAddLegend();
            pieLegend.setPosition(LegendPosition.BOTTOM);
            pieLegend.setOverlay(false);
            pieChart.plot(pieData);
            if (lastRow > chartStartRow + 10) lastRow += 2;
            else lastRow = chartStartRow + 12;
            chartStartRow = lastRow;
            row = sheet.createRow(lastRow);
            cell = row.createCell(0);
            cell.setCellValue("Статистика выполнения заданий");
            sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow, 0, 3));
            cell.setCellStyle(commonStyle);
            lastRow++;
            row = sheet.createRow(lastRow);
            cell = row.createCell(0);
            cell.setCellValue("Номер задания");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(1);
            cell.setCellValue("Балл");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(2);
            cell.setCellValue("Кол-во учеников, получивших балл");
            cell.setCellStyle(commonStyle);
            cell = row.createCell(3);
            cell.setCellValue("Процент от общего числа учеников");
            cell.setCellStyle(commonStyle);
            List<String> taskNums = new ArrayList<>();
            List<Integer> percentages = new ArrayList<>();
            boolean isFirstMerge = true;
            int firstTaskRowNum = 0;
            for (int i = 1; i < tasksCompletingTable.getChildCount(); i++) {
                View tableRow = tasksCompletingTable.getChildAt(i);
                lastRow++;
                row = sheet.createRow(lastRow);
                cell = row.createCell(0);
                String taskNum = ((TextView) tableRow.findViewById(R.id.comp_task_num)).getText().toString();
                cell.setCellValue(taskNum);
                cell.setCellStyle(commonStyle);
                if (taskNum.isEmpty()) {
                    if (!isFirstMerge) sheet.removeMergedRegion(sheet.getNumMergedRegions() - 1);
                    sheet.addMergedRegion(new CellRangeAddress(firstTaskRowNum, lastRow, 0, 0));
                    isFirstMerge = false;
                }
                else {
                    taskNums.add(taskNum);
                    if (i != 1) {
                        String percentage = sheet.getRow(lastRow - 1).getCell(3).getStringCellValue();
                        percentages.add(Integer.valueOf(percentage.substring(0, percentage.length() - 1)));
                    }
                    firstTaskRowNum = lastRow;
                    isFirstMerge = true;
                }
                cell = row.createCell(1);
                cell.setCellValue(((TextView) tableRow.findViewById(R.id.points)).getText().toString());
                cell.setCellStyle(commonStyle);
                cell = row.createCell(2);
                cell.setCellValue(((TextView) tableRow.findViewById(R.id.pupils_completed)).getText().toString());
                cell.setCellStyle(commonStyle);
                cell = row.createCell(3);
                cell.setCellValue(((TextView) tableRow.findViewById(R.id.pupils_comp_percentage)).getText().toString());
                cell.setCellStyle(commonStyle);
            }
            if (tasksCompletingTable.getChildCount() > 1) {
                String percentage = sheet.getRow(lastRow).getCell(3).getStringCellValue();
                percentages.add(Integer.valueOf(percentage.substring(0, percentage.length() - 1)));
                for (int i = chartStartRow; i < chartStartRow + tasksCount - detailedTasks.size(); i++) {
                    row = sheet.getRow(i);
                    cell = row.createCell(5);
                    cell.setCellValue(taskNums.get(i - chartStartRow));
                    cell.setCellStyle(commonStyle);
                    cell = row.createCell(6);
                    cell.setCellValue(percentages.get(i - chartStartRow));
                    cell.setCellStyle(commonStyle);
                }
                anchor = drawing.createAnchor(0, 0, 0, 0, 8, chartStartRow, 8 + tasksCount - detailedTasks.size(), chartStartRow + 10);
                XSSFChart lineChart = drawing.createChart(anchor);
                XDDFDataSource<String> xData = XDDFDataSourcesFactory.fromStringCellRange(
                        (XSSFSheet) sheet, new CellRangeAddress(chartStartRow, chartStartRow + tasksCount - detailedTasks.size(), 5, 5)
                );
                XDDFNumericalDataSource<Double> yData = XDDFDataSourcesFactory.fromNumericCellRange(
                        (XSSFSheet) sheet, new CellRangeAddress(chartStartRow , chartStartRow + tasksCount - detailedTasks.size(), 6, 6)
                );
                XDDFCategoryAxis xAxis = lineChart.createCategoryAxis(AxisPosition.BOTTOM);
                XDDFValueAxis yAxis = lineChart.createValueAxis(AxisPosition.LEFT);
                yAxis.setNumberFormat("#\"%\"");
                XDDFLineChartData lineData = (XDDFLineChartData) lineChart.createData(ChartTypes.LINE, xAxis, yAxis);
                XDDFLineChartData.Series series = (XDDFLineChartData.Series) lineData.addSeries(xData, yData);
                series.setTitle("Процент выполнения заданий", null);
                series.setMarkerStyle(MarkerStyle.SQUARE);
                XDDFChartLegend lineLegend = lineChart.getOrAddLegend();
                lineLegend.setPosition(LegendPosition.BOTTOM);
                lineLegend.setOverlay(false);
                lineChart.plot(lineData);
            }
        }

        updateColumnsWidth(sheet, tasksCount * 2 + 3);

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
                createEtalon(CheckActivity.this, tasksCountInput, etalonNameInput, answersTable, gradesTable);
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
        setPickEtalonIconRequest();
    }
}