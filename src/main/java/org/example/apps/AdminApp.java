package org.example.apps;

import org.example.services.AdminServices;
import org.example.services.CarServices;
import org.example.services.CustomerServices;

import java.util.Objects;
import java.util.Scanner;

public class AdminApp {
    private final CarServices carServices = CarServices.getInstance();
    private final AdminServices adminServices = AdminServices.getInstance();
    private final CustomerServices customerServices = CustomerServices.getInstance();

    public int displayMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n*** Admin Menu ***");
            System.out.println("1. View Stats");
            System.out.println("2. Add a new Car");
            System.out.println("3. View all Cars");
            System.out.println("4. View all Customers");
            System.out.println("5. View Customers by phone number");
            System.out.println("6. View all Travel History");
            System.out.println("7. View Travel History of a Customer");
            System.out.println("8. Return a Car");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    adminServices.viewStats();
                    break;

                case 2:
                    System.out.println("---Fill out the Car Details---");
                    System.out.print("Enter car id: ");
                    String carId = scanner.nextLine();
                    System.out.print("Enter car model: ");
                    String model = scanner.nextLine();
                    carServices.addCar(carId, model);
                    break;

                case 3:
                    carServices.viewAllCars();
                    break;

                case 4:
                    customerServices.viewAllCustomers();
                    break;

                case 5:
                    System.out.print("Enter phone number: ");
                    String phoneNumber = scanner.nextLine();
                    customerServices.viewCustomerByPhoneNumber(phoneNumber);
                    break;

                case 6:
                    carServices.viewAllTravelHistory();
                    break;

                case 7:
                    System.out.print("Enter customer phone number: ");
                    String customerPhoneNumber = scanner.nextLine();
                    carServices.viewTravelHistoryOfCustomer(customerPhoneNumber);
                    break;

                case 8:
                    System.out.print("Enter car ID to return: ");
                    String carIdToReturn = scanner.nextLine();
                    carServices.returnCar(carIdToReturn);
                    break;

                case 9:
                    saveAndExit();
                    exit = true;
                    System.out.println("Exiting the admin menu.");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        return 0;
    }

    public boolean login(String pn, String pw) {
        return Objects.equals(pn, "9980385537") && Objects.equals(pw, "123ok");
    }

    public void saveAndExit() {
        carServices.makeExit();
    }
}
