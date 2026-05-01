package com.smartparking.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smartparking.model.Vehicle;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseService {

    private final DatabaseReference vehicleRef =
            FirebaseDatabase.getInstance().getReference("vehicles");

    private final DatabaseReference slotRef =
            FirebaseDatabase.getInstance().getReference("slots");

    // 🚗 save vehicle
    public void saveVehicle(Vehicle vehicle) {
        vehicleRef.child(vehicle.getPlate()).setValueAsync(vehicle);
    }

    // 🚗 get vehicle
    public Vehicle getVehicle(String plate) {
        CompletableFuture<Vehicle> future = new CompletableFuture<>();

        vehicleRef.child(plate).addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {

                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        Vehicle vehicle = snapshot.getValue(Vehicle.class);
                        future.complete(vehicle);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                }
        );

        try {
            return future.get(); // block đúng cách
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // 🚗 update FULL vehicle (CHECK-IN / CHECK-OUT dùng chung)
    public void updateVehicle(Vehicle vehicle) {
        vehicleRef.child(vehicle.getPlate())
                .setValueAsync(vehicle);
    }

    // 🅿️ update slot
    public void updateSlot(String slotId, boolean occupied, String plate) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("occupied", occupied);
        updates.put("plate", plate);

        slotRef.child(slotId).updateChildrenAsync(updates);

        System.out.println("🔥 Slot updated: " + slotId);
    }
}