package com.trafficsigndetector.trainingorchestratorservice.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "training_session")
public class ThongTinHLEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tracking_id", nullable = false, unique = true, length = 64)
    private String trackingId;

    @Column(name = "epochs", nullable = false)
    private int epochs;

    @Column(name = "batch_size", nullable = false)
    private int batchSize;

    @Column(name = "trang_thai", nullable = false, length = 32)
    private String trangThai;

    @Column(name = "bat_dau_luc")
    private Instant batDauLuc;

    @Column(name = "ket_thuc_luc")
    private Instant ketThucLuc;

    @Column(name = "do_chinh_xac")
    private Double doChinhXac;

    @Column(name = "do_nhay")
    private Double doNhay;

    @Column(name = "current_epoch")
    private Integer currentEpoch;

    @Column(name = "learning_rate", nullable = false)
    private double learningRate;

    @Column(name = "kich_thuoc_anh", nullable = false)
    private int kichThuocAnh;

    @Column(name = "loai_thiet_bi", nullable = false, length = 32)
    private String loaiThietBi;

    @Column(name = "early_stopping_patience", nullable = false)
    private int earlyStoppingPatience;

    @Column(name = "optimizer", nullable = false, length = 64)
    private String optimizer;

    @Column(name = "phien_ban_hl_json", nullable = false, columnDefinition = "text")
    private String phienBanHLJson;

    @Column(name = "mo_hinh_hl_json", nullable = false, columnDefinition = "text")
    private String moHinhHLJson;

    @Column(name = "ds_mau_hl_json", nullable = false, columnDefinition = "text")
    private String dsMauHLJson;

    @Column(name = "duong_dan_mo_hinh_ket_qua", columnDefinition = "text")
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
