package org.example.models;

public class Car {
    private String carId;
    private String model;
    private boolean availability;
    private double hourlyRate; // Added hourly rate field
    private String category;   // Economy, Standard, Premium, etc.

    public Car(String carId, String model) {
        this.carId = carId;
        this.model = model;
        this.availability = true;
        this.hourlyRate = 10.0; // Default hourly rate
        this.category = "Standard"; // Default category
    }

    public Car(String carId, String model, boolean availability) {
        this.carId = carId;
        this.model = model;
        this.availability = availability;
        this.hourlyRate = 10.0; // Default hourly rate
        this.category = "Standard"; // Default category
    }

    // Full constructor with all fields
    public Car(String carId, String model, boolean availability, double hourlyRate, String category) {
        this.carId = carId;
        this.model = model;
        this.availability = availability;
        this.hourlyRate = hourlyRate;
        this.category = category;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Car{" +
                "carId='" + carId + '\'' +
                ", model='" + model + '\'' +
                ", category='" + category + '\'' +
                ", hourlyRate=" + hourlyRate +
                ", availability=" + availability +
                '}';
    }
}