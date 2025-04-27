package com.fooddelivery.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for selecting products to purchase
 */
public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.ProductViewHolder> {

    private List<Product> productList;
    private List<Integer> quantities;

    /**
     * Creates a new ProductSelectionAdapter
     * @param productList List of available products
     */
    public ProductSelectionAdapter(List<Product> productList) {
        this.productList = productList;
        this.quantities = new ArrayList<>(productList.size());

        // Initialize quantities to 0
        for (int i = 0; i < productList.size(); i++) {
            quantities.add(0);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_selection, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, quantities.get(position), position);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * Gets the list of selected products with quantities > 0
     * @return List of selected products
     */
    public ArrayList<Product> getSelectedProducts() {
        ArrayList<Product> selectedProducts = new ArrayList<>();

        for (int i = 0; i < productList.size(); i++) {
            int quantity = quantities.get(i);
            if (quantity > 0) {
                // Create a new product with the selected quantity
                Product original = productList.get(i);
                Product selected = new Product(
                        original.getName(),
                        original.getCategory(),
                        quantity,
                        original.getPrice()
                );
                selectedProducts.add(selected);
            }
        }

        return selectedProducts;
    }

    /**
     * ViewHolder for product selection items
     */
    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvCategory;
        private TextView tvPrice;
        private TextView tvAvailable;
        private EditText etQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAvailable = itemView.findViewById(R.id.tv_available);
            etQuantity = itemView.findViewById(R.id.et_quantity);
        }

        /**
         * Binds product data to the view
         * @param product The product to display
         * @param quantity Current selected quantity
         * @param position Position in the adapter
         */
        public void bind(Product product, int quantity, final int position) {
            tvProductName.setText(product.getName());
            tvCategory.setText(product.getCategory());
            tvPrice.setText(String.format("%.2f €", product.getPrice()));
            tvAvailable.setText(String.format("Available: %d", product.getQuantity()));

            // Set current quantity
            etQuantity.setText(String.valueOf(quantity));

            // Set text change listener to update quantity
            etQuantity.removeTextChangedListener(textWatcher); // Remove to avoid recursive calls
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        int newQuantity = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());

                        // Validate that quantity is not more than available
                        if (newQuantity > product.getQuantity()) {
                            newQuantity = product.getQuantity();
                            etQuantity.setText(String.valueOf(newQuantity));
                            etQuantity.setSelection(etQuantity.getText().length());
                        }

                        quantities.set(position, newQuantity);
                    } catch (NumberFormatException e) {
                        etQuantity.setText("0");
                        quantities.set(position, 0);
                    }
                }
            };
            etQuantity.addTextChangedListener(textWatcher);
        }

        private TextWatcher textWatcher;
    }
}