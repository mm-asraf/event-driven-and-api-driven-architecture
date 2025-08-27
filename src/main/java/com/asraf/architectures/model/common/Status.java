package com.asraf.architectures.model.common;

import lombok.Getter;

@Getter
public enum Status{
    CREATED, PAYMENT_PROCESSED, SHIPPED, DELIVERED,CANCELLED,INVENTORY_RESERVED,PAYMENT_CONFIRMED,PREPARING_SHIPMENT,
    PAYMENT_FAILED
}
