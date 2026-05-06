package com.trafficsigndetector.aimodelservice.entity;

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
@Table(name = "\"MoHinh\"")
public class MoHinhEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"Ten\"", nullable = false, length = 200)
    private String ten;

    @Column(name = "\"MoHinhGoc\"", length = 500)
    private String moHinhGoc;

    @OneToMany(mappedBy = "moHinh", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhienBanEntity> dsPhienBan = new ArrayList<>();

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

    public String getMoHinhGoc() {
        return moHinhGoc;
    }

    public void setMoHinhGoc(String moHinhGoc) {
        this.moHinhGoc = moHinhGoc;
    }

    public List<PhienBanEntity> getDsPhienBan() {
        return dsPhienBan;
    }

    public void setDsPhienBan(List<PhienBanEntity> dsPhienBan) {
        this.dsPhienBan = dsPhienBan;
    }

    public void addPhienBan(PhienBanEntity phienBan) {
        this.dsPhienBan.add(phienBan);
        phienBan.setMoHinh(this);
    }
}
