package com.fooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.adapters.StoreAdapter;
import com.fooddelivery.models.Store;
import com.fooddelivery.network.NetworkTask;
import com.fooddelivery.network.SocketClient;
import com.fooddelivery.utils.Constants;

import java.util.ArrayList;

/**
 * Activity for finding and displaying nearby stores
 */
public class NearbyStoresActivity extends AppCompatActivity {
    private static final String TAG = "NearbyStoresActivity";

    private EditText etLatitude;
    private EditText etLongitude;
    private Button btnSearch;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private RecyclerView recyclerView;
    private StoreAdapter storeAdapter;
    private ArrayList<Store> storeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_stores);

        // Initialize UI elements
        etLatitude = findViewById(R.id.et_latitude);
        etLongitude = findViewById(R.id.et_longitude);
        btnSearch = findViewById(R.id.btn_search);
        progressBar = findViewById(R.id.progress_bar);
        tvNoResults = findViewById(R.id.tv_no_results);
        recyclerView = findViewById(R.id.recycler_view);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        storeAdapter = new StoreAdapter(storeList, store -> {
            // On store item click, open store details activity
            Intent intent = new Intent(NearbyStoresActivity.this, StoreDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_STORE, store);
            startActivity(intent);
        });
        recyclerView.setAdapter(storeAdapter);

        // Set click listener for search button
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchNearbyStores();
            }
        });

        // Βεβαιωθείτε ότι τα views έχουν τη σωστή αρχική κατάσταση
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
    }

    /**
     * Searches for nearby stores based on latitude and longitude input
     */
    private void searchNearbyStores() {
        // Validate inputs
        if (etLatitude.getText().toString().isEmpty() || etLongitude.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter both latitude and longitude", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double latitude = Double.parseDouble(etLatitude.getText().toString());
            double longitude = Double.parseDouble(etLongitude.getText().toString());

            Log.d(TAG, "Searching for stores at lat: " + latitude + ", lng: " + longitude);

            // Show progress bar
            progressBar.setVisibility(View.VISIBLE);
            btnSearch.setEnabled(false);
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);

            // Execute network task asynchronously
            new NetworkTask<ArrayList<Store>>(
                    () -> {
                        // This runs in a background thread
                        SocketClient socketClient = new SocketClient(Constants.SERVER_IP, Constants.SERVER_PORT);
                        socketClient.connect();
                        try {
                            return socketClient.getNearbyStores(latitude, longitude);
                        } finally {
                            socketClient.disconnect();
                        }
                    },
                    new NetworkTask.TaskCallback<ArrayList<Store>>() {
                        @Override
                        public void onSuccess(ArrayList<Store> result) {
                            // Update UI on main thread
                            progressBar.setVisibility(View.GONE);
                            btnSearch.setEnabled(true);

                            // Εκτενής καταγραφή της ανταπόκρισης
                            Log.d(TAG, "Response received. Stores: " +
                                    (result != null ? result.size() : "null"));

                            storeList.clear();

                            if (result != null && !result.isEmpty()) {
                                for (Store store : result) {
                                    Log.d(TAG, "Store received: " + store.getStoreName() +
                                            ", Category: " + store.getCategory());
                                }

                                storeList.addAll(result);

                                // Επαναφορά του adapter αντί για απλή ενημέρωση
                                storeAdapter = new StoreAdapter(storeList, store -> {
                                    Intent intent = new Intent(NearbyStoresActivity.this, StoreDetailsActivity.class);
                                    intent.putExtra(Constants.EXTRA_STORE, store);
                                    startActivity(intent);
                                });
                                recyclerView.setAdapter(storeAdapter);

                                Log.d(TAG, "Updated adapter with " + storeList.size() + " stores");

                                // Εξασφαλίστε ότι το RecyclerView είναι ορατό
                                tvNoResults.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                Log.d(TAG, "No stores found or empty result");
                                recyclerView.setVisibility(View.GONE);
                                tvNoResults.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            // Handle error on main thread
                            Log.e(TAG, "Error during network request", error);
                            progressBar.setVisibility(View.GONE);
                            btnSearch.setEnabled(true);
                            tvNoResults.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);

                            Toast.makeText(NearbyStoresActivity.this,
                                    "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            ).execute();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid latitude/longitude format", e);
        }
    }
}