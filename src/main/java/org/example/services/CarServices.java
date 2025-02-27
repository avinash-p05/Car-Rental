package org.example.services;


import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CarServices {
    private static CarServices instance;
    private final List<Car> cars;
    private final CSVFileService csvFileService;

    private CarServices() {
        csvFileService = CSVFileService.getInstance();
        cars = csvFileService.loadCars();
    }

    public static CarServices getInstance() {
        if (instance == null) {
            instance = new CarServices();
        }
        return instance;
    }

    public void addCar(String carId, String model,String category,double hourlyRate) {
        // Check if car already exists
        for (Car car : cars) {
            if (car.getCarId().equals(carId)) {
                System.out.println("Car with ID " + carId + " already exists.");
                return;
            }
        }

        // Add new car
        Car newCar = new Car(carId, model,true, hourlyRate, category);
        cars.add(newCar);
        System.out.println("Car added successfully: " + model + " (ID: " + carId + ")");
        saveCarsToFile();
    }

    public void viewAllCars() {
        if (cars.isEmpty()) {
            System.out.println("No cars available.");
            return;
        }

        // Create a copy of the cars list to sort
        List<Car> sortedCars = new ArrayList<>(cars);

        // Sort cars by model name using custom merge sort
        mergeSort(sortedCars, 0, sortedCars.size() - 1);

        System.out.println("\nAll Cars (Sorted by Model):");
        for (Car car : sortedCars) {
            System.out.println("ID: " + car.getCarId());
            System.out.println("Model: " + car.getModel());
            System.out.println("Category: " + car.getCategory());
            System.out.println("Hourly Rate: " + car.getHourlyRate());
            System.out.println("Status: " + (car.isAvailable() ? "Available" : "Rented"));
            System.out.println("----------");
        }
    }

    // Custom Merge Sort implementation to sort cars by model name
    private void mergeSort(List<Car> cars, int left, int right) {
        if (left < right) {
            // Find the middle point
            int middle = left + (right - left) / 2;

            // Sort first and second halves
            mergeSort(cars, left, middle);
            mergeSort(cars, middle + 1, right);

            // Merge the sorted halves
            merge(cars, left, middle, right);
        }
    }

    // Merge two subarrays of cars
    private void merge(List<Car> cars, int left, int middle, int right) {
        // Calculate sizes of two subarrays to be merged
        int n1 = middle - left + 1;
        int n2 = right - middle;

        // Create temp arrays
        List<Car> leftArray = new ArrayList<>(n1);
        List<Car> rightArray = new ArrayList<>(n2);

        // Copy data to temp arrays
        for (int i = 0; i < n1; i++) {
            leftArray.add(cars.get(left + i));
        }
        for (int j = 0; j < n2; j++) {
            rightArray.add(cars.get(middle + 1 + j));
        }

        // Merge the temp arrays

        // Initial indexes of first and second subarrays
        int i = 0, j = 0;

        // Initial index of merged subarray
        int k = left;

        while (i < n1 && j < n2) {
            // Compare car models for sorting
            if (leftArray.get(i).getModel().compareToIgnoreCase(rightArray.get(j).getModel()) <= 0) {
                cars.set(k, leftArray.get(i));
                i++;
            } else {
                cars.set(k, rightArray.get(j));
                j++;
            }
            k++;
        }

        // Copy remaining elements of leftArray[] if any
        while (i < n1) {
            cars.set(k, leftArray.get(i));
            i++;
            k++;
        }

        // Copy remaining elements of rightArray[] if any
        while (j < n2) {
            cars.set(k, rightArray.get(j));
            j++;
            k++;
        }
    }

    public List<Car> getAvailableCars() {
        return cars.stream()
                .filter(Car::isAvailable)
                .collect(Collectors.toList());
    }

    public Car getCarById(String carId) {
        for (Car car : cars) {
            if (car.getCarId().equals(carId)) {
                return car;
            }
        }
        return null;
    }

    public void viewAllTravelHistory() {
        CustomerServices customerServices = CustomerServices.getInstance();
        List<Customer> customers = customerServices.getCustomers();

        List<TravelHistory> allHistories = new ArrayList<>();
        for (Customer customer : customers) {
            allHistories.addAll(customer.getTravelHistories());
        }

        if (allHistories.isEmpty()) {
            System.out.println("No travel history found.");
            return;
        }

        System.out.println("\nAll Travel History:");
        for (TravelHistory history : allHistories) {
            Customer customer = null;
            for (Customer c : customers) {
                if (c.getPhoneNumber().equals(history.getCustomerId())) {
                    customer = c;
                    break;
                }
            }

            Car car = getCarById(history.getCarId());

            System.out.println("Customer: " + (customer != null ? customer.getName() : "Unknown"));
            System.out.println("Phone: " + history.getCustomerId());
            System.out.println("Car: " + (car != null ? car.getModel() : "Unknown") + " (ID: " + history.getCarId() + ")");
            System.out.println("Rent Time: " + history.getRentTime());
            System.out.println("Return Time: " + (history.getReturnTime() != null ? history.getReturnTime() : "Active"));
            System.out.println("----------");
        }
    }

    public void viewTravelHistoryOfCustomer(String phoneNumber) {
        CustomerServices customerServices = CustomerServices.getInstance();
        Customer customer = null;

        for (Customer c : customerServices.getCustomers()) {
            if (c.getPhoneNumber().equals(phoneNumber)) {
                customer = c;
                break;
            }
        }

        if (customer == null) {
            System.out.println("Customer not found with phone number: " + phoneNumber);
            return;
        }

        List<TravelHistory> histories = customer.getTravelHistories();
        if (histories.isEmpty()) {
            System.out.println("No travel history found for " + customer.getName());
            return;
        }

        System.out.println("\nTravel History for " + customer.getName() + ":");
        for (TravelHistory history : histories) {
            Car car = getCarById(history.getCarId());

            System.out.println("Car: " + (car != null ? car.getModel() : "Unknown") + " (ID: " + history.getCarId() + ")");
            System.out.println("Rent Time: " + history.getRentTime());
            System.out.println("Return Time: " + (history.getReturnTime() != null ? history.getReturnTime() : "Active"));
            System.out.println("----------");
        }
    }

    public void returnCar(String carId) {
        Car car = getCarById(carId);
        if (car == null) {
            System.out.println("Car not found with ID: " + carId);
            return;
        }

        if (car.isAvailable()) {
            System.out.println("This car is not currently rented.");
            return;
        }

        // Find active travel history for this car
        CustomerServices customerServices = CustomerServices.getInstance();
        for (Customer customer : customerServices.getCustomers()) {
            for (TravelHistory history : customer.getTravelHistories()) {
                if (history.getCarId().equals(carId) && history.isActive()) {
                    // Mark car as returned
                    car.setAvailability(true);
                    history.setReturnTime(LocalDateTime.now());

                    // Save changes
                    saveCarsToFile();
                    customerServices.saveCustomersToFile();

                    System.out.println("Car successfully returned by " + customer.getName());
                    return;
                }
            }
        }

        System.out.println("No active rental found for this car.");
    }

    public void saveCarsToFile() {
        csvFileService.saveCars(cars);
    }

    public void makeExit() {
        saveCarsToFile();
        CustomerServices.getInstance().saveCustomersToFile();
        System.out.println("All data saved to CSV sheets");
    }

    public List<Car> getCars() {
        return cars;
    }
}