package com.smartparking.model;

public class Vehicle {

    private String plate;
    private Long checkInTime;
    private Long checkOutTime;
    private String slotId;
    private Long amount;

    public Vehicle() {
    }

    public Vehicle(String plate, Long checkInTime, String slotId) {
        this.plate = plate;
        this.checkInTime = checkInTime;
        this.slotId = slotId;
        this.checkOutTime = null;
        this.amount = 0L;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Long getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Long checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Long getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Long checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}