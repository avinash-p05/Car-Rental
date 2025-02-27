package org.example.structures;
import org.example.models.Car;
import org.example.models.Customer;

import java.util.*;

/**
 * Graph implementation to represent relationships between customers and cars
 * This can be used for analyzing rental patterns and making recommendations
 */
public class RentalGraph {
    // Using adjacency lists to represent the graph
    private Map<String, List<RentalEdge>> customerToCarEdges; // Customers as nodes connected to cars
    private Map<String, List<RentalEdge>> carToCustomerEdges; // Cars as nodes connected to customers

    public RentalGraph() {
        this.customerToCarEdges = new HashMap<>();
        this.carToCustomerEdges = new HashMap<>();
    }

    // Edge in the graph representing a rental relationship
    public static class RentalEdge {
        private String nodeId; // Connected node (car or customer)
        private Date rentalDate;
        private int rentalDuration; // in hours

        public RentalEdge(String nodeId, Date rentalDate, int rentalDuration) {
            this.nodeId = nodeId;
            this.rentalDate = rentalDate;
            this.rentalDuration = rentalDuration;
        }

        public String getNodeId() {
            return nodeId;
        }

        public Date getRentalDate() {
            return rentalDate;
        }

        public int getRentalDuration() {
            return rentalDuration;
        }
    }

    // Add a rental relationship to the graph
    public void addRentalRelationship(String customerId, String carId, Date rentalDate, int duration) {
        // Add edge from customer to car
        if (!customerToCarEdges.containsKey(customerId)) {
            customerToCarEdges.put(customerId, new LinkedList<>());
        }
        customerToCarEdges.get(customerId).add(new RentalEdge(carId, rentalDate, duration));

        // Add edge from car to customer
        if (!carToCustomerEdges.containsKey(carId)) {
            carToCustomerEdges.put(carId, new LinkedList<>());
        }
        carToCustomerEdges.get(carId).add(new RentalEdge(customerId, rentalDate, duration));
    }

    // Find all cars rented by a customer
    public List<String> getCarsRentedByCustomer(String customerId) {
        List<String> rentedCars = new ArrayList<>();
        if (customerToCarEdges.containsKey(customerId)) {
            for (RentalEdge edge : customerToCarEdges.get(customerId)) {
                rentedCars.add(edge.getNodeId());
            }
        }
        return rentedCars;
    }

    // Find all customers who rented a specific car
    public List<String> getCustomersWhoRentedCar(String carId) {
        List<String> customers = new ArrayList<>();
        if (carToCustomerEdges.containsKey(carId)) {
            for (RentalEdge edge : carToCustomerEdges.get(carId)) {
                customers.add(edge.getNodeId());
            }
        }
        return customers;
    }

    // Find most popular cars (most frequently rented)
    public List<Map.Entry<String, Integer>> getMostPopularCars(int limit) {
        Map<String, Integer> carRentalCount = new HashMap<>();

        for (Map.Entry<String, List<RentalEdge>> entry : carToCustomerEdges.entrySet()) {
            carRentalCount.put(entry.getKey(), entry.getValue().size());
        }

        // Sort cars by rental count
        List<Map.Entry<String, Integer>> sortedCars = new LinkedList<>(carRentalCount.entrySet());
        sortedCars.sort((a, b) -> b.getValue() - a.getValue());

        // Return top N cars
        return sortedCars.subList(0, Math.min(limit, sortedCars.size()));
    }

    // Recommend cars to a customer based on similar customer preferences
    public Set<String> recommendCarsForCustomer(String customerId) {
        Set<String> recommendations = new HashSet<>();

        // Get all cars this customer has rented
        List<String> rentedCars = getCarsRentedByCustomer(customerId);

        System.out.println("1");
        // Find customers with similar preferences
        Set<String> similarCustomers = new HashSet<>();
        for (String carId : rentedCars) {
            List<String> customersWhoRentedThisCar = getCustomersWhoRentedCar(carId);
            for (String customer : customersWhoRentedThisCar) {
                if (!customer.equals(customerId)) {
                    similarCustomers.add(customer);
                }
            }
        }
        System.out.println("2");
        // Get cars rented by similar customers but not by this customer
        Set<String> rentedCarsSet = new HashSet<>(rentedCars);
        for (String customer : similarCustomers) {
            List<String> otherCustomerCars = getCarsRentedByCustomer(customer);
            for (String car : otherCustomerCars) {
                if (!rentedCarsSet.contains(car)) {
                    recommendations.add(car);
                }
            }
        }
        System.out.println("3");
        return recommendations;
    }
}
