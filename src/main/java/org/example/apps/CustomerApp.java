package org.example.apps;

import org.example.models.Billing;
import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;
import org.example.services.*;

import java.util.List;
import java.util.Scanner;

public class CustomerApp {
    private final CarServices carServices = CarServices.getInstance();
    private final CustomerServices customerServices = CustomerServices.getInstance();
    private final AuthServices authServices = AuthServices.getInstance();
    private final BillingService billingService = BillingService.getInstance();
    private final AdvanceService advanceService = AdvanceService.getInstance();


    public int displayMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        String name = "";

        while (!exit) {
            // Update name from current customer if logged in
            Customer currentCustomer = customerServices.getCurrentCustomer();
            if (currentCustomer != null) {
                name = currentCustomer.getName();
            }

            String nameDisplay = name.isEmpty() ? "" : " (Logged in as: " + name + ")";

            System.out.println("\n*** Customer Menu" + nameDisplay + " ***");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Rent a Car");
            System.out.println("4. View my travel History");
            System.out.println("5. Return a Car");
            System.out.println("6. View my Billing History");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter your Phone Number: ");
                    String pn = scanner.nextLine();
                    System.out.print("Enter the password: ");
                    String pw = scanner.nextLine();
                    name = customerServices.login(pn, pw);
                    break;

                case 2:
                    System.out.print("Enter your Phone Number: ");
                    String phoneNumber = scanner.nextLine();
                    System.out.print("Enter your Name: ");
                    name = scanner.nextLine();
                    System.out.print("Enter the password: ");
                    String password = scanner.nextLine();
                    String hashedPassword = authServices.generateHash(password);
                    boolean status = customerServices.register(phoneNumber, name, hashedPassword);
                    if (status) {
                        System.out.println("Successfully Registered!!");
                    } else {
                        System.out.println("Failed to Register!! User may already exist!! Try to login");
                    }
                    break;

                case 3:
                    if (name.isEmpty()) {
                        System.out.println("Please login first to rent a car.");
                    } else {
                        System.out.println("Renting Car for - " + name);
                        rentCarWithAdvancePayment(name);
                    }
                    break;

                case 4:
                    customerServices.viewTravelHistory();
                    break;

                case 5:
                    if (name.isEmpty()) {
                        System.out.println("Please login first to return a car.");
                    } else {
                        returnCarWithBilling();
                    }
                    break;

                case 6:
                    if (name.isEmpty()) {
                        System.out.println("Please login first to view billing history.");
                    } else {
                        viewMyBillingHistory();
                    }
                    break;

                case 7:
                    customerServices.saveCustomersToFile();
                    carServices.saveCarsToFile();
                    billingService.saveBillingsToFile();
                    exit = true;
                    System.out.println("Exiting the customer menu. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        return 0;
    }

    private void rentCarWithAdvancePayment(String customerName) {
        Customer currentCustomer = customerServices.getCurrentCustomer();
        if (currentCustomer == null || !currentCustomer.getName().equals(customerName)) {
            System.out.println("Please login first.");
            return;
        }

        // Display available cars
        List<Car> availableCars = carServices.getAvailableCars();
        if (availableCars.isEmpty()) {
            System.out.println("No cars available for rent.");
            return;
        }
        //Display most popular cars
        advanceService.displayMostPopularCars(2);
        System.out.println("----------------");

        //Recommend cars for the customer
        advanceService.displayCarRecommendationsForCustomer(currentCustomer.getPhoneNumber());
        System.out.println("----------------");

        System.out.println("\nAvailable Cars:");
        System.out.println("----------------");
        for (int i = 0; i < availableCars.size(); i++) {
            Car car = availableCars.get(i);
            System.out.println((i + 1) + ". " + car.getModel() +
                    " (ID: " + car.getCarId() + ") - Category: " + car.getCategory() +
                    " - Hourly Rate: $" + String.format("%.2f", car.getHourlyRate()));
        }

        // Get user selection
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nSelect a car (1-" + availableCars.size() + "): ");
        int selection = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (selection < 1 || selection > availableCars.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        // Get selected car
        Car selectedCar = availableCars.get(selection - 1);
        double hourlyRate = selectedCar.getHourlyRate();

        // Calculate minimum advance payment (1 hour)
        double minimumAdvance = hourlyRate;

        // Get advance payment from user
        System.out.println("\nCar details:");
        System.out.println("Model: " + selectedCar.getModel());
        System.out.println("Category: " + selectedCar.getCategory());
        System.out.println("Hourly Rate: $" + String.format("%.2f", hourlyRate));
        System.out.println("Minimum Advance Payment (1 hour): $" + String.format("%.2f", minimumAdvance));

        System.out.print("\nEnter advance payment amount (minimum $" + String.format("%.2f", minimumAdvance) + "): $");
        double advancePayment = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        if (advancePayment < minimumAdvance) {
            System.out.println("Advance payment must be at least $" + String.format("%.2f", minimumAdvance));
            return;
        }

        // Create billing record
        Billing billing = new Billing(currentCustomer.getPhoneNumber(), selectedCar.getCarId(), hourlyRate, advancePayment);
        billingService.addBilling(billing);

        // Create travel history
        TravelHistory travelHistory = new TravelHistory(currentCustomer.getPhoneNumber(), selectedCar.getCarId());
        currentCustomer.addTravelHistory(travelHistory);

        // Mark car as unavailable
        selectedCar.setAvailability(false);

        // Save changes
        billingService.saveBillingsToFile();
        customerServices.saveCustomersToFile();
        carServices.saveCarsToFile();

        System.out.println("\nCar rented successfully!");
        System.out.println("Bill ID: " + billing.getBillId());
        System.out.println("Advance Payment: $" + String.format("%.2f", advancePayment));
        System.out.println("You will be charged $" + String.format("%.2f", hourlyRate) + " per hour when you return the car.");
    }

    private void returnCarWithBilling() {
        Customer currentCustomer = customerServices.getCurrentCustomer();
        if (currentCustomer == null) {
            System.out.println("Please login first.");
            return;
        }

        // Get active rentals for the current customer
        List<TravelHistory> activeRentals = new java.util.ArrayList<>();
        for (TravelHistory history : currentCustomer.getTravelHistories()) {
            if (history.isActive()) {
                activeRentals.add(history);
            }
        }

        if (activeRentals.isEmpty()) {
            System.out.println("You don't have any active car rentals.");
            return;
        }

        System.out.println("\nYour Active Rentals:");
        System.out.println("-------------------");
        for (int i = 0; i < activeRentals.size(); i++) {
            TravelHistory rental = activeRentals.get(i);
            Car car = carServices.getCarById(rental.getCarId());
            String carDetails = car != null ? car.getModel() + " (Category: " + car.getCategory() + ")" : "Unknown";

            System.out.println((i + 1) + ". Car ID: " + rental.getCarId() + " - " + carDetails);
            System.out.println("   Rented on: " + rental.getRentTime());
        }

        // Get user selection
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nSelect a car to return (1-" + activeRentals.size() + "): ");
        int selection = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (selection < 1 || selection > activeRentals.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        // Get selected rental
        TravelHistory selectedRental = activeRentals.get(selection - 1);
        String carId = selectedRental.getCarId();

        // Process billing
        double amountDue = billingService.processCarsReturn(carId);

        if (amountDue >= 0) {
            Car car = carServices.getCarById(carId);

            System.out.println("\nReturn Summary:");
            System.out.println("--------------");
            if (car != null) {
                System.out.println("Car: " + car.getModel() + " (ID: " + carId + ")");
                System.out.println("Hourly Rate: $" + String.format("%.2f", car.getHourlyRate()));
            }
            System.out.println("Rental Duration: From " + selectedRental.getRentTime() + " to now");
            System.out.println("Remaining amount to be paid: $" + String.format("%.2f", amountDue));

            System.out.print("\nConfirm payment (y/n)? ");
            String confirm = scanner.nextLine().toLowerCase();

            if (confirm.equals("y") || confirm.equals("yes")) {
                // Return the car
                carServices.returnCar(carId);
                System.out.println("Payment processed and car returned successfully.");

                // Mark the billing as paid
                billingService.markBillingAsPaid(carId);
                billingService.saveBillingsToFile();
            } else {
                System.out.println("Payment not confirmed. Car return process canceled.");
            }
        } else {
            System.out.println("No active billing found for this car.");
        }
    }

    private void viewMyBillingHistory() {
        Customer currentCustomer = customerServices.getCurrentCustomer();
        if (currentCustomer == null) {
            System.out.println("Please login first.");
            return;
        }

        String phoneNumber = currentCustomer.getPhoneNumber();
        List<Billing> billings = billingService.getBillingHistoryForCustomer(phoneNumber);

        if (billings.isEmpty()) {
            System.out.println("You don't have any billing history.");
            return;
        }

        System.out.println("\nYour Billing History:");
        System.out.println("--------------------");

        double totalSpent = 0.0;
        for (Billing billing : billings) {
            Car car = carServices.getCarById(billing.getCarId());
            String carModel = car != null ? car.getModel() : "Unknown";

            System.out.println("Bill ID: " + billing.getBillId());
            System.out.println("Car: " + carModel + " (ID: " + billing.getCarId() + ")");
            System.out.println("Hourly Rate: $" + String.format("%.2f", billing.getHourlyRate()));
            System.out.println("Advance Payment: $" + String.format("%.2f", billing.getAdvancePayment()));
            System.out.println("Final Amount: $" + String.format("%.2f", billing.getFinalAmount()));
            System.out.println("Billing Time: " + billing.getBillingTime());
            System.out.println("Status: " + (billing.isPaid() ? "Paid" : "Pending"));
            System.out.println("----------");

            if (billing.isPaid()) {
                totalSpent += billing.getFinalAmount();
            }
        }

        System.out.println("\nTotal Amount Spent: $" + String.format("%.2f", totalSpent));
    }
}