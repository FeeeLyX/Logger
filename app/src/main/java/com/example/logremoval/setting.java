package com.example.logremoval;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        TextView text1 = findViewById(R.id.textView_set);
        text1.setText("ха,наебал,ты все равно гей");
    }
}
