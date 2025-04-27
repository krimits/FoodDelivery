package com.fooddelivery.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.models.Product;

import java.util.List;

/**
 * Adapter for displaying Product items in a RecyclerView
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    /**
     * Creates a new ProductAdapter
     * @param productList List of products to display
     */
    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * ViewHolder for product items
     */
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvCategory;
        private TextView tvPrice;
        private TextView tvQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
        }

        /**
         * Binds product data to the view
         * @param product The product to display
         */
        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvCategory.setText(product.getCategory());
            tvPrice.setText(String.format("%.2f €", product.getPrice()));
            tvQuantity.setText(String.format("Available: %d", product.getQuantity()));
        }
    }
}