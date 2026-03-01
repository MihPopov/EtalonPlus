package com.cubably.gradeplus.Presentation;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cubably.gradeplus.Common.DatabaseHelper;
import com.cubably.gradeplus.Common.EtalonTransferService;
import com.cubably.gradeplus.R;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Активность для управления эталонами и создания новых. Отсюда можно перейти в EtalonActivity.
 */
public class StorageActivity extends BaseActivity {

    private static final int IMPORT_ETALON_REQUEST = 101;
    private static final int EXPORT_ETALON_REQUEST = 102;
    private EtalonTransferService etalonTransferService;
    private int selectedEtalonIdForExport = -1;
    private String selectedEtalonNameForExport = "";

    ScrollView scrollView;
    RecyclerView recyclerView;
    EtalonsAdapter etalonsAdapter;
    CardView uploadEtalonIconCard;
    LinearLayout etalonCreationPage, etalonTablesCreationPage;
    TextInputEditText etalonNameInput, tasksCountInput;
    androidx.gridlayout.widget.GridLayout answersEtalonTable, gradesEtalonTable, detailedEtalonTable;

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
        detailedEtalonTable = findViewById(R.id.detailed_etalon_creation_table);

        etalonIconBitmap = ((BitmapDrawable) getDrawable(R.drawable.etalon_plus)).getBitmap();

        //Загрузка эталонов из базы данных
        dbHelper = new DatabaseHelper(this);
        etalonTransferService = new EtalonTransferService(dbHelper);
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
        }, new EtalonsAdapter.OnEtalonExportListener() {
            @Override
            public void onExport(int position) {
                selectEtalonForExport(position);
            }
        });
        recyclerView.setAdapter(etalonsAdapter);

        initAdapters();

        uploadEtalonIconCard.setOnClickListener(new View.OnClickListener() {
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

                case IMPORT_ETALON_REQUEST:
                    handleEtalonImport(data);
                    break;

                case EXPORT_ETALON_REQUEST:
                    handleEtalonExport(data);
                    break;
            }
        }
    }

    public void onExitFromStorageClick(View view) {
        startActivity(new Intent(StorageActivity.this, HomeActivity.class));
    }

    public void onEtalonImportClick(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityForResult(intent, IMPORT_ETALON_REQUEST);
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

    //Генерация таблиц для создания эталона
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
        answersEtalonTable.removeAllViews();
        answersEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_answers_header, null));
        for (int i = 1; i <= tasksCount; i++) {
            View row = getLayoutInflater().inflate(R.layout.table_answers_row, null);
            ((TextView) row.findViewById(R.id.task_num)).setText(i + "");
            Spinner checkMethodsDropdown = row.findViewById(R.id.check_method_dropdown);
            Spinner answerTypesDropdown = row.findViewById(R.id.answer_type_dropdown);

            setupCheckMethodsDropdown(checkMethodsDropdown, row, null);
            setupAnswerTypesDropdown(answerTypesDropdown, row, i, this, detailedEtalonTable, null, null);

            answersEtalonTable.addView(row);
        }
        setupTextChangedListener(tasksCountInput, this, answersEtalonTable, detailedEtalonTable);
        detailedEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_header, null));
        detailedEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_detailed_answers_merged_row, null));
        gradesEtalonTable.addView(getLayoutInflater().inflate(R.layout.table_grades_header, null));
        loadGradesSystem(gradesEtalonTable);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, etalonTablesCreationPage.getBottom());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onCreateEtalonClick(View view) {
        createEtalon(this, tasksCountInput, etalonNameInput, answersEtalonTable, gradesEtalonTable);
        etalonList.clear();
        etalonList.addAll(dbHelper.getAllEtalons());
        etalonsAdapter.notifyDataSetChanged();
    }

    private void selectEtalonForExport(int position) {
        selectedEtalonIdForExport = etalonList.get(position).getId();
        selectedEtalonNameForExport = etalonList.get(position).getName();
        Uri exportUri = createMediaStoreExportUri(selectedEtalonNameForExport);
        if (exportUri == null) {
            Toast.makeText(this, "Не удалось создать файл для экспорта", Toast.LENGTH_SHORT).show();
            clearExportSelection();
            return;
        }
        handleEtalonExport(exportUri);
    }

    private void handleEtalonExport(@Nullable Intent data) {
        Uri uri = data != null ? data.getData() : null;
        handleEtalonExport(uri);
    }

    private void handleEtalonExport(@Nullable Uri uri) {
        if (uri == null || selectedEtalonIdForExport < 0) {
            Toast.makeText(this, "Не удалось выполнить экспорт", Toast.LENGTH_SHORT).show();
            clearExportSelection();
            return;
        }
        File zipFile = etalonTransferService.exportEtalonZip(selectedEtalonIdForExport, selectedEtalonNameForExport, this);
        if (zipFile == null || !zipFile.exists()) {
            Toast.makeText(this, "Ошибка создания ZIP", Toast.LENGTH_LONG).show();
            clearExportSelection();
            return;
        }
        try (InputStream fis = new FileInputStream(zipFile);
             OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new IOException("Поток записи недоступен");
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
            Toast.makeText(this, "Эталон экспортирован", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            clearExportSelection();
        }
    }

    @Nullable
    private Uri createMediaStoreExportUri(String etalonName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, etalonName.replaceAll("\\s+", "_") + ".zip");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/zip");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        return getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
    }

    private void clearExportSelection() {
        selectedEtalonIdForExport = -1;
        selectedEtalonNameForExport = "";
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleEtalonImport(@Nullable Intent data) {
        Uri uri = data != null ? data.getData() : null;
        if (uri == null) {
            Toast.makeText(this, "Не удалось выбрать файл", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            etalonTransferService.importEtalonZip(uri, this);
            etalonList.clear();
            etalonList.addAll(dbHelper.getAllEtalons());
            etalonsAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Эталон импортирован", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка импорта: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}