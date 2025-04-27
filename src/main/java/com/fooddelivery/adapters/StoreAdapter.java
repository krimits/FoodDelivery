package com.fooddelivery.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fooddelivery.R;
import com.fooddelivery.models.Store;

import java.util.List;

/**
 * Adapter for displaying Store items in a RecyclerView
 */
public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final List<Store> storeList;
    private final OnStoreClickListener listener;

    /**
     * Interface for store item click events
     */
    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    /**
     * Creates a new StoreAdapter
     * @param storeList List of stores to display
     * @param listener Click listener for store items
     */
    public StoreAdapter(List<Store> storeList, OnStoreClickListener listener) {
        this.storeList = storeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = storeList.get(position);
        holder.bind(store, listener);
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    /**
     * ViewHolder for store items
     */
    static class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStoreName;
        private final TextView tvCategory;
        private final TextView tvStars;
        private final TextView tvPriceCategory;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tv_store_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvStars = itemView.findViewById(R.id.tv_stars);
            tvPriceCategory = itemView.findViewById(R.id.tv_price_category);
        }

        /**
         * Binds store data to the view
         * @param store The store to display
         * @param listener Click listener for the store item
         */
        @SuppressLint("DefaultLocale")
        public void bind(final Store store, final OnStoreClickListener listener) {
            tvStoreName.setText(store.getStoreName());
            tvCategory.setText(store.getCategory());
            tvStars.setText(String.format("%.1f ★ (%d reviews)", store.getStars(), store.getNoOfReviews()));
            tvPriceCategory.setText(store.calculatePriceCategory());

            // Set click listener for the entire view
            itemView.setOnClickListener(v -> listener.onStoreClick(store));
        }
    }
}