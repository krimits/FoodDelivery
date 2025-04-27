package com.fooddelivery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fooddelivery.R;
import com.fooddelivery.network.NetworkTask;
import com.fooddelivery.network.SocketClient;
import com.fooddelivery.utils.Constants;

/**
 * Activity for rating a store
 */
public class RateStoreActivity extends AppCompatActivity {

    private String storeName;
    private TextView tvStoreName;
    private EditText etStoreName;
    private RatingBar ratingBar;
    private Button btnSubmit;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_store);

        // Initialize UI elements
        tvStoreName = findViewById(R.id.tv_store_name);
        etStoreName = findViewById(R.id.et_store_name);
        ratingBar = findViewById(R.id.rating_bar);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);

        // Get store name from intent if available
        storeName = getIntent().getStringExtra(Constants.EXTRA_STORE_NAME);
        if (storeName != null && !storeName.isEmpty()) {
            tvStoreName.setVisibility(View.VISIBLE);
            tvStoreName.setText(storeName);
            etStoreName.setVisibility(View.GONE);
        } else {
            tvStoreName.setVisibility(View.GONE);
            etStoreName.setVisibility(View.VISIBLE);
        }

        // Set click listener for submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRating();
            }
        });
    }

    /**
     * Submits the store rating
     */
    private void submitRating() {
        // Get store name if not passed in intent
        if (storeName == null || storeName.isEmpty()) {
            storeName = etStoreName.getText().toString().trim();
            if (storeName.isEmpty()) {
                Toast.makeText(this, "Please enter the store name", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Get rating
        int rating = Math.round(ratingBar.getRating());
        if (rating < 1) {
            Toast.makeText(this, "Please select a rating (1-5 stars)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Rating")
                .setMessage("You are about to rate \"" + storeName + "\" with " + rating + " stars. Proceed?")
                .setPositiveButton("Yes", (dialog, which) -> sendRating(storeName, rating))
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Sends the rating to the server
     * @param storeName Name of the store to rate
     * @param rating Rating value (1-5)
     */
    private void sendRating(final String storeName, final int rating) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        new NetworkTask<String>(
                () -> {
                    // This runs in a background thread
                    SocketClient socketClient = new SocketClient(Constants.SERVER_IP, Constants.SERVER_PORT);
                    socketClient.connect();
                    try {
                        return socketClient.rateStore(storeName, rating);
                    } finally {
                        socketClient.disconnect();
                    }
                },
                new NetworkTask.TaskCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        // Update UI on main thread
                        progressBar.setVisibility(View.GONE);

                        // Show result and finish
                        new AlertDialog.Builder(RateStoreActivity.this)
                                .setTitle("Rating Submitted")
                                .setMessage(result)
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void onError(Exception error) {
                        // Handle error on main thread
                        progressBar.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(RateStoreActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ).execute();
    }
}