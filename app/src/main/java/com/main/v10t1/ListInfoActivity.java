package com.main.v10t1;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.TextView;

public class ListInfoActivity extends AppCompatActivity {

    private TextView yearText;
    private TextView carInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView cityText = findViewById(R.id.CityText);
        yearText = findViewById(R.id.YearText);
        carInfoText = findViewById(R.id.CarInfoText);

        CarDataStorage storage = CarDataStorage.getInstance();
        cityText.setText(storage.getCity());
        yearText.setText(String.valueOf(storage.getYear()));

        StringBuilder info = new StringBuilder();
        int total = 0;
        for (CarData data : storage.getCarData()) {
            info.append(data.getType()).append(": ").append(data.getAmount()).append("\n");
            total += data.getAmount();
        }
        info.append("\nYhteensä: ").append(total);

        carInfoText.setText(info.toString());
    }
}