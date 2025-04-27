package com.fooddelivery.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Generic network task class that executes operations in a background thread
 * @param <T> The type of response data
 */
public class NetworkTask<T> {
    private static final String TAG = "NetworkTask";
    
    public interface TaskCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }

    private final NetworkOperation<T> operation;
    private final TaskCallback<T> callback;
    private final Handler mainHandler;

    /**
     * Functional interface for defining network operations
     * @param <T> The type of response data
     */
    public interface NetworkOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Creates a new NetworkTask
     * @param operation The network operation to execute
     * @param callback The callback to receive results or errors
     */
    public NetworkTask(NetworkOperation<T> operation, TaskCallback<T> callback) {
        this.operation = operation;
        this.callback = callback;
        // Handler attached to the main thread for UI updates
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Executes the network operation in a background thread
     */
    public void execute() {
        new Thread(() -> {
            try {
                // Execute the operation in the background thread
                final T result = operation.execute();
                
                // Deliver the result on the main thread
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (final Exception e) {
                Log.e(TAG, "Error executing network task: " + e.getMessage(), e);
                
                // Deliver the error on the main thread
                mainHandler.post(() -> callback.onError(e));
            }
        }).start();
    }
}