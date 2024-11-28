package com.example.eternalwayfinder;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomePage extends AppCompatActivity {

    private Button buttonEnglish, buttonTagalog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        buttonEnglish = findViewById(R.id.buttonEnglish);
        buttonTagalog = findViewById(R.id.buttonTagalog);

        buttonEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MainActivity.class);
                intent.putExtra("language", "en"); // Pass English as the language
                startActivity(intent);
            }
        });

        buttonTagalog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MainActivity.class);
                intent.putExtra("language", "tl"); // Pass Tagalog as the language
                startActivity(intent);
            }
        });
    }
}
