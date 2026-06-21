package com.smartparking.model;

public class Slot {

    private String slotId;
    private boolean occupied;
    private String plate;

    private boolean reserved;
    private String reservedPlate;

    private double lat;
    private double lng;

    public Slot() {
    }

    public Slot(String slotId, boolean occupied, String plate) {
        this.slotId = slotId;
        this.occupied = occupied;
        this.plate = plate;
        this.reserved = false;
        this.reservedPlate = null;
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

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public String getReservedPlate() {
        return reservedPlate;
    }

    public void setReservedPlate(String reservedPlate) {
        this.reservedPlate = reservedPlate;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}