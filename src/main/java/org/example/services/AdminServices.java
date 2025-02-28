package org.example.services;


import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminServices {
    private static AdminServices instance;
    private final CarServices carServices;
    private final CustomerServices customerServices;

    private AdminServices() {
        carServices = CarServices.getInstance();
        customerServices = CustomerServices.getInstance();
    }

    public static AdminServices getInstance() {
        if (instance == null) {
            instance = new AdminServices();
        }
        return instance;
    }

    public void viewStats() {
        List<Car> cars = carServices.getCars();
        List<Customer> customers = customerServices.getCustomers();

        // Calculate stats
        int totalCars = cars.size();
        int availableCars = (int) cars.stream().filter(Car::isAvailable).count();
        int rentedCars = totalCars - availableCars;

        int totalCustomers = customers.size();

        // Count active rentals and total rentals
        int activeRentals = 0;
        int totalRentals = 0;

        for (Customer customer : customers) {
            for (TravelHistory history : customer.getTravelHistories()) {
                totalRentals++;
                if (history.isActive()) {
                    activeRentals++;
                }
            }
        }

        // Most popular car models
        Map<String, Integer> carModelRentals = new HashMap<>();
        for (Customer customer : customers) {
            for (TravelHistory history : customer.getTravelHistories()) {
                Car car = carServices.getCarById(history.getCarId());
                if (car != null) {
                    String model = car.getModel();
                    carModelRentals.put(model, carModelRentals.getOrDefault(model, 0) + 1);
                }
            }
        }

        // Print statistics
        System.out.println("\n===== SYSTEM STATISTICS =====");
        System.out.println("Total Cars: " + totalCars);
        System.out.println("Available Cars: " + availableCars);
        System.out.println("Currently Rented Cars: " + rentedCars);
        System.out.println("Total Customers: " + totalCustomers);
        System.out.println("Total Rentals (all time): " + totalRentals);
        System.out.println("Active Rentals: " + activeRentals);

        if (!carModelRentals.isEmpty()) {
            System.out.println("\nPopular Car Models:");
            carModelRentals.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> System.out.println("- " + entry.getKey() + ": " + entry.getValue() + " rentals"));
        }

        System.out.println("=============================");
    }
}
