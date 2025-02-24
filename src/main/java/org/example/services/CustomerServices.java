package org.example.services;


import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.Priority;
import org.example.models.TravelHistory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class CustomerServices {
    private static CustomerServices instance;
    private final List<Customer> customers;
    private final CarServices carServices;
    private final CSVFileService csvFileService;
    private final AuthServices authServices;
    private Customer currentCustomer;
    private PriorityBlockingQueue<Priority> priorityCustomers = new PriorityBlockingQueue<>();

    private CustomerServices() {
        csvFileService = CSVFileService.getInstance();
        customers = csvFileService.loadCustomers();
        carServices = CarServices.getInstance();
        authServices = AuthServices.getInstance();
    }

    public static CustomerServices getInstance() {
        if (instance == null) {
            instance = new CustomerServices();
        }
        return instance;
    }

    public String login(String phoneNumber, String password) {
        for (Customer customer : customers) {
            if (customer.getPhoneNumber().equals(phoneNumber) && customer.getPassword().equals(password)) {
                currentCustomer = customer;
                System.out.println("Login successful. Welcome, " + customer.getName() + "!");
                return customer.getName();
            }
        }
        System.out.println("Invalid credentials. Please try again or register.");
        return "";
    }

    public boolean register(String phoneNumber, String name, String password) {
        // Check if user already exists
        for (Customer customer : customers) {
            if (customer.getPhoneNumber().equals(phoneNumber)) {
                return false;
            }
        }

        // Create new customer
        Customer newCustomer = new Customer(phoneNumber, name, password);
        customers.add(newCustomer);
        currentCustomer = newCustomer;
        saveCustomersToFile();
        return true;
    }

    //add priority for the customers
    public boolean rentCar(String customerName) {
        if (currentCustomer == null || !currentCustomer.getName().equals(customerName)) {
            System.out.println("Please login first.");
            return false;
        }

        // Display available cars
        List<Car> availableCars = carServices.getAvailableCars();
        if (availableCars.isEmpty()) {
            System.out.println("No cars available for rent.");
            return false;
        }

        System.out.println("\nAvailable Cars:");
        for (int i = 0; i < availableCars.size(); i++) {
            System.out.println((i + 1) + ". " + availableCars.get(i).getModel() + " (ID: " + availableCars.get(i).getCarId() + ")");
        }

        // Get user selection
        Scanner scanner = new Scanner(System.in);
        System.out.print("Select a car (1-" + availableCars.size() + "): ");
        int selection = scanner.nextInt();

        if (selection < 1 || selection > availableCars.size()) {
            System.out.println("Invalid selection.");
            return false;
        }

        // Rent the car
        Car selectedCar = availableCars.get(selection - 1);
        TravelHistory travelHistory = new TravelHistory(currentCustomer.getPhoneNumber(), selectedCar.getCarId());
        currentCustomer.addTravelHistory(travelHistory);

        selectedCar.setAvailability(false);

        // Save changes
        saveCustomersToFile();
        carServices.saveCarsToFile();

        return true;
    }

    public void viewTravelHistory() {
        if (currentCustomer == null) {
            System.out.println("Please login first.");
            return;
        }

        List<TravelHistory> histories = currentCustomer.getTravelHistories();
        if (histories.isEmpty()) {
            System.out.println("No travel history found.");
            return;
        }

        System.out.println("\nYour Travel History:");
        for (TravelHistory history : histories) {
            Car car = carServices.getCarById(history.getCarId());
            String carModel = car != null ? car.getModel() : "Unknown";

            System.out.println("Car: " + carModel + " (ID: " + history.getCarId() + ")");
            System.out.println("Rent Time: " + history.getRentTime());
            System.out.println("Return Time: " + (history.getReturnTime() != null ? history.getReturnTime() : "Active"));
            System.out.println("----------");
        }
    }

    public void viewAllCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }

        System.out.println("\nAll Customers:");
        for (Customer customer : customers) {
            System.out.println("Name: " + customer.getName());
            System.out.println("Phone: " + customer.getPhoneNumber());
            System.out.println("Travel History Count: " + customer.getTravelHistories().size());
            System.out.println("----------");
        }
    }

    public void viewCustomerByPhoneNumber(String phoneNumber) {
        for (Customer customer : customers) {
            if (customer.getPhoneNumber().equals(phoneNumber)) {
                System.out.println("\nCustomer Details:");
                System.out.println("Name: " + customer.getName());
                System.out.println("Phone: " + customer.getPhoneNumber());

                List<TravelHistory> histories = customer.getTravelHistories();
                System.out.println("Travel History Count: " + histories.size());

                if (!histories.isEmpty()) {
                    System.out.println("\nTravel History:");
                    for (TravelHistory history : histories) {
                        Car car = carServices.getCarById(history.getCarId());
                        String carModel = car != null ? car.getModel() : "Unknown";

                        System.out.println("Car: " + carModel + " (ID: " + history.getCarId() + ")");
                        System.out.println("Rent Time: " + history.getRentTime());
                        System.out.println("Return Time: " + (history.getReturnTime() != null ? history.getReturnTime() : "Active"));
                        System.out.println("----------");
                    }
                }

                return;
            }
        }

        System.out.println("Customer not found with phone number: " + phoneNumber);
    }

    public void saveCustomersToFile() {
        csvFileService.saveCustomers(customers);
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }
}
