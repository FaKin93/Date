package com.example.date;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private TextView selectedDate;
    private EditText eventInput;
    private Button addEventButton, deleteEventButton, themeToggleButton;
    private ListView eventList;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> events;
    private HashMap<String, ArrayList<String>> eventsMap;

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        selectedDate = findViewById(R.id.selectedDate);
        eventInput = findViewById(R.id.eventInput);
        addEventButton = findViewById(R.id.addEventButton);
        deleteEventButton = findViewById(R.id.deleteEventButton);
        themeToggleButton = findViewById(R.id.themeToggleButton);
        eventList = findViewById(R.id.eventList);

        eventsMap = new HashMap<>();
        events = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, events);
        eventList.setAdapter(adapter);
        eventList.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // Позволяет выбирать одно событие

        sharedPreferences = getSharedPreferences("MyCalendarApp", MODE_PRIVATE);
        loadEvents();
        loadTheme(); // Загрузка темы при старте приложения

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            selectedDate.setText(date);
            updateEventList(date);
        });

        addEventButton.setOnClickListener(v -> {
            String selectedDateString = selectedDate.getText().toString();
            String event = eventInput.getText().toString();
            if (!event.isEmpty() && !selectedDateString.isEmpty()) {
                addEvent(selectedDateString, event);
                eventInput.setText("");
            }
        });

        // Удаление выбранного события
        deleteEventButton.setOnClickListener(v -> {
            String selectedDateString = selectedDate.getText().toString();
            int selectedPosition = eventList.getCheckedItemPosition(); // Получаем позицию выбранного события
            if (selectedPosition >= 0) {
                String eventToRemove = events.get(selectedPosition); // Получаем событие по позиции
                removeEvent(selectedDateString, eventToRemove);
                eventList.clearChoices(); // Сбрасываем выбор
                adapter.notifyDataSetChanged(); // Обновляем список
            }
        });

        themeToggleButton.setOnClickListener(v -> toggleTheme());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void addEvent(String date, String event) {
        if (!eventsMap.containsKey(date)) {
            eventsMap.put(date, new ArrayList<>());
        }
        eventsMap.get(date).add(event);
        saveEvents();
        updateEventList(date);
    }

    private void removeEvent(String date, String event) {
        if (eventsMap.containsKey(date)) {
            eventsMap.get(date).remove(event);
            saveEvents();
            updateEventList(date);
        }
    }

    private void updateEventList(String date) {
        events.clear();
        if (eventsMap.containsKey(date)) {
            events.addAll(eventsMap.get(date));
        }
        adapter.notifyDataSetChanged();
    }

    private void loadEvents() {
        String json = sharedPreferences.getString("events", "{}");
        eventsMap = new Gson().fromJson(json, new TypeToken<HashMap<String, ArrayList<String>>>(){}.getType());
    }

    private void saveEvents() {
        String json = new Gson().toJson(eventsMap);
        sharedPreferences.edit().putString("events", json).apply();
    }

    private void loadTheme() {
        int themeMode = sharedPreferences.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            sharedPreferences.edit().putInt("themeMode", AppCompatDelegate.MODE_NIGHT_NO).apply();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            sharedPreferences.edit().putInt("themeMode", AppCompatDelegate.MODE_NIGHT_YES).apply();
        }
    }
}