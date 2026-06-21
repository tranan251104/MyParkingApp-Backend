package com.smartparking.repository;

import com.google.firebase.database.*;
import com.smartparking.model.Slot;
import com.smartparking.model.Vehicle;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@DependsOn("firebaseConfig")
public class FirebaseService {

    private final DatabaseReference vehicleRef;
    private final DatabaseReference slotRef;
    private final DatabaseReference gateCommandRef;

    public FirebaseService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        this.vehicleRef = database.getReference("vehicles");
        this.slotRef = database.getReference("slots");
        this.gateCommandRef = database.getReference("gateCommands");
    }

    private String safeKey(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase()
                .replace(".", "_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_")
                .replace(" ", "");
    }

    public void saveVehicle(Vehicle vehicle) {
        String plateKey = safeKey(vehicle.getPlate());

        vehicleRef.child(plateKey)
                .setValueAsync(vehicle);

        System.out.println("Vehicle saved: " + vehicle.getPlate());
    }

    public Vehicle getVehicle(String plate) {
        CompletableFuture<Vehicle> future = new CompletableFuture<>();

        String plateKey = safeKey(plate);

        vehicleRef.child(plateKey)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                Vehicle vehicle = snapshot.getValue(Vehicle.class);
                                future.complete(vehicle);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                future.completeExceptionally(
                                        new RuntimeException(error.getMessage())
                                );
                            }
                        }
                );

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateVehicle(Vehicle vehicle) {
        String plateKey = safeKey(vehicle.getPlate());

        vehicleRef.child(plateKey)
                .setValueAsync(vehicle);

        System.out.println("Vehicle updated: " + vehicle.getPlate());
    }

    public void updateSlotFull(Slot slot) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("slotId", slot.getSlotId());
        updates.put("occupied", slot.isOccupied());
        updates.put("plate", slot.getPlate());
        updates.put("reserved", slot.isReserved());
        updates.put("reservedPlate", slot.getReservedPlate());
        updates.put("lat", slot.getLat());
        updates.put("lng", slot.getLng());

        slotRef.child(slot.getSlotId())
                .updateChildrenAsync(updates);

        System.out.println("Slot updated: " + slot.getSlotId());
    }

    public void sendGateCommand(String gateId, String command) {
        Map<String, Object> data = new HashMap<>();

        data.put("gateId", gateId);
        data.put("command", command);
        data.put("timestamp", System.currentTimeMillis());

        gateCommandRef.child(gateId)
                .setValueAsync(data);

        System.out.println("Gate command: " + gateId + " - " + command);
    }

    public void openEntryGate() {
        sendGateCommand("entry", "OPEN");
    }

    public void closeEntryGate() {
        sendGateCommand("entry", "CLOSE");
    }

    public void openExitGate() {
        sendGateCommand("exit", "OPEN");
    }

    public void closeExitGate() {
        sendGateCommand("exit", "CLOSE");
    }
}