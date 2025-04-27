package com.fooddelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.adapters.ProductAdapter;
import com.fooddelivery.models.Product;
import com.fooddelivery.models.Store;
import com.fooddelivery.network.NetworkTask;
import com.fooddelivery.network.SocketClient;
import com.fooddelivery.utils.Constants;

import java.util.ArrayList;

/**
 * Activity for displaying store details and products
 */
public class StoreDetailsActivity extends AppCompatActivity {

    private Store store;
    private TextView tvStoreName;
    private TextView tvCategory;
    private TextView tvStars;
    private TextView tvPriceCategory;
    private Button btnPurchase;
    private Button btnRate;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private final ArrayList<Product> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_details);

        // Get store from intent
        store = (Store) getIntent().getSerializableExtra(Constants.EXTRA_STORE);
        if (store == null) {
            Toast.makeText(this, "Error: Store details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        tvStoreName = findViewById(R.id.tv_store_name);
        tvCategory = findViewById(R.id.tv_category);
        tvStars = findViewById(R.id.tv_stars);
        tvPriceCategory = findViewById(R.id.tv_price_category);
        btnPurchase = findViewById(R.id.btn_purchase);
        btnRate = findViewById(R.id.btn_rate);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);

        // Set store information
        tvStoreName.setText(store.getStoreName());
        tvCategory.setText(store.getCategory());
        tvStars.setText(String.format("%.1f ★ (%d reviews)", store.getStars(), store.getNoOfReviews()));
        tvPriceCategory.setText(store.calculatePriceCategory());

        // Set up RecyclerView for products
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        // Set click listeners
        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productList.isEmpty()) {
                    Toast.makeText(StoreDetailsActivity.this,
                            "No products available for purchase", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(StoreDetailsActivity.this, PurchaseActivity.class);
                    intent.putExtra(Constants.EXTRA_STORE, store);
                    startActivity(intent);
                }
            }
        });

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreDetailsActivity.this, RateStoreActivity.class);
                intent.putExtra(Constants.EXTRA_STORE_NAME, store.getStoreName());
                startActivity(intent);
            }
        });

        // Fetch products for the store
        fetchStoreProducts();
    }

    /**
     * Fetches products for the selected store
     */
    private void fetchStoreProducts() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        new NetworkTask<ArrayList<Product>>(
                () -> {
                    // This runs in a background thread
                    SocketClient socketClient = new SocketClient(Constants.SERVER_IP, Constants.SERVER_PORT);
                    socketClient.connect();
                    try {
                        return socketClient.getStoreProducts(store.getStoreName());
                    } finally {
                        socketClient.disconnect();
                    }
                },
                new NetworkTask.TaskCallback<ArrayList<Product>>() {
                    @Override
                    public void onSuccess(ArrayList<Product> result) {
                        // Update UI on main thread
                        progressBar.setVisibility(View.GONE);

                        productList.clear();
                        if (result != null && !result.isEmpty()) {
                            productList.addAll(result);
                            productAdapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(StoreDetailsActivity.this,
                                    "No products available for this store", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        // Handle error on main thread
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(StoreDetailsActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ).execute();
    }
}