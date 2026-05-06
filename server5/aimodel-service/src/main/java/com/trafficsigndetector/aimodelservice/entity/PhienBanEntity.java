package com.trafficsigndetector.aimodelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"PhienBan\"")
public class PhienBanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"Ten\"", nullable = false, length = 100)
    private String ten;

    @Column(name = "\"MoTa\"", length = 500)
    private String moTa;

    @Column(name = "\"DuongDanMH\"", length = 500)
    private String duongDanMH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"MoHinhId\"", nullable = false)
    private MoHinhEntity moHinh;

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

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getDuongDanMH() {
        return duongDanMH;
    }

    public void setDuongDanMH(String duongDanMH) {
        this.duongDanMH = duongDanMH;
    }

    public MoHinhEntity getMoHinh() {
        return moHinh;
    }

    public void setMoHinh(MoHinhEntity moHinh) {
        this.moHinh = moHinh;
    }
}
