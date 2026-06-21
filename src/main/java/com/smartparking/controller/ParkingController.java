package com.smartparking.controller;

import com.smartparking.dto.*;
import com.smartparking.service.ParkingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "*")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/suggest")
    public ApiResponse<?> suggest(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return parkingService.suggest(lat, lng);
    }

    @PostMapping("/reserve")
    public ApiResponse<?> reserve(@RequestBody ReserveRequest request) {
        return parkingService.reserve(
                request.getSlotId(),
                request.getPlate()
        );
    }

    @GetMapping("/reserve")
    public ApiResponse<?> reserveLegacy(
            @RequestParam String slotId,
            @RequestParam String plate
    ) {
        return parkingService.reserve(slotId, plate);
    }

    @PostMapping("/checkin")
    public ApiResponse<?> checkIn(@RequestBody PlateRequest request) {
        return parkingService.checkIn(request.getPlate());
    }

    @GetMapping("/checkin")
    public ApiResponse<?> checkInLegacy(@RequestParam String plate) {
        return parkingService.checkIn(plate);
    }

    @PostMapping("/checkout")
    public ApiResponse<?> checkOut(@RequestBody CheckoutRequest request) {
        return parkingService.checkOut(request.getSlotId());
    }

    @GetMapping("/checkout")
    public ApiResponse<?> checkOutLegacy(@RequestParam String slotId) {
        return parkingService.checkOut(slotId);
    }

    @PostMapping("/checkout/by-plate")
    public ApiResponse<?> checkOutByPlate(@RequestBody PlateRequest request) {
        return parkingService.checkOutByPlate(request.getPlate());
    }

    @GetMapping("/checkout/by-plate")
    public ApiResponse<?> checkOutByPlateLegacy(@RequestParam String plate) {
        return parkingService.checkOutByPlate(plate);
    }

    @PostMapping("/slot/sensor")
    public ApiResponse<?> updateSlotFromSensor(@RequestBody SlotSensorRequest request) {
        return parkingService.updateSlotFromSensor(
                request.getSlotId(),
                request.isOccupied()
        );
    }

    @GetMapping("/slots/detail")
    public ApiResponse<?> getAllSlots() {
        return parkingService.getAllSlotDetail();
    }
}