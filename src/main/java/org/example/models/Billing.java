package org.example.models;

import java.time.Duration;
import java.time.LocalDateTime;

public class Billing {
    private String billId;
    private String customerId;
    private String carId;
    private double hourlyRate;
    private double advancePayment;
    private double finalAmount;
    private LocalDateTime billingTime;
    private boolean isPaid;

    // Constructor for creating a new billing record when renting
    public Billing(String customerId, String carId, double hourlyRate, double advancePayment) {
        this.billId = generateBillId(customerId, carId);
        this.customerId = customerId;
        this.carId = carId;
        this.hourlyRate = hourlyRate;
        this.advancePayment = advancePayment;
        this.finalAmount = 0.0; // Will be calculated on return
        this.billingTime = LocalDateTime.now();
        this.isPaid = false;
    }

    // Constructor for loading from CSV
    public Billing(String billId, String customerId, String carId, double hourlyRate,
                   double advancePayment, double finalAmount, LocalDateTime billingTime, boolean isPaid) {
        this.billId = billId;
        this.customerId = customerId;
        this.carId = carId;
        this.hourlyRate = hourlyRate;
        this.advancePayment = advancePayment;
        this.finalAmount = finalAmount;
        this.billingTime = billingTime;
        this.isPaid = isPaid;
    }

    // Generate a unique bill ID
    private String generateBillId(String customerId, String carId) {
        return "BILL-" + customerId.substring(Math.max(0, customerId.length() - 4)) +
                "-" + carId + "-" + System.currentTimeMillis() % 10000;
    }

    // Calculate the final bill amount based on rental duration
    public double calculateFinalBill(LocalDateTime rentTime, LocalDateTime returnTime) {
        if (returnTime == null) {
            returnTime = LocalDateTime.now();
        }

        // Calculate duration in hours (rounded up)
        Duration duration = Duration.between(rentTime, returnTime);
        long hours = duration.toHours();
        if (duration.toMinutesPart() > 0) {
            hours++; // Round up partial hours
        }

        // Minimum 1 hour charge
        hours = Math.max(1, hours);

        // Calculate total amount
        finalAmount = hourlyRate * hours;

        // Deduct advance payment
        double remainingAmount = finalAmount - advancePayment;

        return remainingAmount;
    }

    // Mark bill as paid
    public void markAsPaid() {
        this.isPaid = true;
    }

    // Getters and Setters
    public String getBillId() {
        return billId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCarId() {
        return carId;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getAdvancePayment() {
        return advancePayment;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public LocalDateTime getBillingTime() {
        return billingTime;
    }

    public boolean isPaid() {
        return isPaid;
    }

    @Override
    public String toString() {
        return "Billing{" +
                "billId='" + billId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", carId='" + carId + '\'' +
                ", hourlyRate=" + hourlyRate +
                ", advancePayment=" + advancePayment +
                ", finalAmount=" + finalAmount +
                ", billingTime=" + billingTime +
                ", isPaid=" + isPaid +
                '}';
    }
}
