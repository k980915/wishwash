package com.example.wishwash;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView textLabel = findViewById(R.id.textLabel);
        TextView textConfidence = findViewById(R.id.textConfidence);

        String labelText = getIntent().getStringExtra("labelText");
        String confidenceText = getIntent().getStringExtra("confidenceText");

        textLabel.setText(getString(R.string.label, labelText));
        textConfidence.setText(getString(R.string.confidence, confidenceText));




        // Intent에서 결과 값을 가져옵니다.

    }
}