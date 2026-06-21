package com.smartparking.dto;

public class ReserveRequest {

    private String slotId;
    private String plate;

    public ReserveRequest() {
    }

    public ReserveRequest(String slotId, String plate) {
        this.slotId = slotId;
        this.plate = plate;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }
}