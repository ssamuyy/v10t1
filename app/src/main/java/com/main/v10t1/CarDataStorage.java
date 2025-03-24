package com.main.v10t1;

import java.util.ArrayList;

class CarDataStorage {
    private static CarDataStorage instance = null;
    private String city;
    private int year;
    private ArrayList<CarData> carData;

    private CarDataStorage() {
        carData = new ArrayList<>();
    }

    public static CarDataStorage getInstance() {
        if (instance == null) {
            instance = new CarDataStorage();
        }
        return instance;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCity() {
        return city;
    }

    public int getYear() {
        return year;
    }

    public void addCarData(CarData data) {
        carData.add(data);
    }

    public ArrayList<CarData> getCarData() {
        return carData;
    }

    public void clearData() {
        carData.clear();
    }
}