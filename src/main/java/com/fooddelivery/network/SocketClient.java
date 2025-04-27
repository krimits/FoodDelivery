package com.fooddelivery.network;

import android.util.Log;

import com.fooddelivery.models.MapReduceRequest;
import com.fooddelivery.models.Product;
import com.fooddelivery.models.Purchase;
import com.fooddelivery.models.Store;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class for handling TCP socket connections to the Master server
 */
public class SocketClient {
    private static final String TAG = "SocketClient";

    private final String serverIp;
    private final int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public SocketClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * Connects to the Master server
     * @throws IOException if there's an error connecting
     */
    public void connect() throws IOException {
        socket = new Socket(serverIp, serverPort);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        Log.d(TAG, "Connected to server: " + serverIp + ":" + serverPort);
    }

    /**
     * Disconnects from the Master server
     */
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            Log.d(TAG, "Disconnected from server");
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage());
        }
    }

    /**
     * Gets nearby stores within 5km based on user's location
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @return List of nearby stores
     * @throws Exception if there's an error communicating with the server
     */
    public ArrayList<Store> getNearbyStores(double latitude, double longitude) throws Exception {
        // Λεπτομερές logging για αποσφαλμάτωση
        Log.d(TAG, "Sending nearby stores request for lat:" + latitude + ", lng:" + longitude);

        // Create default request with 5km radius
        MapReduceRequest request = new MapReduceRequest(
                latitude,
                longitude,
                new ArrayList<>(), // No category filter
                0,                  // No minimum stars
                "",                 // No price filter
                5.0                 // 5km radius
        );

        // Send request to server
        out.writeObject("client");
        out.flush();
        Log.d(TAG, "Sent 'client' command to server");

        out.writeObject(request);
        out.flush();
        Log.d(TAG, "Sent MapReduceRequest to server");

        // Receive response from server
        Log.d(TAG, "Waiting for server response...");
        @SuppressWarnings("unchecked")
        ArrayList<Store> stores = (ArrayList<Store>) in.readObject();

        // Έλεγχος και καταγραφή απάντησης
        if (stores == null) {
            Log.d(TAG, "Received null store list from server");
        } else {
            Log.d(TAG, "Received " + stores.size() + " stores from server");
            for (Store store : stores) {
                Log.d(TAG, "Store: " + store.getStoreName() + ", Category: " +
                        store.getCategory() + ", Stars: " + store.getStars());
            }
        }

        return stores;
    }

    /**
     * Gets filtered stores based on user criteria
     * @param request The MapReduceRequest containing filter criteria
     * @return List of filtered stores
     * @throws Exception if there's an error communicating with the server
     */
    public ArrayList<Store> getFilteredStores(MapReduceRequest request) throws Exception {
        Log.d(TAG, "Sending filtered stores request");

        // Send request to server
        out.writeObject("filter");
        out.flush();

        out.writeObject(request);
        out.flush();

        // Receive response from server
        @SuppressWarnings("unchecked")
        ArrayList<Store> stores = (ArrayList<Store>) in.readObject();

        // Logging για αποσφαλμάτωση
        if (stores == null) {
            Log.d(TAG, "Received null filtered store list");
        } else {
            Log.d(TAG, "Received " + stores.size() + " filtered stores");
        }

        return stores;
    }

    /**
     * Fetches products from a specific store
     * @param storeName Name of the store
     * @return List of products from the store
     * @throws Exception if there's an error communicating with the server
     */
    public ArrayList<Product> getStoreProducts(String storeName) throws Exception {
        Log.d(TAG, "Fetching products for store: " + storeName);

        // Send request to server
        out.writeObject("fetchProducts");
        out.flush();

        out.writeObject(storeName);
        out.flush();

        // Receive response from server
        @SuppressWarnings("unchecked")
        ArrayList<Product> products = (ArrayList<Product>) in.readObject();

        Log.d(TAG, "Received " + (products != null ? products.size() : "null") + " products");

        return products;
    }

    /**
     * Submits a purchase request
     * @param purchase The Purchase object with customer and product details
     * @param storeName Name of the store to purchase from
     * @return Response message from the server
     * @throws Exception if there's an error communicating with the server
     */
    public String submitPurchase(Purchase purchase, String storeName) throws Exception {
        Log.d(TAG, "Submitting purchase for store: " + storeName);

        // Send request to server
        out.writeObject("purchase");
        out.flush();

        out.writeObject(purchase);
        out.flush();

        out.writeObject(storeName);
        out.flush();

        // Receive response from server
        String response = (String) in.readObject();
        Log.d(TAG, "Purchase response: " + response);

        return response;
    }

    /**
     * Submits a store rating
     * @param storeName Name of the store to rate
     * @param rating Rating value (1-5)
     * @return Response message from the server
     * @throws Exception if there's an error communicating with the server
     */
    public String rateStore(String storeName, int rating) throws Exception {
        Log.d(TAG, "Rating store: " + storeName + " with " + rating + " stars");

        // Send request to server
        out.writeObject("rate");
        out.flush();

        out.writeObject(storeName);
        out.flush();

        out.writeObject(rating);
        out.flush();

        // Receive response from server
        String response = (String) in.readObject();
        Log.d(TAG, "Rating response: " + response);

        return response;
    }
}