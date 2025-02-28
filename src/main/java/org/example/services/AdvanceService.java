package org.example.services;

import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;
import org.example.structures.DataCache;
import org.example.structures.RentalGraph;

import java.util.*;

public class AdvanceService {
    private static AdvanceService instance;
    private RentalGraph rentalGraph;
    private DataCache<String, Car> carCache;
    private DataCache<String, Customer> customerCache;
    
    private AdvanceService() {
        this.rentalGraph = new RentalGraph();
        this.carCache = new DataCache<>(100);
        this.customerCache = new DataCache<>(50);
        buildRentalGraphFromHistory();
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
        System.out.println("Your Recommended Cars - ");
        System.out.println("----------------");


        for (String carId : recommendedCarIds) {
            Car car = carCache.get(carId);
            if (car == null) {
                car = CarServices.getInstance().getCarById(carId);
                if (car != null) {
                    carCache.put(carId, car);
                }
            }
            if (car != null  ) {
                System.out.println("Car ID: " + car.getCarId() + " | Model: " + car.getModel() + " | Category: " + car.getCategory());
                System.out.println();
            }
        }
    }

    public void displayMostPopularCars(int limit) {
        List<Map.Entry<String, Integer>> popularCarIds = rentalGraph.getMostPopularCars(limit);
        System.out.println("Top " + limit + " Most Popular Cars:");
        System.out.println("----------------");

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
                System.out.println();
            }

        }
        System.out.println();
    }

}
