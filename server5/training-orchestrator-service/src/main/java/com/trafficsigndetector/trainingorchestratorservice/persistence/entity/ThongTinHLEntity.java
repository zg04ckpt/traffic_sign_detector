package com.trafficsigndetector.trainingorchestratorservice.persistence.entity;

import java.time.Instant;

/**
 * Mutable training session row (PostgreSQL {@code training_session}). Plain bean — no JPA.
 */
public class ThongTinHLEntity {

    private Integer id;

    private String trackingId;

    private int epochs;

    private int batchSize;

    private String trangThai;

    private Instant batDauLuc;

    private Instant ketThucLuc;

    private Double doChinhXac;

    private Double doNhay;

    private Integer currentEpoch;

    private double learningRate;

    private int kichThuocAnh;

    private String loaiThietBi;

    private int earlyStoppingPatience;

    private String optimizer;

    private String phienBanHLJson;

    private String moHinhHLJson;

    private String dsMauHLJson;

    private String duongDanMoHinhKetQua;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Instant getBatDauLuc() {
        return batDauLuc;
    }

    public void setBatDauLuc(Instant batDauLuc) {
        this.batDauLuc = batDauLuc;
    }

    public Instant getKetThucLuc() {
        return ketThucLuc;
    }

    public void setKetThucLuc(Instant ketThucLuc) {
        this.ketThucLuc = ketThucLuc;
    }

    public Double getDoChinhXac() {
        return doChinhXac;
    }

    public void setDoChinhXac(Double doChinhXac) {
        this.doChinhXac = doChinhXac;
    }

    public Double getDoNhay() {
        return doNhay;
    }

    public void setDoNhay(Double doNhay) {
        this.doNhay = doNhay;
    }

    public Integer getCurrentEpoch() {
        return currentEpoch;
    }

    public void setCurrentEpoch(Integer currentEpoch) {
        this.currentEpoch = currentEpoch;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getKichThuocAnh() {
        return kichThuocAnh;
    }

    public void setKichThuocAnh(int kichThuocAnh) {
        this.kichThuocAnh = kichThuocAnh;
    }

    public String getLoaiThietBi() {
        return loaiThietBi;
    }

    public void setLoaiThietBi(String loaiThietBi) {
        this.loaiThietBi = loaiThietBi;
    }

    public int getEarlyStoppingPatience() {
        return earlyStoppingPatience;
    }

    public void setEarlyStoppingPatience(int earlyStoppingPatience) {
        this.earlyStoppingPatience = earlyStoppingPatience;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.optimizer = optimizer;
    }

    public String getPhienBanHLJson() {
        return phienBanHLJson;
    }

    public void setPhienBanHLJson(String phienBanHLJson) {
        this.phienBanHLJson = phienBanHLJson;
    }

    public String getMoHinhHLJson() {
        return moHinhHLJson;
    }

    public void setMoHinhHLJson(String moHinhHLJson) {
        this.moHinhHLJson = moHinhHLJson;
    }

    public String getDsMauHLJson() {
        return dsMauHLJson;
    }

    public void setDsMauHLJson(String dsMauHLJson) {
        this.dsMauHLJson = dsMauHLJson;
    }

    public String getDuongDanMoHinhKetQua() {
        return duongDanMoHinhKetQua;
    }

    public void setDuongDanMoHinhKetQua(String duongDanMoHinhKetQua) {
        this.duongDanMoHinhKetQua = duongDanMoHinhKetQua;
    }
}
