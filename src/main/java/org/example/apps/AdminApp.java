package org.example.apps;

import org.example.models.Billing;
import org.example.models.Car;
import org.example.services.*;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class AdminApp {
    private final CarServices carServices = CarServices.getInstance();
    private final AdminServices adminServices = AdminServices.getInstance();
    private final CustomerServices customerServices = CustomerServices.getInstance();
    private final BillingService billingService = BillingService.getInstance();

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
            System.out.println("9. View All Billing Records");
            System.out.println("10. View Billing History for Customer");
            System.out.println("11. Exit");
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
                    System.out.print("Enter car category (Economy/Standard/Premium/Luxury): ");
                    String category = scanner.nextLine();
                    System.out.print("Enter hourly rate: $");
                    double hourlyRate = scanner.nextDouble();
                    scanner.nextLine(); // consume newline

                    carServices.addCar(carId, model, category, hourlyRate);
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

                    // Process billing first
                    double amountDue = billingService.processCarsReturn(carIdToReturn);

                    if (amountDue >= 0) {
                        System.out.println("Remaining amount to be paid: $" + String.format("%.2f", amountDue));
                        System.out.print("Confirm payment received (y/n)? ");
                        String confirm = scanner.nextLine().toLowerCase();

                        if (confirm.equals("y") || confirm.equals("yes")) {
                            // Now return the car
                            carServices.returnCar(carIdToReturn);
                            System.out.println("Payment processed and car returned successfully.");
                        } else {
                            System.out.println("Payment not confirmed. Car return process canceled.");
                        }
                    } else {
                        System.out.println("No active billing found for this car.");
                    }
                    break;

                case 9:
                    viewAllBillingRecords();
                    break;

                case 10:
                    System.out.print("Enter customer phone number: ");
                    String customerPhone = scanner.nextLine();
                    viewBillingHistoryForCustomer(customerPhone);
                    break;

                case 11:
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

    private void viewAllBillingRecords() {
        List<Billing> allBillings = billingService.getAllBillings();

        if (allBillings.isEmpty()) {
            System.out.println("No billing records found.");
            return;
        }

        System.out.println("\n===== ALL BILLING RECORDS =====");
        for (Billing billing : allBillings) {
            printBillingDetails(billing);
        }
        System.out.println("===============================");
    }

    private void viewBillingHistoryForCustomer(String phoneNumber) {
        List<Billing> customerBillings = billingService.getBillingHistoryForCustomer(phoneNumber);

        if (customerBillings.isEmpty()) {
            System.out.println("No billing records found for customer with phone number: " + phoneNumber);
            return;
        }

        System.out.println("\n===== BILLING HISTORY FOR CUSTOMER =====");
        System.out.println("Customer: " + customerServices.getCustomerNameByPhone(phoneNumber));
        System.out.println("Phone: " + phoneNumber);

        double totalSpent = 0.0;
        for (Billing billing : customerBillings) {
            printBillingDetails(billing);
            if (billing.isPaid()) {
                totalSpent += billing.getFinalAmount();
            }
        }

        System.out.println("\nTotal amount spent: $" + String.format("%.2f", totalSpent));
        System.out.println("========================================");
    }

    private void printBillingDetails(Billing billing) {
        Car car = carServices.getCarById(billing.getCarId());
        String customerName = customerServices.getCustomerNameByPhone(billing.getCustomerId());

        System.out.println("\nBill ID: " + billing.getBillId());
        System.out.println("Customer: " + customerName + " (" + billing.getCustomerId() + ")");
        System.out.println("Car: " + (car != null ? car.getModel() : "Unknown") + " (ID: " + billing.getCarId() + ")");
        System.out.println("Hourly Rate: $" + String.format("%.2f", billing.getHourlyRate()));
        System.out.println("Advance Payment: $" + String.format("%.2f", billing.getAdvancePayment()));
        System.out.println("Final Amount: $" + String.format("%.2f", billing.getFinalAmount()));
        System.out.println("Billing Time: " + billing.getBillingTime());
        System.out.println("Status: " + (billing.isPaid() ? "Paid" : "Pending"));
        System.out.println("----------");
    }

    public boolean login(String pn, String pw) {
        return Objects.equals(pn, "avinash") && Objects.equals(pw, "123");
    }

    public void saveAndExit() {
        carServices.makeExit();
        billingService.saveBillingsToFile();
    }
}