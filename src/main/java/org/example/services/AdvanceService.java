package org.example.services;

import org.checkerframework.checker.units.qual.C;
import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;
import org.example.structures.CarCategoryList;
import org.example.structures.DataCache;
import org.example.structures.RentalGraph;

import java.util.*;

public class AdvanceService {
    private static AdvanceService instance;
    private RentalGraph rentalGraph;
    private DataCache<String, Car> carCache;
    private DataCache<String, Customer> customerCache;
    private CarCategoryList categoryList;
    private Set<String> premiumCustomers;
    private Set<String> blacklistedCustomers;

    private AdvanceService() {
        this.rentalGraph = new RentalGraph();
        this.carCache = new DataCache<>(100);
        this.customerCache = new DataCache<>(50);
        this.categoryList = new CarCategoryList();
        this.premiumCustomers = new HashSet<>();
        this.blacklistedCustomers = new HashSet<>();
        initializeCategories();
        buildRentalGraphFromHistory();
    }

    private void initializeCategories() {
        categoryList.addCategory("Economy", 8.0);
        categoryList.addCategory("Standard", 10.0);
        categoryList.addCategory("Premium", 15.0);
        categoryList.addCategory("Luxury", 25.0);
    }

    public static AdvanceService getInstance() {
        if (instance == null) {
            instance = new AdvanceService();
        }
        return instance;
    }

    public void buildRentalGraphFromHistory() {
        CustomerServices customerServices = CustomerServices.getInstance();
        List<Customer> customers = customerServices.getCustomers();

        for (Customer customer : customers) {
            for (TravelHistory history : customer.getTravelHistories()) {
                if (history.getReturnTime() != null) {
                    long durationMillis = history.getReturnTime().toEpochSecond(java.time.ZoneOffset.UTC) -
                            history.getRentTime().toEpochSecond(java.time.ZoneOffset.UTC);
                    int durationHours = (int) (durationMillis / 3600);

                    rentalGraph.addRentalRelationship(
                            customer.getPhoneNumber(),
                            history.getCarId(),
                            Date.from(history.getRentTime().toInstant(java.time.ZoneOffset.UTC)),
                            durationHours
                    );

                    customerCache.put(customer.getPhoneNumber(), customer);
                    Car car = CarServices.getInstance().getCarById(history.getCarId());
                    if (car != null) {
                        carCache.put(car.getCarId(), car);
                    }
                }
            }
        }
    }

    public void displayCarRecommendationsForCustomer(String customerId) {
        Set<String> recommendedCarIds = rentalGraph.recommendCarsForCustomer(customerId);
        System.out.println("Recommended Cars for Customer ID: " + customerId);

        for (String carId : recommendedCarIds) {
            Car car = carCache.get(carId);
            if (car == null) {
                car = CarServices.getInstance().getCarById(carId);
                if (car != null) {
                    carCache.put(carId, car);
                }
            }
//            Car car = CarServices.getInstance().getCarById(carId);
//            System.out.println(car.getCarId());
            if (car != null  ) {
                System.out.println("Car ID: " + car.getCarId() + " | Model: " + car.getModel() + " | Category: " + car.getCategory());
            }
        }
    }

    public void displayMostPopularCars(int limit) {
        List<Map.Entry<String, Integer>> popularCarIds = rentalGraph.getMostPopularCars(limit);
        System.out.println("Top " + limit + " Most Popular Cars:");

        for (Map.Entry<String, Integer> entry : popularCarIds) {
            String carId = entry.getKey();
            Car car = carCache.get(carId);
            if (car == null) {
                car = CarServices.getInstance().getCarById(carId);
                if (car != null) {
                    carCache.put(carId, car);
                }
            }

            if (car != null) {
                System.out.println("Car ID: " + car.getCarId() + " | Model: " + car.getModel() + " | Category: " + car.getCategory() +
                        " | Rented " + entry.getValue() + " times");
            }
        }
    }

    public void addPremiumCustomer(String customerId) {
        premiumCustomers.add(customerId);
        System.out.println("Customer ID " + customerId + " added to Premium Customers.");
    }

    public void checkPremiumCustomer(String customerId) {
        System.out.println("Customer ID " + customerId + " is " + (premiumCustomers.contains(customerId) ? "a Premium Customer." : "not a Premium Customer."));
    }

    public void addBlacklistedCustomer(String customerId) {
        blacklistedCustomers.add(customerId);
        System.out.println("Customer ID " + customerId + " added to Blacklist.");
    }

    public void checkBlacklistedCustomer(String customerId) {
        System.out.println("Customer ID " + customerId + " is " + (blacklistedCustomers.contains(customerId) ? "Blacklisted." : "not Blacklisted."));
    }

    public void displayCategoryBasePrice(String category) {
        System.out.println("Base price for " + category + " category: $" + categoryList.getCategoryBasePrice(category) + " per hour.");
    }

    public void displaySuggestedUpgrade(String currentCategory) {
        System.out.println("Suggested upgrade for " + currentCategory + ": " + categoryList.getNextHigherCategory(currentCategory));
    }

    public void displayAllCarCategories() {
        System.out.println("Available Car Categories: " + Arrays.toString(categoryList.getAllCategories()));
    }

    public void addCarCategory(String category, double basePrice) {
        categoryList.addCategory(category, basePrice);
        System.out.println("New Car Category Added: " + category + " with base price $" + basePrice + " per hour.");
    }
}
