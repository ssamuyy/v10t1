package com.main.v10t1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SearchActivity extends AppCompatActivity {

    private EditText cityNameEdit, yearEdit;
    private TextView statusText;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cityNameEdit = findViewById(R.id.CityNameEdit);
        yearEdit = findViewById(R.id.YearEdit);
        statusText = findViewById(R.id.StatusText);
    }

    public void searchData(View view) {
        String city = cityNameEdit.getText().toString().trim();
        String yearStr = yearEdit.getText().toString().trim();

        if (city.isEmpty() || !yearStr.matches("\\d{4}")) {
            statusText.setText("Haku epäonnistui, syötä kelvollinen kaupunki ja vuosiluku");
            return;
        }

        int year = Integer.parseInt(yearStr);
        statusText.setText("Haetaan...");

        executor.execute(() -> getData(this, city, year));
    }

    public void openListInfoActivity(View view) {
        Intent intent = new Intent(this, ListInfoActivity.class);
        startActivity(intent);
    }

    public void getData(Context context, String city, int year) {
        try {
            String apiUrl = "https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/mkan/statfin_mkan_pxt_11ic.px";
            HttpURLConnection metaConn = (HttpURLConnection) new URL(apiUrl).openConnection();
            metaConn.setRequestMethod("GET");
            BufferedReader metaReader = new BufferedReader(new InputStreamReader(metaConn.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode metadata = mapper.readTree(metaReader);
            metaReader.close();

            // Haetaan aluenimi ja sen koodi
            String areaCode = null;
            for (JsonNode node : metadata.get("variables")) {
                if ("Alue".equals(node.get("code").asText())) {
                    JsonNode values = node.get("values");
                    JsonNode valueTexts = node.get("valueTexts");
                    for (int i = 0; i < valueTexts.size(); i++) {
                        if (valueTexts.get(i).asText().equalsIgnoreCase(city)) {
                            areaCode = values.get(i).asText();
                            break;
                        }
                    }
                }
            }

            if (areaCode == null) {
                runOnUiThread(() -> statusText.setText("Haku epäonnistui, kaupunkia ei löytynyt"));
                return;
            }

            // JSON pyyntö
            JsonNode query = mapper.readTree(context.getResources().openRawResource(R.raw.query));
            ((ObjectNode) query.get("query").get(0).get("selection")).putArray("values").add(areaCode);
            ((ObjectNode) query.get("query").get(3).get("selection")).putArray("values").add(String.valueOf(year));

            // Lähettää POST pyynnön
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream os = conn.getOutputStream();
            mapper.writeValue(os, query);
            os.flush();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonNode response = mapper.readTree(reader);
            reader.close();

            JsonNode values = response.get("value");
            if (values == null || values.size() != 5) {
                runOnUiThread(() -> statusText.setText("Haku epäonnistui, virheellinen vastaus"));
                return;
            }

            CarDataStorage storage = CarDataStorage.getInstance();
            storage.clearData();
            storage.setCity(city);
            storage.setYear(year);
            String[] types = {"Henkilöautot", "Pakettiautot", "Kuorma-autot", "Linja-autot", "Erikoisautot"};
            for (int i = 0; i < types.length; i++) {
                storage.addCarData(new CarData(types[i], values.get(i).asInt()));
            }

            runOnUiThread(() -> statusText.setText("Haku onnistui"));

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> statusText.setText("Haku epäonnistui: " + e.getMessage()));
        }
    }
}