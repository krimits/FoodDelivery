package com.fooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.fooddelivery.R;

/**
 * Main entry point of the application
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        Button btnNearbyStores = findViewById(R.id.btn_nearby_stores);
        Button btnFilterStores = findViewById(R.id.btn_filter_stores);
        Button btnRateStore = findViewById(R.id.btn_rate_store);

        // Set click listeners
        btnNearbyStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NearbyStoresActivity.class));
            }
        });

        btnFilterStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FilterStoresActivity.class));
            }
        });

        btnRateStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RateStoreActivity.class));
            }
        });
    }
}