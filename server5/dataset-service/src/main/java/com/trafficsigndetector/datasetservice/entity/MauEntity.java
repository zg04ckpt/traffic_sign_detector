package com.trafficsigndetector.datasetservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"Mau\"")
public class MauEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"DuongDanAnh\"", nullable = false, length = 500)
    private String duongDanAnh;

    @Column(name = "\"DoPhanGiai\"", length = 50)
    private String doPhanGiai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"TapDuLieuId\"", nullable = false)
    private TapDuLieuEntity tapDuLieu;

    @OneToMany(mappedBy = "mau", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<KhungNhanDangEntity> dsBien = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String duongDanAnh) {
        this.duongDanAnh = duongDanAnh;
    }

    public String getDoPhanGiai() {
        return doPhanGiai;
    }

    public void setDoPhanGiai(String doPhanGiai) {
        this.doPhanGiai = doPhanGiai;
    }

    public TapDuLieuEntity getTapDuLieu() {
        return tapDuLieu;
    }

    public void setTapDuLieu(TapDuLieuEntity tapDuLieu) {
        this.tapDuLieu = tapDuLieu;
    }

    public List<KhungNhanDangEntity> getDsBien() {
        return dsBien;
    }

    public void setDsBien(List<KhungNhanDangEntity> dsBien) {
        this.dsBien = dsBien;
    }
}
