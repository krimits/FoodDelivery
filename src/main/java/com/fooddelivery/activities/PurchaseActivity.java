package com.fooddelivery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.adapters.ProductSelectionAdapter;
import com.fooddelivery.models.Product;
import com.fooddelivery.models.Purchase;
import com.fooddelivery.models.Store;
import com.fooddelivery.network.NetworkTask;
import com.fooddelivery.network.SocketClient;
import com.fooddelivery.utils.Constants;

import java.util.ArrayList;

/**
 * Activity for purchasing products from a store
 */
public class PurchaseActivity extends AppCompatActivity {

    private Store store;
    private TextView tvStoreName;
    private EditText etCustomerName;
    private EditText etCustomerEmail;
    private Button btnPurchase;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ProductSelectionAdapter productAdapter;
    private ArrayList<Product> productList = new ArrayList<>();
    private ArrayList<Product> selectedProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        // Get store from intent
        store = (Store) getIntent().getSerializableExtra(Constants.EXTRA_STORE);
        if (store == null) {
            Toast.makeText(this, "Error: Store details not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        tvStoreName = findViewById(R.id.tv_store_name);
        etCustomerName = findViewById(R.id.et_customer_name);
        etCustomerEmail = findViewById(R.id.et_customer_email);
        btnPurchase = findViewById(R.id.btn_purchase);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);

        // Set store name
        tvStoreName.setText(store.getStoreName());

        // Set up RecyclerView for product selection
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductSelectionAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        // Set click listener for purchase button
        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseProducts();
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
        btnPurchase.setEnabled(false);

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
                        btnPurchase.setEnabled(true);

                        productList.clear();
                        if (result != null && !result.isEmpty()) {
                            productList.addAll(result);
                            productAdapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(PurchaseActivity.this,
                                    "No products available for this store", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        // Handle error on main thread
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PurchaseActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        ).execute();
    }

    /**
     * Processes the purchase of selected products
     */
    private void purchaseProducts() {
        // Validate inputs
        String customerName = etCustomerName.getText().toString().trim();
        String customerEmail = etCustomerEmail.getText().toString().trim();

        if (customerName.isEmpty() || customerEmail.isEmpty()) {
            Toast.makeText(this, "Please enter your name and email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected products from adapter
        selectedProducts = productAdapter.getSelectedProducts();
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Please select at least one product", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create purchase object
        Purchase purchase = new Purchase(customerName, customerEmail, selectedProducts);

        // Show confirmation dialog
        showConfirmationDialog(purchase);
    }

    /**
     * Shows a confirmation dialog before submitting the purchase
     * @param purchase The purchase to confirm
     */
    private void showConfirmationDialog(final Purchase purchase) {
        // Calculate total price
        double totalPrice = 0;
        StringBuilder productsSummary = new StringBuilder();

        for (Product product : purchase.getPurchasedProducts()) {
            totalPrice += product.getPrice() * product.getQuantity();
            productsSummary.append("- ").append(product.getName())
                    .append(" (").append(product.getQuantity()).append("): ")
                    .append(String.format("%.2f €", product.getPrice() * product.getQuantity()))
                    .append("\n");
        }

        // Build confirmation message
        String message = "Order Summary:\n\n" +
                "Store: " + store.getStoreName() + "\n\n" +
                "Products:\n" + productsSummary.toString() + "\n" +
                "Total: " + String.format("%.2f €", totalPrice) + "\n\n" +
                "Proceed with purchase?";

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> submitPurchase(purchase))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Submits the purchase to the server
     * @param purchase The purchase to submit
     */
    private void submitPurchase(final Purchase purchase) {
        progressBar.setVisibility(View.VISIBLE);
        btnPurchase.setEnabled(false);

        new NetworkTask<String>(
                () -> {
                    // This runs in a background thread
                    SocketClient socketClient = new SocketClient(Constants.SERVER_IP, Constants.SERVER_PORT);
                    socketClient.connect();
                    try {
                        return socketClient.submitPurchase(purchase, store.getStoreName());
                    } finally {
                        socketClient.disconnect();
                    }
                },
                new NetworkTask.TaskCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        // Update UI on main thread
                        progressBar.setVisibility(View.GONE);

                        // Show result
                        new AlertDialog.Builder(PurchaseActivity.this)
                                .setTitle("Purchase Result")
                                .setMessage(result)
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void onError(Exception error) {
                        // Handle error on main thread
                        progressBar.setVisibility(View.GONE);
                        btnPurchase.setEnabled(true);
                        Toast.makeText(PurchaseActivity.this,
                                "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        ).execute();
    }
}