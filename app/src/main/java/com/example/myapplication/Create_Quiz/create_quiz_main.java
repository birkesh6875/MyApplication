package com.example.myapplication.Create_Quiz;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.util.Objects;
import com.example.myapplication.R;

public class create_quiz_main extends AppCompatActivity {
    private Button addButton;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_1);

        toolbar = findViewById(R.id.toolbartst);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Инициализируем кнопку "Add"
        addButton = findViewById(R.id.add_button);

        // Устанавливаем обработчик нажатия на кнопку
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Запускаем активность создания теста
                Intent intent = new Intent(create_quiz_main.this, Custom_quiz.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
