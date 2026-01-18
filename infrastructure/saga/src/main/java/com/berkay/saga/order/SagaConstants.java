package com.berkay.saga.order;

public final class SagaConstants {

    private SagaConstants(){}

    // Bu type sayesinde outbox tablosundan henüz işlenmemiş mesajlar arasından özellikle Order süreciyle alakalı olan mesajlar alınabilir.
    public static final String ORDER_SAGA_NAME = "OrderProcessingSaga";
}
