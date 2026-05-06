package com.trafficsigndetector.datasetservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"LoaiBien\"")
public class LoaiBienEntity {

    @Id
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"Ten\"", nullable = false, length = 200)
    private String ten;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }
}
