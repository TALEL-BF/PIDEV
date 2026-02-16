package com.auticare.entities;

public class PlanningRow {
    private String time;
    private String activity;

    public PlanningRow(String time, String activity) {
        this.time = time;
        this.activity = activity;
    }

    // Getters et Setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }
}