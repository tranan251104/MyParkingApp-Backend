package com.smartparking.service;

import com.smartparking.dto.ApiResponse;
import com.smartparking.model.Slot;
import com.smartparking.model.Vehicle;
import com.smartparking.repository.FirebaseService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.*;

@Service
public class ParkingService {

    @Autowired
    private FirebaseService firebaseService;

    private final Map<String, Slot> slotMap = new HashMap<>();
    private final Map<String, String> vehicleSlotMap = new HashMap<>();

    // 🔥 INIT LOCAL DATA
    public ParkingService() {
        for (int i = 1; i <= 1000; i++) {
            String id = "S" + i;
            slotMap.put(id, new Slot(id, false, null));
        }
    }

    // Sửa lại hàm để nhận tham số (nếu gọi từ App)
    public ApiResponse<?> checkIn(String slotId, String plate) {
        // 1. Kiểm tra tham số đầu vào (Phòng trường hợp link bị thiếu dữ liệu)
        if (slotId == null || plate == null || slotId.isEmpty() || plate.isEmpty()) {
            return new ApiResponse<>(false, "Thiếu thông tin vị trí hoặc biển số xe", null);
        }

        // 2. Kiểm tra xem xe này đã có trong bãi chưa (Tránh lỗi 1 xe vào 2 lần)
        if (vehicleSlotMap.containsKey(plate)) {
            return new ApiResponse<>(false, "Xe biển số " + plate + " hiện đang có trong bãi rồi!", null);
        }

        // 3. Kiểm tra vị trí ô đỗ
        Slot slot = slotMap.get(slotId);
        if (slot == null) {
            return new ApiResponse<>(false, "Vị trí " + slotId + " không tồn tại", null);
        }

        // 4. Kiểm tra ô đỗ đã có xe khác chưa
        if (slot.isOccupied()) {
            return new ApiResponse<>(false, "Ô đỗ " + slotId + " hiện đã có xe khác!", null);
        }

        // 5. Cập nhật trạng thái bộ nhớ đệm (Local Maps)
        slot.setOccupied(true);
        slot.setPlate(plate);
        vehicleSlotMap.put(plate, slotId);

        // 6. Tạo đối tượng xe và lưu giờ vào (Để sau này Checkout tính tiền)
        Vehicle vehicle = new Vehicle(
                plate,
                System.currentTimeMillis(), // Lấy giờ hệ thống hiện tại
                slotId
        );

        // 7. Đồng bộ lên Firebase Realtime Database
        firebaseService.saveVehicle(vehicle);
        firebaseService.updateSlot(slotId, true, plate);

        System.out.println("🚗 [HỆ THỐNG] CHECK-IN THÀNH CÔNG: " + plate + " -> " + slotId);

        return new ApiResponse<>(true, "Check-in thành công",
                Map.of("plate", plate, "slot", slotId));
    }

    public ApiResponse<?> checkOut(@RequestParam String slotId) { // Nhận slotId từ App

        // 1. Tìm biển số xe (plate) dựa trên slotId trong Map của bạn
        String plate = null;
        for (Map.Entry<String, String> entry : vehicleSlotMap.entrySet()) {
            if (entry.getValue().equals(slotId)) {
                plate = entry.getKey();
                break;
            }
        }

        // 2. Kiểm tra nếu ô đỗ trống hoặc không có xe
        if (plate == null) {
            return new ApiResponse<>(false, "Ô đỗ " + slotId + " hiện đang trống hoặc không có xe!", null);
        }

        Slot slot = slotMap.get(slotId);
        if (slot == null) {
            return new ApiResponse<>(false, "Vị trí không hợp lệ", null);
        }

        // 3. Tính toán thời gian và tiền bạc
        // ============================
        // ⏱ TIME CALCULATION
        // ============================
        Vehicle vehicle = firebaseService.getVehicle(plate);
        long checkInTime = vehicle.getCheckInTime();
        long checkOutTime = System.currentTimeMillis();
        long durationMs = checkOutTime - checkInTime;

        // Tính số phút (cho demo)
        long minutes = durationMs / (1000 * 60);

        // ============================
        // 💰 PRICE CALCULATION (DEMO: 5000đ / PHÚT)
        // ============================
        long pricePerMinute = 5000;

        // Đảm bảo tối thiểu 1 phút (5000đ) để ngay khi vào rồi ra luôn bạn vẫn thấy có tiền phí
        long billableMinutes = Math.max(1, minutes);
        long amount = billableMinutes * pricePerMinute;

        // 4. Cập nhật dữ liệu (Xóa xe khỏi bãi)
        slot.setOccupied(false);
        slot.setPlate(null);
        vehicle.setCheckOutTime(checkOutTime);
        vehicle.setAmount(amount);

        vehicleSlotMap.remove(plate); // Xóa xe khỏi map quản lý

        // 5. Đồng bộ Firebase
        firebaseService.updateVehicle(vehicle);
        firebaseService.updateSlot(slotId, false, null);

        System.out.println("🚗 CHECK-OUT THÀNH CÔNG: " + plate + " tại " + slotId);

        return new ApiResponse<>(true, "Check-out success",
                Map.of(
                        "plate", plate,
                        "slot", slotId,
                        "durationMinutes", minutes,
                        "amount", amount
                ));
    }

    // 📊 SUMMARY
    public ApiResponse<?> getSlots() {

        long available = slotMap.values()
                .stream()
                .filter(slot -> !slot.isOccupied())
                .count();

        return new ApiResponse<>(true, "Slot summary",
                Map.of(
                        "total", slotMap.size(),
                        "available", available,
                        "occupied", slotMap.size() - available
                ));
    }

    // 📋 ALL SLOT DETAIL
    public ApiResponse<?> getAllSlotDetail() {
        return new ApiResponse<>(true, "All slots", slotMap.values());
    }

    // 🔥 CALL PYTHON AI

}