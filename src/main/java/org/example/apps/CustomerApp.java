package org.example.apps;

import org.example.models.Customer;
import org.example.services.CarServices;
import org.example.services.CustomerServices;

import java.util.Scanner;

public class CustomerApp {
    private final CarServices carServices = CarServices.getInstance();
    private final CustomerServices customerServices = CustomerServices.getInstance();

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
            System.out.println("6. Exit");
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

                    boolean status = customerServices.register(phoneNumber, name, password);
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
                        if (customerServices.rentCar(name)) {
                            System.out.println("Successfully Rented the car!!");
                        }
                    }
                    break;

                case 4:
                    customerServices.viewTravelHistory();
                    break;

                case 5:
                    if (name.isEmpty()) {
                        System.out.println("Please login first to return a car.");
                    } else {
                        System.out.print("Enter car ID to return: ");
                        String carId = scanner.nextLine();
                        carServices.returnCar(carId);
                    }
                    break;

                case 6:
                    customerServices.saveCustomersToFile();
                    carServices.saveCarsToFile();
                    exit = true;
                    System.out.println("Exiting the customer menu. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        return 0;
    }
}
