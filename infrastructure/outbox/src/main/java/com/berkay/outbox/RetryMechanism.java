package com.berkay.outbox;

public interface RetryMechanism<T> {
    void executeWithRetry(T item, Runnable action, Runnable onMaxRetriesExceeded);
}
