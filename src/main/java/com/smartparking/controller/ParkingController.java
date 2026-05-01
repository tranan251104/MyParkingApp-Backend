package com.smartparking.controller;

import com.smartparking.dto.ApiResponse;
import com.smartparking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    // Check-in
    @GetMapping("/checkin")
    public ApiResponse<?> checkIn(
            @RequestParam(required = false) String slotId,
            @RequestParam(required = false) String plate) {
        return parkingService.checkIn(slotId, plate);
    }

    // Check-out
    @GetMapping("/checkout")
    public ApiResponse<?> checkOut(@RequestParam String slotId) {
        return parkingService.checkOut(slotId); // Truyền slotId vào đây
    }

    // Thống kê
    @GetMapping("/slots")
    public ApiResponse<?> getSlots() {
        return parkingService.getSlots();
    }

    // danh sách slot cho Flutter
    @GetMapping("/slots/detail")
    public ApiResponse<?> getAllSlots() {
        return parkingService.getAllSlotDetail();
    }

    // test
    @GetMapping("/test")
    public String test() {
        return "Backend OK";
    }
}