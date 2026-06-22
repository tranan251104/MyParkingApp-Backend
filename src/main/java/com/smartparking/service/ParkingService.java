package com.smartparking.service;

import com.smartparking.dto.ApiResponse;
import com.smartparking.model.Slot;
import com.smartparking.model.Vehicle;
import com.smartparking.repository.FirebaseService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParkingService {

    private final FirebaseService firebaseService;

    private final Map<String, Slot> slotMap = new ConcurrentHashMap<>();
    private final Map<String, String> vehicleSlotMap = new ConcurrentHashMap<>();

    @Value("${parking.init-firebase-slots:true}")
    private boolean initFirebaseSlotsEnabled;

    public ParkingService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
        initLocalSlots();
    }

    private void initLocalSlots() {
        for (int i = 1; i <= 1000; i++) {
            String id = "S" + i;

            Slot slot = new Slot(id, false, null);

            slot.setLat(21.028 + (i * 0.00005));
            slot.setLng(105.85 + (i * 0.00005));

            slot.setReserved(false);
            slot.setReservedPlate(null);

            slotMap.put(id, slot);
        }

        System.out.println("Local slots initialized: " + slotMap.size());
    }

    @PostConstruct
    public void initFirebaseSlots() {
        if (!initFirebaseSlotsEnabled) {
            System.out.println("Init Firebase slots skipped");
            return;
        }

        for (Slot slot : slotMap.values()) {
            firebaseService.updateSlotFull(slot);
        }

        System.out.println("Init slots to Firebase completed");
    }

    private String normalizePlate(String plate) {
        if (plate == null) {
            return null;
        }

        return plate
                .trim()
                .toUpperCase()
                .replace(" ", "");
    }

    private int extractSlotNumber(String slotId) {
        try {
            return Integer.parseInt(slotId.replace("S", ""));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        final int R = 6371000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c =
                2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public ApiResponse<?> suggest(double userLat, double userLng) {
        List<Map<String, Object>> result =
                slotMap.values()
                        .stream()
                        .filter(slot -> !slot.isOccupied() && !slot.isReserved())
                        .map(slot -> {
                            double distance = calculateDistance(
                                    userLat,
                                    userLng,
                                    slot.getLat(),
                                    slot.getLng()
                            );

                            Map<String, Object> data = new LinkedHashMap<>();

                            data.put("slotId", slot.getSlotId());
                            data.put("distance", (int) distance);
                            data.put("lat", slot.getLat());
                            data.put("lng", slot.getLng());

                            return data;
                        })
                        .sorted(
                                Comparator.comparingInt(
                                        m -> ((Number) m.get("distance")).intValue()
                                )
                        )
                        .limit(3)
                        .toList();

        return new ApiResponse<>(
                true,
                "Suggest OK",
                result
        );
    }

    public ApiResponse<?> reserve(String slotId, String plate) {
        plate = normalizePlate(plate);

        if (slotId == null || slotId.isBlank()) {
            return ApiResponse.fail("Thiếu slotId");
        }

        if (plate == null || plate.isBlank()) {
            return ApiResponse.fail("Thiếu biển số");
        }

        Slot slot = slotMap.get(slotId);

        if (slot == null) {
            return ApiResponse.fail("Slot không tồn tại");
        }

        if (slot.isOccupied()) {
            return ApiResponse.fail("Slot đã có xe");
        }

        if (slot.isReserved()) {
            return ApiResponse.fail("Slot đã được đặt trước");
        }

        slot.setReserved(true);
        slot.setReservedPlate(plate);

        firebaseService.updateSlotFull(slot);

        return new ApiResponse<>(
                true,
                "Đặt chỗ thành công",
                Map.of(
                        "slotId", slotId,
                        "plate", plate
                )
        );
    }

    public ApiResponse<?> checkIn(String plate) {
        String normalizedPlate = normalizePlate(plate);

        if (normalizedPlate == null || normalizedPlate.isBlank()) {
            return ApiResponse.fail("Thiếu biển số");
        }

        if (vehicleSlotMap.containsKey(normalizedPlate)) {
            return ApiResponse.fail("Xe đã trong bãi");
        }

        Optional<Slot> reservedSlot =
                slotMap.values()
                        .stream()
                        .filter(slot ->
                                !slot.isOccupied()
                                        && slot.isReserved()
                                        && normalizedPlate.equals(slot.getReservedPlate())
                        )
                        .findFirst();

        Slot selectedSlot;

        if (reservedSlot.isPresent()) {
            selectedSlot = reservedSlot.get();

            selectedSlot.setReserved(false);
            selectedSlot.setReservedPlate(null);

        } else {
            List<Slot> availableSlots =
                    slotMap.values()
                            .stream()
                            .filter(slot -> !slot.isOccupied() && !slot.isReserved())
                            .sorted(
                                    Comparator.comparingInt(
                                            slot -> extractSlotNumber(slot.getSlotId())
                                    )
                            )
                            .toList();

            if (availableSlots.isEmpty()) {
                return ApiResponse.fail("Hết chỗ");
            }

            selectedSlot = availableSlots.get(0);
        }

        selectedSlot.setOccupied(true);
        selectedSlot.setPlate(normalizedPlate);

        vehicleSlotMap.put(normalizedPlate, selectedSlot.getSlotId());

        Vehicle vehicle = new Vehicle(
                normalizedPlate,
                System.currentTimeMillis(),
                selectedSlot.getSlotId()
        );

        firebaseService.saveVehicle(vehicle);
        firebaseService.updateSlotFull(selectedSlot);

        firebaseService.openEntryGate();

        return new ApiResponse<>(
                true,
                "Check-in OK",
                Map.of(
                        "plate", normalizedPlate,
                        "slotId", selectedSlot.getSlotId()
                )
        );
    }

    public ApiResponse<?> checkOut(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return ApiResponse.fail("Thiếu slotId");
        }

        Slot slot = slotMap.get(slotId);

        if (slot == null) {
            return ApiResponse.fail("Slot không tồn tại");
        }

        if (!slot.isOccupied()) {
            return ApiResponse.fail("Slot trống");
        }

        String plate = slot.getPlate();

        if (plate == null || plate.isBlank()) {
            return ApiResponse.fail("Slot đang có xe nhưng không có biển số");
        }

        Vehicle vehicle = firebaseService.getVehicle(plate);

        if (vehicle == null) {
            return ApiResponse.fail("Không tìm thấy xe trong Firebase");
        }

        long checkInTime = vehicle.getCheckInTime() == null
                ? System.currentTimeMillis()
                : vehicle.getCheckInTime();

        long durationMinutes =
                (System.currentTimeMillis() - checkInTime) / 60000;

        long chargedMinutes = Math.max(1, durationMinutes);
        long amount = chargedMinutes * 5000L;

        vehicle.setCheckOutTime(System.currentTimeMillis());
        vehicle.setAmount(amount);

        slot.setOccupied(false);
        slot.setPlate(null);
        slot.setReserved(false);
        slot.setReservedPlate(null);

        vehicleSlotMap.remove(plate);

        firebaseService.updateVehicle(vehicle);
        firebaseService.updateSlotFull(slot);

        firebaseService.openExitGate();

        return new ApiResponse<>(
                true,
                "Checkout OK",
                Map.of(
                        "plate", plate,
                        "slotId", slotId,
                        "minutes", durationMinutes,
                        "amount", amount
                )
        );
    }

    public ApiResponse<?> checkOutByPlate(String plate) {
        String normalizedPlate = normalizePlate(plate);

        if (normalizedPlate == null || normalizedPlate.isBlank()) {
            return ApiResponse.fail("Thiếu biển số");
        }

        String slotId = vehicleSlotMap.get(normalizedPlate);

        if (slotId == null) {
            Optional<Slot> slotOptional =
                    slotMap.values()
                            .stream()
                            .filter(slot -> normalizedPlate.equals(slot.getPlate()))
                            .findFirst();

            if (slotOptional.isPresent()) {
                slotId = slotOptional.get().getSlotId();
            }
        }

        if (slotId == null) {
            Vehicle vehicle = firebaseService.getVehicle(normalizedPlate);

            if (vehicle == null) {
                return ApiResponse.fail("Không tìm thấy xe trong bãi");
            }

            slotId = vehicle.getSlotId();

            if (slotId == null || slotId.isBlank()) {
                return ApiResponse.fail("Xe có trong Firebase nhưng không có slotId");
            }

            Slot slot = slotMap.get(slotId);

            if (slot == null) {
                return ApiResponse.fail("Slot của xe không tồn tại");
            }

            slot.setOccupied(true);
            slot.setPlate(normalizedPlate);

            vehicleSlotMap.put(normalizedPlate, slotId);
        }

        return checkOut(slotId);
    }

    public ApiResponse<?> updateSlotFromSensor(String slotId, boolean occupied) {
        if (slotId == null || slotId.isBlank()) {
            return ApiResponse.fail("Thiếu slotId");
        }

        Slot slot = slotMap.get(slotId);

        if (slot == null) {
            return ApiResponse.fail("Slot không tồn tại");
        }

        slot.setOccupied(occupied);

        if (!occupied) {
            String oldPlate = slot.getPlate();

            if (oldPlate != null) {
                vehicleSlotMap.remove(oldPlate);
            }

            slot.setPlate(null);
            slot.setReserved(false);
            slot.setReservedPlate(null);
        }

        firebaseService.updateSlotFull(slot);

        return new ApiResponse<>(
                true,
                "Cập nhật cảm biến thành công",
                Map.of(
                        "slotId", slotId,
                        "occupied", occupied
                )
        );
    }

    public ApiResponse<?> getAllSlotDetail() {
        List<Slot> slots =
                slotMap.values()
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        slot -> extractSlotNumber(slot.getSlotId())
                                )
                        )
                        .toList();

        return new ApiResponse<>(
                true,
                "All slots",
                slots
        );
    }
}