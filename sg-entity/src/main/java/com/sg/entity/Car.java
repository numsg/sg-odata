package com.sg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by gaoqiang on 2017/4/25.
 */
@Entity(name = "CAR")
public class Car {

    @javax.persistence.Id
    @Column
    private int Id;

    @Column
    private String Model;

    @Column
    private String ModelYear;

    @Column
    private double Price;

//    @Column
//    private String Currency;

    @Column
    private Date CreateTime;

    @ManyToOne
    private Manufacturer Manufacturer;

    @ManyToOne
    private People People;


    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getModelYear() {
        return ModelYear;
    }

    public void setModelYear(String modelYear) {
        ModelYear = modelYear;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

//    public String getCurrency() {
//        return Currency;
//    }
//
//    public void setCurrency(String currency) {
//        Currency = currency;
//    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public Manufacturer getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        Manufacturer = manufacturer;
    }

    public Date getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(Date createTime) {
        CreateTime = createTime;
    }

    public People getPeople() {
        return People;
    }

    public void setPeople(People people) {
        People = people;
    }
}