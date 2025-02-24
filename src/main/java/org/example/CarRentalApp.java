package org.example;

import org.example.apps.AdminApp;
import org.example.apps.CustomerApp;

import java.util.Scanner;

public class CarRentalApp {
    private static final CustomerApp customerApp = new CustomerApp();
    private static final AdminApp adminApp = new AdminApp();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\nCar Rental System");
            System.out.println("1. Admin Login");
            System.out.println("2. User Register/Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int firstChoice = scanner.nextInt();
            scanner.nextLine();

            switch (firstChoice) {
                case 1:
                    System.out.print("Enter your username: ");
                    String pn = scanner.nextLine();
                    System.out.print("Enter the password: ");
                    String pw = scanner.nextLine();
                    if (adminApp.login(pn, pw)) {
                        System.out.println("Login Successful, Welcome Admin");
                        adminApp.displayMenu();
                    } else {
                        System.out.println("Invalid admin credentials!");
                    }
                    break;

                case 2:
                    customerApp.displayMenu();
                    break;

                case 3:
                    System.out.println("Saving data and exiting...");
                    adminApp.saveAndExit();
                    exit = true;
                    break;

                default:
                    System.out.println("Invalid choice!!");
                    break;
            }
        }

        scanner.close();
    }
}
