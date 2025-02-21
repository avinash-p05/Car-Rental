package org.example.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TravelHistory {
    private String customerId;
    private String carId;
    private LocalDateTime rentTime;
    private LocalDateTime returnTime;

    public TravelHistory(String customerId, String carId) {
        this.customerId = customerId;
        this.carId = carId;
        this.rentTime = LocalDateTime.now();
    }

    public TravelHistory(String customerId, String carId, LocalDateTime rentTime, LocalDateTime returnTime) {
        this.customerId = customerId;
        this.carId = carId;
        this.rentTime = rentTime;
        this.returnTime = returnTime;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCarId() {
        return carId;
    }

    public LocalDateTime getRentTime() {
        return rentTime;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public boolean isActive() {
        return returnTime == null;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String rentTimeStr = rentTime.format(formatter);
        String returnTimeStr = returnTime != null ? returnTime.format(formatter) : "Active";

        return "TravelHistory{" +
                "customerId='" + customerId + '\'' +
                ", carId='" + carId + '\'' +
                ", rentTime=" + rentTimeStr +
                ", returnTime=" + returnTimeStr +
                '}';
    }
}
