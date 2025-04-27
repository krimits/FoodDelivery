package com.fooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.adapters.StoreAdapter;
import com.fooddelivery.models.MapReduceRequest;
import com.fooddelivery.models.Store;
import com.fooddelivery.network.NetworkTask;
import com.fooddelivery.network.SocketClient;
import com.fooddelivery.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Activity for filtering stores based on criteria
 */
public class FilterStoresActivity extends AppCompatActivity {

    private EditText etLatitude;
    private EditText etLongitude;
    private EditText etFoodCategories;
    private SeekBar sbMinStars;
    private TextView tvMinStars;
    private RadioGroup rgPriceCategory;
    private Button btnFilter;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private RecyclerView recyclerView;
    private StoreAdapter storeAdapter;
    private ArrayList<Store> storeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_stores);

        // Initialize UI elements
        etLatitude = findViewById(R.id.et_latitude);
        etLongitude = findViewById(R.id.et_longitude);
        etFoodCategories = findViewById(R.id.et_food_categories);
        sbMinStars = findViewById(R.id.sb_min_stars);
        tvMinStars = findViewById(R.id.tv_min_stars);
        rgPriceCategory = findViewById(R.id.rg_price_category);
        btnFilter = findViewById(R.id.btn_filter);
        progressBar = findViewById(R.id.progress_bar);
        tvNoResults = findViewById(R.id.tv_no_results);
        recyclerView = findViewById(R.id.recycler_view);

        // Set up min stars seekbar
        sbMinStars.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float stars = progress / 10.0f;
                tvMinStars.setText(String.format("Minimum Stars: %.1f", stars));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storeAdapter = new StoreAdapter(storeList, store -> {
            // On store item click, open store details activity
            Intent intent = new Intent(FilterStoresActivity.this, StoreDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_STORE, store);
            startActivity(intent);
        });
        recyclerView.setAdapter(storeAdapter);

        // Set click listener for filter button
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterStores();
            }
        });
    }

    /**
     * Filters stores based on user criteria
     */
    private void filterStores() {
        // Validate location inputs
        if (etLatitude.getText().toString().isEmpty() || etLongitude.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter both latitude and longitude", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get location
            double latitude = Double.parseDouble(etLatitude.getText().toString());
            double longitude = Double.parseDouble(etLongitude.getText().toString());

            // Get food categories
            String categoriesStr = etFoodCategories.getText().toString().trim();
            ArrayList<String> categories = new ArrayList<>();
            if (!categoriesStr.isEmpty()) {
                categories = new ArrayList<>(Arrays.asList(categoriesStr.split("\\s*,\\s*")));
            }

            // Get minimum stars
            float minStars = sbMinStars.getProgress() / 10.0f;

            // Get price category
            String priceCategory = "";
            int priceCategoryId = rgPriceCategory.getCheckedRadioButtonId();
            if (priceCategoryId != -1) {
                RadioButton selectedRadioButton = findViewById(priceCategoryId);
                priceCategory = selectedRadioButton.getText().toString();
            }

            // Create filter request
            MapReduceRequest request = new MapReduceRequest(
                    latitude,
                    longitude,
                    categories,
                    minStars,
                    priceCategory,
                    5.0 // 5km radius
            );

            // Show progress
            progressBar.setVisibility(View.VISIBLE);
            btnFilter.setEnabled(false);
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);

            // Execute network task asynchronously
            new NetworkTask<ArrayList<Store>>(
                    () -> {
                        // This runs in a background thread
                        SocketClient socketClient = new SocketClient(Constants.SERVER_IP, Constants.SERVER_PORT);
                        socketClient.connect();
                        try {
                            return socketClient.getFilteredStores(request);
                        } finally {
                            socketClient.disconnect();
                        }
                    },
                    new NetworkTask.TaskCallback<ArrayList<Store>>() {
                        @Override
                        public void onSuccess(ArrayList<Store> result) {
                            // Update UI on main thread
                            progressBar.setVisibility(View.GONE);
                            btnFilter.setEnabled(true);

                            storeList.clear();
                            if (result != null && !result.isEmpty()) {
                                storeList.addAll(result);
                                storeAdapter.notifyDataSetChanged();
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                tvNoResults.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            // Handle error on main thread
                            progressBar.setVisibility(View.GONE);
                            btnFilter.setEnabled(true);
                            Toast.makeText(FilterStoresActivity.this,
                                    "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            ).execute();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
        }
    }
}