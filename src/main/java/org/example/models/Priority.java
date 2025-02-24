package org.example.models;

public class Priority implements Comparable<Priority>{
    private int priority;
    private String name;

    public Priority(int priority,String name){
        this.priority = priority;
        this.name = name;
    }
    public int compareTo(Priority other){
        return Integer.compare(this.priority,other.priority);
    }


}
