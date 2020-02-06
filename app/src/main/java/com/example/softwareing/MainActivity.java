package com.example.softwareing;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button task1;
    private Button task2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        task1 = findViewById(R.id.task1);
        task2 = findViewById(R.id.task2);
        task1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTask1();
            }
        });
        task2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTask2();
            }
        });
    }

    private void openTask1(){
        Intent intent = new Intent(this, Task1.class);
        startActivity(intent);
    }

    private void openTask2(){
        Intent intent = new Intent(this, Task2.class);
        startActivity(intent);
    }

}
