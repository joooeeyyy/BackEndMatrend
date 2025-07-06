package com.tekbridge.alertapp.Design.Builder;


//Builder design patterns produces different types and representation of an object using the construction process

public class Car {

    int interger;
    String model;
    String color;
    String weight;
    String height;

    Car(CarBuilder builder){
        this.interger = builder.interger;

    }

    public static class CarBuilder {

        int interger;
        String model;
        String color;
        String weight;
        String height;

        public CarBuilder id(int interger){
            this.interger = interger;
            return this;
        }

        public Car build(){
            return new Car(this);
        }
    }

    void test(){
        Car car = new Car.CarBuilder().id(1).build();
    }
}



