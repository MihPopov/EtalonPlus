package com.example.bigchallengesproject.Presentation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bigchallengesproject.Common.DatabaseHelper;
import com.example.bigchallengesproject.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsActivity extends AppCompatActivity {

    androidx.gridlayout.widget.GridLayout gradesSystemTable;
    TextInputEditText tableNameInput, symbolsToIgnoreInput;
    MaterialSwitch oneTableSwitch, oneSheetSwitch, coloringSwitch, analysisSwitch;
    LinearLayout oneSheetLayout;

    DatabaseHelper dbHelper;
    SharedPreferences settings;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));

        gradesSystemTable = findViewById(R.id.grades_system_table);
        tableNameInput = findViewById(R.id.table_name_input);
        symbolsToIgnoreInput = findViewById(R.id.symbols_to_ignore_input);
        oneTableSwitch = findViewById(R.id.one_table_switch);
        oneSheetLayout = findViewById(R.id.one_sheet_layout);
        oneSheetSwitch = findViewById(R.id.one_sheet_switch);
        coloringSwitch = findViewById(R.id.coloring_switch);
        analysisSwitch = findViewById(R.id.analysis_switch);

        dbHelper = new DatabaseHelper(this);
        settings = getSharedPreferences("Preferences", MODE_PRIVATE);

        symbolsToIgnoreInput.setText(settings.getString("symbolsToIgnore", "()[].=;"));

        List<String> gradesSystem = dbHelper.getGradesSystem();
        gradesSystemTable.addView(getLayoutInflater().inflate(R.layout.table_grades_system_header, null));
        for (int i = 1; i <= gradesSystem.size(); i++) {
            androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_grades_system_row, null);
            ((TextView) row.findViewById(R.id.grade_num)).setText(i + "");
            ((EditText) row.findViewById(R.id.grade_input)).setText(gradesSystem.get(i - 1));
            gradesSystemTable.addView(row);
        }

        tableNameInput.setText(settings.getString("tableName", "Результаты проверки"));
        boolean isOneTableEnabled = settings.getBoolean("isOneTableEnabled", false);
        oneTableSwitch.setChecked(isOneTableEnabled);
        if (isOneTableEnabled) {
            oneSheetLayout.setVisibility(VISIBLE);
            oneSheetSwitch.setChecked(settings.getBoolean("isOneSheetEnabled", false));
        }
        oneTableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) oneSheetLayout.setVisibility(VISIBLE);
                else oneSheetLayout.setVisibility(GONE);
            }
        });
        coloringSwitch.setChecked(settings.getBoolean("isColoringEnabled", true));
        analysisSwitch.setChecked(settings.getBoolean("isAnalysisEnabled", false));
    }

    public void onExitFromSettingsClick(View view) {
        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
    }

    public void onSaveSettingsClick(View view) {
        String tableName = tableNameInput.getText().toString().trim();
        if (tableName.isEmpty()) {
            Toast.makeText(this, "Напишите имя таблицы корректно!", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            String symbolsToIgnore = symbolsToIgnoreInput.getText().toString();
            symbolsToIgnore = symbolsToIgnore.replace(" ", "").chars()
                    .distinct()
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining());
            boolean isOneTableEnabled = oneTableSwitch.isChecked();
            SharedPreferences.Editor prefEditor = settings.edit();
            prefEditor.putString("symbolsToIgnore", symbolsToIgnore);
            prefEditor.putString("tableName", tableName);
            prefEditor.putBoolean("isOneTableEnabled", isOneTableEnabled);
            if (isOneTableEnabled) prefEditor.putBoolean("isOneSheetEnabled", oneSheetSwitch.isChecked());
            prefEditor.putBoolean("isColoringEnabled", coloringSwitch.isChecked());
            prefEditor.putBoolean("isAnalysisEnabled", analysisSwitch.isChecked());
            prefEditor.apply();
        }
        List<String> newGradesSystem = new ArrayList<>();
        for (int i = 1; i < gradesSystemTable.getChildCount(); i++) {
            View row = gradesSystemTable.getChildAt(i);
            String grade = ((EditText) row.findViewById(R.id.grade_input)).getText().toString().trim();
            if (grade.isEmpty()) {
                Toast.makeText(this, "Заполните таблицу корректно!", Toast.LENGTH_SHORT).show();
                return;
            }
            newGradesSystem.add(grade);
        }
        dbHelper.updateGradesSystem(newGradesSystem);
        Toast.makeText(this, "Настройки сохранены!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    public void onAddGradeClick(View view) {
        androidx.gridlayout.widget.GridLayout row = (androidx.gridlayout.widget.GridLayout) getLayoutInflater().inflate(R.layout.table_grades_system_row, null);
        ((TextView) row.findViewById(R.id.grade_num)).setText(gradesSystemTable.getChildCount() + "");
        gradesSystemTable.addView(row);
    }

    public void onRemoveGradeClick(View view) {
        if (gradesSystemTable.getChildCount() > 2) gradesSystemTable.removeViewAt(gradesSystemTable.getChildCount() - 1);
    }
}