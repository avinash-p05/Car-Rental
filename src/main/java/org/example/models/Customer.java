package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String phoneNumber;
    private String name;
    private String password;
    private List<TravelHistory> travelHistories;

    public Customer(String phoneNumber, String name, String password) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.password = password;
        this.travelHistories = new ArrayList<>();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<TravelHistory> getTravelHistories() {
        return travelHistories;
    }

    public void addTravelHistory(TravelHistory travelHistory) {
        this.travelHistories.add(travelHistory);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
