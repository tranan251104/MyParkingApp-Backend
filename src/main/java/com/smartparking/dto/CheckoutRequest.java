package com.smartparking.dto;

public class CheckoutRequest {

    private String slotId;

    public CheckoutRequest() {
    }

    public CheckoutRequest(String slotId) {
        this.slotId = slotId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
}