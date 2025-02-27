package org.example.structures;

import org.example.models.Car;

/**
 * Custom LinkedList implementation for managing car categories and pricing tiers
 */
public class CarCategoryList {
    private Node head;
    private int size;

    private static class Node {
        String category;
        double basePrice;
        Node next;

        Node(String category, double basePrice) {
            this.category = category;
            this.basePrice = basePrice;
            this.next = null;
        }
    }

    public CarCategoryList() {
        this.head = null;
        this.size = 0;
    }

    // Add a new category with its base price
    public void addCategory(String category, double basePrice) {
        // Check if category already exists
        if (getCategoryBasePrice(category) > 0) {
            // Update existing category
            Node current = head;
            while (current != null) {
                if (current.category.equals(category)) {
                    current.basePrice = basePrice;
                    return;
                }
                current = current.next;
            }
        }

        // Add new category
        Node newNode = new Node(category, basePrice);

        // If empty list or new category has lower base price than head
        if (head == null || head.basePrice > basePrice) {
            newNode.next = head;
            head = newNode;
        } else {
            // Find position to insert (maintain sorted order by price)
            Node current = head;
            while (current.next != null && current.next.basePrice <= basePrice) {
                current = current.next;
            }

            newNode.next = current.next;
            current.next = newNode;
        }

        size++;
    }

    // Get base price for a category
    public double getCategoryBasePrice(String category) {
        Node current = head;
        while (current != null) {
            if (current.category.equals(category)) {
                return current.basePrice;
            }
            current = current.next;
        }
        return -1; // Category not found
    }

    // Get next higher category (for upselling)
    public String getNextHigherCategory(String category) {
        Node current = head;
        Node categoryNode = null;

        // Find the node for the given category
        while (current != null) {
            if (current.category.equals(category)) {
                categoryNode = current;
                break;
            }
            current = current.next;
        }

        // If category not found or it's the highest category
        if (categoryNode == null || categoryNode.next == null) {
            return null;
        }

        return categoryNode.next.category;
    }

    // Get all categories as array
    public String[] getAllCategories() {
        String[] categories = new String[size];
        Node current = head;
        int index = 0;

        while (current != null) {
            categories[index++] = current.category;
            current = current.next;
        }

        return categories;
    }

    // Check if the list contains a category
    public boolean containsCategory(String category) {
        Node current = head;
        while (current != null) {
            if (current.category.equals(category)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Remove a category
    public boolean removeCategory(String category) {
        if (head == null) {
            return false;
        }

        if (head.category.equals(category)) {
            head = head.next;
            size--;
            return true;
        }

        Node current = head;
        while (current.next != null && !current.next.category.equals(category)) {
            current = current.next;
        }

        if (current.next != null) {
            current.next = current.next.next;
            size--;
            return true;
        }

        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}