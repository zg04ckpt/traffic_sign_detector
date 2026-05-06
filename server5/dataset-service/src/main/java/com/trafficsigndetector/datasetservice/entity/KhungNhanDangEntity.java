package com.trafficsigndetector.datasetservice.entity;

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
@Table(name = "\"KhungNhanDang\"")
public class KhungNhanDangEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"XCenter\"")
    private Float xCenter;

    @Column(name = "\"YCenter\"")
    private Float yCenter;

    @Column(name = "\"W\"")
    private Float w;

    @Column(name = "\"H\"")
    private Float h;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"MauId\"", nullable = false)
    private MauEntity mau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"BienId\"", nullable = false)
    private LoaiBienEntity bien;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getXCenter() {
        return xCenter;
    }

    public void setXCenter(Float xCenter) {
        this.xCenter = xCenter;
    }

    public Float getYCenter() {
        return yCenter;
    }

    public void setYCenter(Float yCenter) {
        this.yCenter = yCenter;
    }

    public Float getW() {
        return w;
    }

    public void setW(Float w) {
        this.w = w;
    }

    public Float getH() {
        return h;
    }

    public void setH(Float h) {
        this.h = h;
    }

    public MauEntity getMau() {
        return mau;
    }

    public void setMau(MauEntity mau) {
        this.mau = mau;
    }

    public LoaiBienEntity getBien() {
        return bien;
    }

    public void setBien(LoaiBienEntity bien) {
        this.bien = bien;
    }
}
