package com.mihpopov.etalonplus.Presentation;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

import com.mihpopov.etalonplus.Common.DatabaseHelper;
import com.mihpopov.etalonplus.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Активность для управления эталонами и создания новых. Отсюда можно перейти в EtalonActivity.
 */
public class StorageActivity extends BaseActivity {

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
    }
}