package com.smartparking.dto;

public class SlotSensorRequest {

    private String slotId;
    private boolean occupied;

    public SlotSensorRequest() {
    }

    public SlotSensorRequest(String slotId, boolean occupied) {
        this.slotId = slotId;
        this.occupied = occupied;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}