package org.example.structures;

import org.example.models.Car;
import org.example.models.Customer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Cache implementation for frequently accessed car and customer data
 * Uses HashMap for O(1) lookups and LinkedList for LRU (Least Recently Used) functionality
 */
public class DataCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;
    private final LinkedList<K> lruList; // Track usage order for LRU eviction

    public DataCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>(capacity);
        this.lruList = new LinkedList<>();
    }

    public V get(K key) {
        if (!cache.containsKey(key)) {
            return null;
        }

        // Update LRU list - move this key to the front (most recently used)
        lruList.remove(key);
        lruList.addFirst(key);

        return cache.get(key);
    }

    public void put(K key, V value) {
        if (cache.containsKey(key)) {
            // Update existing entry
            cache.put(key, value);

            // Update LRU tracking
            lruList.remove(key);
            lruList.addFirst(key);
            return;
        }

        // Check if cache is full
        if (cache.size() >= capacity) {
            // Remove least recently used item
            K leastUsed = lruList.removeLast();
            cache.remove(leastUsed);
        }

        // Add new item
        cache.put(key, value);
        lruList.addFirst(key);
    }

    public void remove(K key) {
        if (cache.containsKey(key)) {
            cache.remove(key);
            lruList.remove(key);
        }
    }

    public void clear() {
        cache.clear();
        lruList.clear();
    }

    public int size() {
        return cache.size();
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }
}