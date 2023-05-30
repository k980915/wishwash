package com.example.wishwash;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;



public class BasketActivity extends AppCompatActivity {

    ImageButton bk_1;
    ImageButton bk_2;
    ImageButton bk_3;
    ImageButton bk_4;
    ImageButton bk_5;
    ImageButton bt_3_1;
    ImageButton bt_4_1;
    ImageButton bt_5_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);

        ImageButton bk_1 = (ImageButton) findViewById(R.id.bk_1);
        bk_1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
                startActivity(intent);
            }
        });

        ImageButton bk_2 = (ImageButton) findViewById(R.id.bk_2);
        bk_2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DialogActivity_2.class);
                startActivity(intent);
            }
        });

        ImageButton bk_3 = (ImageButton) findViewById(R.id.bk_3);
        bk_3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DialogActivity_3.class);
                startActivity(intent);
            }
        });

        ImageButton bk_4 = (ImageButton) findViewById(R.id.bk_4);
        bk_4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DialogActivity_4.class);
                startActivity(intent);
            }
        });

        ImageButton bk_5 = (ImageButton) findViewById(R.id.bk_5);
        bk_5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DialogActivity_5.class);
                startActivity(intent);
            }
        });

        ImageButton bt_3_1 = (ImageButton) findViewById(R.id.bt_3_1);
        bt_3_1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });

        ImageButton bt_4_1 = (ImageButton) findViewById(R.id.bt_4_1);
        bt_4_1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                startActivity(intent);
            }
        });

        ImageButton bt_5_1 = (ImageButton) findViewById(R.id.bt_5_1);
        bt_5_1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            }
        });
    }
}