package com.berkay.outbox;

public interface OutboxScheduler {

    void processOutboxMessage();
}
