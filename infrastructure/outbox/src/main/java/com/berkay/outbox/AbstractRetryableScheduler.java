package com.berkay.outbox;

/**
 * DB-Agnostic base scheduler to process outbox messages with retry logic.
 */
public abstract class AbstractRetryableScheduler implements OutboxScheduler {

    /**
     * Implementing class is expected to use a mechanism like 'SKIP LOCKED' 
     * in its repository layer to fetch records concurrently.
     */
    protected abstract void fetchAndProcessRecords();

    @Override
    public void processOutboxMessage() {
        try {
            fetchAndProcessRecords();
        } catch (Exception e) {
            // Unhandled exception caught by the scheduler boundary. 
            // Retries will happen automatically in the next scheduled execution.
            handleError(e);
        }
    }

    protected abstract void handleError(Exception e);
}
