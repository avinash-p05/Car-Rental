package org.example.services;

import org.example.models.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CSVFileService {
    private static final String DATA_DIRECTORY = "data";
    private static final String CUSTOMERS_FILE = "customers.csv";
    private static final String CARS_FILE = "cars.csv";
    private static final String TRAVEL_HISTORY_FILE = "travel_history.csv";
    private static final String BILLINGS_FILE = "billings.csv"; // New file for billings
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static CSVFileService instance;

    private CSVFileService() {
        // Create data directory if it doesn't exist
        try {
            Path dataDir = Paths.get(DATA_DIRECTORY);
            if (!Files.exists(dataDir)) {
                Files.createDirectory(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CSVFileService getInstance() {
        if (instance == null) {
            instance = new CSVFileService();
        }
        return instance;
    }

    // Load customers from CSV file
    public List<Customer> loadCustomers() {
        // Existing code remains the same
        List<Customer> customers = new ArrayList<>();
        Path customerFile = Paths.get(DATA_DIRECTORY, CUSTOMERS_FILE);

        if (!Files.exists(customerFile)) {
            return customers;
        }

        try (BufferedReader reader = Files.newBufferedReader(customerFile)) {
            // Skip header line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String phoneNumber = parts[0];
                    String name = parts[1];
                    String password = parts[2];
                    customers.add(new Customer(phoneNumber, name, password));
                }
            }

            // Load travel histories for each customer
            loadTravelHistories(customers);

            return customers;
        } catch (IOException e) {
            System.err.println("Error loading customers from CSV: " + e.getMessage());
            return customers;
        }
    }

    // Load cars from CSV file - updated to include hourly rate and category
    public List<Car> loadCars() {
        List<Car> cars = new ArrayList<>();
        Path carFile = Paths.get(DATA_DIRECTORY, CARS_FILE);

        if (!Files.exists(carFile)) {
            return cars;
        }

        try (BufferedReader reader = Files.newBufferedReader(carFile)) {
            // Skip header line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String carId = parts[0];
                    String model = parts[1];
                    boolean availability = Boolean.parseBoolean(parts[2]);

                    // Check if we have hourly rate and category in the file
                    double hourlyRate = 10.0; // Default
                    String category = "Standard"; // Default

                    if (parts.length >= 4) {
                        try {
                            hourlyRate = Double.parseDouble(parts[3]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid hourly rate for car " + carId + ": " + parts[3]);
                        }
                    }

                    if (parts.length >= 5) {
                        category = parts[4];
                    }

                    cars.add(new Car(carId, model, availability, hourlyRate, category));
                }
            }
            return cars;
        } catch (IOException e) {
            System.err.println("Error loading cars from CSV: " + e.getMessage());
            return cars;
        }
    }

    // Load billings from CSV file
    public List<Billing> loadBillings() {
        List<Billing> billings = new ArrayList<>();
        Path billingFile = Paths.get(DATA_DIRECTORY, BILLINGS_FILE);

        if (!Files.exists(billingFile)) {
            return billings;
        }

        try (BufferedReader reader = Files.newBufferedReader(billingFile)) {
            // Skip header line
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    try {
                        String billId = parts[0];
                        String customerId = parts[1];
                        String carId = parts[2];
                        double hourlyRate = Double.parseDouble(parts[3]);
                        double advancePayment = Double.parseDouble(parts[4]);
                        double finalAmount = Double.parseDouble(parts[5]);
                        LocalDateTime billingTime = LocalDateTime.parse(parts[6], DATE_FORMATTER);
                        boolean isPaid = Boolean.parseBoolean(parts[7]);

                        Billing billing = new Billing(billId, customerId, carId, hourlyRate,
                                advancePayment, finalAmount, billingTime, isPaid);
                        billings.add(billing);
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Error parsing billing record: " + line);
                    }
                }
            }
            return billings;
        } catch (IOException e) {
            System.err.println("Error loading billings from CSV: " + e.getMessage());
            return billings;
        }
    }

    // Save cars to CSV file - updated to include hourly rate and category
    public void saveCars(List<Car> cars) {
        Path carFile = Paths.get(DATA_DIRECTORY, CARS_FILE);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(carFile))) {
            // Write header
            writer.println("CarId,Model,Available,HourlyRate,Category");

            // Write car data
            for (Car car : cars) {
                writer.printf("%s,%s,%s,%.2f,%s%n",
                        car.getCarId(),
                        car.getModel(),
                        car.isAvailable(),
                        car.getHourlyRate(),
                        car.getCategory());
            }
        } catch (IOException e) {
            System.err.println("Error saving cars to CSV: " + e.getMessage());
        }
    }

    // Save billings to CSV file
    public void saveBillings(List<Billing> billings) {
        Path billingFile = Paths.get(DATA_DIRECTORY, BILLINGS_FILE);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(billingFile))) {
            // Write header
            writer.println("BillId,CustomerId,CarId,HourlyRate,AdvancePayment,FinalAmount,BillingTime,IsPaid");

            // Write billing data
            for (Billing billing : billings) {
                writer.printf("%s,%s,%s,%.2f,%.2f,%.2f,%s,%s%n",
                        billing.getBillId(),
                        billing.getCustomerId(),
                        billing.getCarId(),
                        billing.getHourlyRate(),
                        billing.getAdvancePayment(),
                        billing.getFinalAmount(),
                        billing.getBillingTime().format(DATE_FORMATTER),
                        billing.isPaid());
            }
        } catch (IOException e) {
            System.err.println("Error saving billings to CSV: " + e.getMessage());
        }
    }

    // Load travel histories from CSV file
    private void loadTravelHistories(List<Customer> customers) {
        Path historyFile = Paths.get(DATA_DIRECTORY, TRAVEL_HISTORY_FILE);

        if (!Files.exists(historyFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(historyFile)) {
            // Skip header line
            reader.readLine();

            Map<String, Customer> customerMap = customers.stream()
                    .collect(Collectors.toMap(Customer::getPhoneNumber, c -> c));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String customerId = parts[0];
                    String carId = parts[1];
                    LocalDateTime rentTime = LocalDateTime.parse(parts[2], DATE_FORMATTER);
                    LocalDateTime returnTime = parts[3].equals("Active") ?
                            null : LocalDateTime.parse(parts[3], DATE_FORMATTER);

                    Customer customer = customerMap.get(customerId);
                    if (customer != null) {
                        TravelHistory history = new TravelHistory(customerId, carId, rentTime, returnTime);
                        customer.addTravelHistory(history);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading travel histories from CSV: " + e.getMessage());
        }
    }

    // Save customers to CSV file
    public void saveCustomers(List<Customer> customers) {
        Path customerFile = Paths.get(DATA_DIRECTORY, CUSTOMERS_FILE);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(customerFile))) {
            // Write header
            writer.println("PhoneNumber,Name,Password");

            // Write customer data
            for (Customer customer : customers) {
                writer.printf("%s,%s,%s%n",
                        customer.getPhoneNumber(),
                        customer.getName(),
                        customer.getPassword());
            }

            // Save travel histories
            saveTravelHistories(customers);

        } catch (IOException e) {
            System.err.println("Error saving customers to CSV: " + e.getMessage());
        }
    }

    // Save cars to CSV file
//    public void saveCars(List<Car> cars) {
//        Path carFile = Paths.get(DATA_DIRECTORY, CARS_FILE);
//
//        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(carFile))) {
//            // Write header
//            writer.println("CarId,Model,Available");
//
//            // Write car data
//            for (Car car : cars) {
//                writer.printf("%s,%s,%s%n",
//                        car.getCarId(),
//                        car.getModel(),
//                        car.isAvailable());
//            }
//        } catch (IOException e) {
//            System.err.println("Error saving cars to CSV: " + e.getMessage());
//        }
//    }

    // Save travel histories to CSV file
    private void saveTravelHistories(List<Customer> customers) {
        Path historyFile = Paths.get(DATA_DIRECTORY, TRAVEL_HISTORY_FILE);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(historyFile))) {
            // Write header
            writer.println("CustomerId,CarId,RentTime,ReturnTime");

            // Write travel history data
            for (Customer customer : customers) {
                for (TravelHistory history : customer.getTravelHistories()) {
                    writer.printf("%s,%s,%s,%s%n",
                            history.getCustomerId(),
                            history.getCarId(),
                            history.getRentTime().format(DATE_FORMATTER),
                            history.getReturnTime() != null ?
                                    history.getReturnTime().format(DATE_FORMATTER) : "Active");
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving travel histories to CSV: " + e.getMessage());
        }
    }

    // Create backup of all data
    public void createBackup(String backupName) {
        String backupDir = DATA_DIRECTORY + "/backups/" + backupName + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try {
            // Create backup directory
            Files.createDirectories(Paths.get(backupDir));

            // Copy all data files to backup directory
            Files.copy(Paths.get(DATA_DIRECTORY, CUSTOMERS_FILE),
                    Paths.get(backupDir, CUSTOMERS_FILE));
            Files.copy(Paths.get(DATA_DIRECTORY, CARS_FILE),
                    Paths.get(backupDir, CARS_FILE));
            Files.copy(Paths.get(DATA_DIRECTORY, TRAVEL_HISTORY_FILE),
                    Paths.get(backupDir, TRAVEL_HISTORY_FILE));

            System.out.println("Backup created successfully at: " + backupDir);
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }

    // Restore from backup
    public boolean restoreFromBackup(String backupPath) {
        try {
            // Copy all backup files to main data directory
            Files.copy(Paths.get(backupPath, CUSTOMERS_FILE),
                    Paths.get(DATA_DIRECTORY, CUSTOMERS_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(backupPath, CARS_FILE),
                    Paths.get(DATA_DIRECTORY, CARS_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(backupPath, TRAVEL_HISTORY_FILE),
                    Paths.get(DATA_DIRECTORY, TRAVEL_HISTORY_FILE),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Restore completed successfully from: " + backupPath);
            return true;
        } catch (IOException e) {
            System.err.println("Error restoring from backup: " + e.getMessage());
            return false;
        }
    }

    // Helper method to escape CSV values properly
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (needsQuoting) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}