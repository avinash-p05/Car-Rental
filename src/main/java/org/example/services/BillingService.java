package org.example.services;

import org.example.models.Billing;
import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillingService {
    private static BillingService instance;
    private final List<Billing> billings;
    private final CSVFileService csvFileService;
    private final CarServices carServices;
    private final CustomerServices customerServices;

    private BillingService() {
        csvFileService = CSVFileService.getInstance();
        billings = csvFileService.loadBillings();
        carServices = CarServices.getInstance();
        customerServices = CustomerServices.getInstance();
    }

    public static BillingService getInstance() {
        if (instance == null) {
            instance = new BillingService();
        }
        return instance;
    }

    public void addBilling(Billing billing) {
        billings.add(billing);
    }

    public double processCarsReturn(String carId) {
        Car car = carServices.getCarById(carId);
        if (car == null || car.isAvailable()) {
            return -1; // Car not found or not rented
        }

        // Find the active billing for this car
        Billing activeBilling = null;
        for (Billing billing : billings) {
            if (billing.getCarId().equals(carId) && !billing.isPaid()) {
                activeBilling = billing;
                break;
            }
        }

        if (activeBilling == null) {
            return -1; // No active billing found
        }

        // Find the rental start time from travel history
        LocalDateTime rentTime = null;
        Customer customer = null;

        for (Customer c : customerServices.getCustomers()) {
            for (TravelHistory history : c.getTravelHistories()) {
                if (history.getCarId().equals(carId) && history.isActive()) {
                    rentTime = history.getRentTime();
                    customer = c;
                    break;
                }
            }
            if (rentTime != null) break;
        }

        if (rentTime == null) {
            return -1; // No active rental found
        }

        // Calculate the final bill amount
        double remainingAmount = activeBilling.calculateFinalBill(rentTime, LocalDateTime.now());

        // Update the final amount in the billing
        activeBilling.setFinalAmount(activeBilling.getAdvancePayment() + remainingAmount);

        // Save the updated billing
        saveBillingsToFile();

        return remainingAmount;
    }

    public void markBillingAsPaid(String carId) {
        for (Billing billing : billings) {
            if (billing.getCarId().equals(carId) && !billing.isPaid()) {
                billing.markAsPaid();
                break;
            }
        }
    }

    public List<Billing> getBillingHistoryForCustomer(String phoneNumber) {
        List<Billing> customerBillings = new ArrayList<>();
        for (Billing billing : billings) {
            if (billing.getCustomerId().equals(phoneNumber)) {
                customerBillings.add(billing);
            }
        }
        return customerBillings;
    }

    public List<Billing> getAllBillings() {
        return billings;
    }

    public void saveBillingsToFile() {
        csvFileService.saveBillings(billings);
    }

    public String getCustomerNameByPhone(String phoneNumber) {
        Customer customer = customerServices.getCustomers().stream()
                .filter(c -> c.getPhoneNumber().equals(phoneNumber))
                .findFirst()
                .orElse(null);
        return customer != null ? customer.getName() : "Unknown";
    }
}