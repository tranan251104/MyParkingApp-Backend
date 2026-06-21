package com.smartparking.dto;

public class PlateRequest {

    private String plate;

    public PlateRequest() {
    }

    public PlateRequest(String plate) {
        this.plate = plate;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }
}