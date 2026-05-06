package com.trafficsigndetector.datasetservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"TapDuLieu\"")
public class TapDuLieuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"Ten\"", nullable = false, length = 200)
    private String ten;

    @OneToMany(mappedBy = "tapDuLieu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MauEntity> dsMau = new ArrayList<>();

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

    public List<MauEntity> getDsMau() {
        return dsMau;
    }

    public void setDsMau(List<MauEntity> dsMau) {
        this.dsMau = dsMau;
    }

    public void addMau(MauEntity mau) {
        this.dsMau.add(mau);
        mau.setTapDuLieu(this);
    }

    public void removeMau(MauEntity mau) {
        this.dsMau.remove(mau);
        mau.setTapDuLieu(null);
    }
}
