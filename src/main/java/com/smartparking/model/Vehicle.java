package com.smartparking.model;

public class Vehicle {

    private String plate;
    private long checkInTime;
    private Long checkOutTime;
    private String slot;
    private Long amount;

    public Vehicle() {}

    public Vehicle(String plate, long checkInTime, String slot) {
        this.plate = plate;
        this.checkInTime = checkInTime;
        this.slot = slot;
    }

    // ✅ GETTERS
    public String getPlate() {
        return plate;
    }

    public long getCheckInTime() {
        return checkInTime;
    }

    public Long getCheckOutTime() {
        return checkOutTime;
    }

    public String getSlot() {
        return slot;
    }
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public void setCheckInTime(long checkInTime) {
        this.checkInTime = checkInTime;
    }

    public void setCheckOutTime(Long checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }
}