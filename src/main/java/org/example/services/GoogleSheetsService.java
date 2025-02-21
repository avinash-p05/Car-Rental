package org.example.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.models.Car;
import org.example.models.Customer;
import org.example.models.TravelHistory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleSheetsService {
    private static final String APPLICATION_NAME = "Car Rental App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String SPREADSHEET_ID = "YOUR_SPREADSHEET_ID"; // Replace with your Google Sheet ID

    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static GoogleSheetsService instance;
    private final Sheets sheetsService;

    private GoogleSheetsService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static GoogleSheetsService getInstance() {
        if (instance == null) {
            try {
                instance = new GoogleSheetsService();
            } catch (Exception e) {
                System.err.println("Error initializing GoogleSheetsService: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return instance;
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets
        InputStream in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // Load customers from Google Sheets
    public List<Customer> loadCustomers() {
        try {
            List<Customer> customers = new ArrayList<>();
            String range = "Customers!A2:C";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values != null && !values.isEmpty()) {
                for (List<Object> row : values) {
                    if (row.size() >= 3) {
                        String phoneNumber = (String) row.get(0);
                        String name = (String) row.get(1);
                        String password = (String) row.get(2);
                        customers.add(new Customer(phoneNumber, name, password));
                    }
                }
            }

            // Load travel histories for each customer
            loadTravelHistories(customers);

            return customers;
        } catch (IOException e) {
            System.err.println("Error loading customers from Google Sheets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Load cars from Google Sheets
    public List<Car> loadCars() {
        try {
            List<Car> cars = new ArrayList<>();
            String range = "Cars!A2:C";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values != null && !values.isEmpty()) {
                for (List<Object> row : values) {
                    if (row.size() >= 3) {
                        String carId = (String) row.get(0);
                        String model = (String) row.get(1);
                        boolean availability = Boolean.parseBoolean((String) row.get(2));
                        cars.add(new Car(carId, model, availability));
                    }
                }
            }
            return cars;
        } catch (IOException e) {
            System.err.println("Error loading cars from Google Sheets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Load travel histories from Google Sheets
    private void loadTravelHistories(List<Customer> customers) {
        try {
            String range = "TravelHistory!A2:D";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values != null && !values.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                Map<String, Customer> customerMap = customers.stream()
                        .collect(Collectors.toMap(Customer::getPhoneNumber, c -> c));

                for (List<Object> row : values) {
                    if (row.size() >= 3) {
                        String customerId = (String) row.get(0);
                        String carId = (String) row.get(1);
                        LocalDateTime rentTime = LocalDateTime.parse((String) row.get(2), formatter);
                        LocalDateTime returnTime = row.size() >= 4 && !row.get(3).equals("Active") ?
                                LocalDateTime.parse((String) row.get(3), formatter) : null;

                        Customer customer = customerMap.get(customerId);
                        if (customer != null) {
                            TravelHistory history = new TravelHistory(customerId, carId, rentTime, returnTime);
                            customer.addTravelHistory(history);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading travel histories from Google Sheets: " + e.getMessage());
        }
    }

    // Save customers to Google Sheets
//    public void saveCustomers(List<Customer> customers) {
//        try {
//            // Clear existing data
//            String range = "Customers!A2:C";
//            sheetsService.spreadsheets().values().clear(SPREADSHEET_ID, range)
//                    .setRequestBody(new com.google.api.services.sheets.v4.model.ClearValuesRequest())
//                    .execute();
//
//            // Prepare new data
//            List<List<Object>> values = new ArrayList<>();
//            for (Customer customer : customers) {
//                List<Object> row = new ArrayList<>();
//                row.add(customer.getPhoneNumber());
//                row.add(customer.getName());
//                row.add(customer.getPassword());
//                values.add(row);
//            }
//
//            // Write new data
//            ValueRange body = new ValueRange().setValues(values);
//            sheetsService.spreadsheets().values().update(SPREADSHEET_ID, range, body)
//                    .setValueInputOption("RAW")
//                    .execute();
//
//            // Save travel histories
//            saveTravelHistories(customers);
//
//        } catch (IOException e) {
//            System.err.println("Error saving customers to Google Sheets: " + e.getMessage());
//        }
//    }
//
//    // Save cars to Google Sheets
//    public void saveCars(List<Car> cars) {
//        try {
//            // Clear existing data
//            String range = "Cars!A2:C";
//            sheetsService.spreadsheets().values().clear(SPREADSHEET_ID, range)
//                    .setRequestBody(new com.google.api.services.sheets.v4.model.ClearValuesRequest())
//                    .execute();
//
//            // Prepare new data
//            List<List<Object>> values = new ArrayList<>();
//            for (Car car : cars) {
//                List<Object> row = new ArrayList<>();
//                row.add(car.getCarId());
//                row.add(car.getModel());
//                row.add(String.valueOf(car.isAvailable()));
//                values.add(row);
//            }
//
//            // Write new data
//            ValueRange body = new ValueRange().setValues(values);
//            sheetsService.spreadsheets().values().update(SPREADSHEET_ID, range, body)
//                    .setValueInputOption("RAW")
//                    .execute();
//
//        } catch (IOException e) {
//            System.err.println("Error saving cars to Google Sheets: " + e.getMessage());
//        }
//    }
//
//    // Save travel histories to Google Sheets
//    private void saveTravelHistories(List<Customer> customers) {
//        try {
//            // Clear existing data
//            String range = "TravelHistory!A2:D";
//            sheetsService.spreadsheets().values().clear(SPREADSHEET_ID, range)
//                    .setRequestBody(new com.google.api.services.sheets.v4.model.ClearValuesRequest())
//                    .execute();
//
//            // Prepare new data
//            List<List<Object>> values = new ArrayList<>();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//            for (Customer customer : customers) {
//                for (TravelHistory history : customer.getTravelHistories()) {
//                    List<Object> row = new ArrayList<>();
//                    row.add(history.getCustomerId());
//                    row.add(history.getCarId());
//                    row.add(history.getRentTime().format(formatter));
//                    row.add(history.getReturnTime() != null ?
//                            history.getReturnTime().format(formatter) : "Active");
//                    values.add(row);
//                }
//            }
//
//            // Write new data
//            ValueRange body = new ValueRange().setValues(values);
//            sheetsService.spreadsheets().values().update(SPREADSHEET_ID, range, body)
//                    .setValueInputOption("RAW")
//                    .execute();
//
//        } catch (IOException e) {
//            System.err.println("Error saving travel histories to Google Sheets: " + e.getMessage());
//        }
//    }
}
