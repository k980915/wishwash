package com.example.wishwash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;


@ExperimentalGetImage
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton bt_1 = findViewById(R.id.bt_1);
        bt_1.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            startActivity(intent);
        });

        ImageButton bt_2 = findViewById(R.id.bt_2);
        bt_2.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BasketActivity.class);
            startActivity(intent);
        });


        ImageButton bt_4 = findViewById(R.id.bt_4);
        bt_4.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
            startActivity(intent);
        });

        ImageButton bt_5 = findViewById(R.id.bt_5);
        bt_5.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivity(intent);
        });
    }
}