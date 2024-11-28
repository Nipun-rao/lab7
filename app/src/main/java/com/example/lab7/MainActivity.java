package com.example.lab7;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private String apiKey = "7e943c97096a9784391a981c4d878b22";
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        Button buttonGetForecast = findViewById(R.id.buttonGetForecast);
        buttonGetForecast.setOnClickListener(this::onClick);
    }

    private void fetchWeatherData(String url) {
        ProgressBar progressBar = findViewById(R.id.progressBarLoading);
        progressBar.setVisibility(View.VISIBLE);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        // Parsing JSON response
                        JSONObject main = response.getJSONObject("main");
                        double currentTemp = main.getDouble("temp");
                        double maxTemp = main.getDouble("temp_max");
                        double minTemp = main.getDouble("temp_min");
                        int humidity = main.getInt("humidity");

                        JSONArray weatherArray = response.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String description = weather.getString("description");
                        String icon = weather.getString("icon");

                        // Update UI
                        updateUI(currentTemp, maxTemp, minTemp, humidity, description, icon);
                    } catch (JSONException e) {
                        Toast.makeText(this, "JSON Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private void updateUI(double currentTemp, double maxTemp, double minTemp, int humidity, String description, String icon) {
        ((TextView) findViewById(R.id.textCurrentTemp)).setText("Current Temp: " + currentTemp + "°C");
        ((TextView) findViewById(R.id.textMaxTemp)).setText("Max Temp: " + maxTemp + "°C");
        ((TextView) findViewById(R.id.textMinTemp)).setText("Min Temp: " + minTemp + "°C");
        ((TextView) findViewById(R.id.textHumidity)).setText("Humidity: " + humidity + "%");
        ((TextView) findViewById(R.id.textDescription)).setText("Description: " + description);

        // Fetch and display the weather icon
        String iconUrl = "https://openweathermap.org/img/w/" + icon + ".png";
        ImageRequest imageRequest = new ImageRequest(iconUrl, bitmap -> {
            ImageView iconView = findViewById(R.id.imageWeatherIcon);
            iconView.setImageBitmap(bitmap);
        }, 0, 0, ImageView.ScaleType.CENTER, null,
                error -> Toast.makeText(this, "Failed to load icon", Toast.LENGTH_SHORT).show());

        queue.add(imageRequest);
    }

    private void onClick(View view) {
        String cityName = ((EditText) findViewById(R.id.editTextCity)).getText().toString();
        try {
            cityName = URLEncoder.encode(cityName, "UTF-8");
            String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName +
                    "&appid=" + apiKey + "&units=metric";
            fetchWeatherData(url);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "Error encoding city name", Toast.LENGTH_SHORT).show();
        }
    }
}
