package com.trafficsigndetector.trainingorchestratorservice.persistence.entity;

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
@Table(name = "mau_hl")
public class MauHLEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thong_tin_hl_id", nullable = false)
    private ThongTinHLEntity thongTinHL;

    @Column(name = "thong_tin_mau_json", nullable = false, columnDefinition = "text")
    private String thongTinMauJson;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ThongTinHLEntity getThongTinHL() {
        return thongTinHL;
    }

    public void setThongTinHL(ThongTinHLEntity thongTinHL) {
        this.thongTinHL = thongTinHL;
    }

    public String getThongTinMauJson() {
        return thongTinMauJson;
    }

    public void setThongTinMauJson(String thongTinMauJson) {
        this.thongTinMauJson = thongTinMauJson;
    }
}
