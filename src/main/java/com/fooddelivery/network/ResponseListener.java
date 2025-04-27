package com.fooddelivery.network;

/**
 * Interface for handling asynchronous network responses
 * @param <T> The type of response data
 */
public interface ResponseListener<T> {
    
    /**
     * Called when a network request completes successfully
     * @param response The response data
     */
    void onSuccess(T response);
    
    /**
     * Called when a network request fails
     * @param error The error that occurred
     */
    void onError(Exception error);
    
    /**
     * Called when a network request starts (optional)
     * Default implementation does nothing
     */
    default void onStart() {
        // Optional method - can be implemented by classes that need this event
    }
    
    /**
     * Called when a network request completes (regardless of success or failure) (optional)
     * Default implementation does nothing
     */
    default void onComplete() {
        // Optional method - can be implemented by classes that need this event
    }
}