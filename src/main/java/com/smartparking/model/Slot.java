package com.smartparking.model;

public class Slot {
    private String slotId;
    private boolean occupied;
    private String plate;



    public Slot(String slotId, boolean occupied, String plate) {
        this.slotId = slotId;
        this.occupied = occupied;
        this.plate = plate;
    }

    public String getSlotId() {
        return slotId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getPlate() {
        return plate;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }
}