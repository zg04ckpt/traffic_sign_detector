package com.trafficsigndetector.datasetservice.persistence.sqlite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQLite cache entity for TapDuLieu (Dataset)
 */
@Entity
@Table(name = "tap_du_lieu_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TapDuLieuSqliteEntity {
    
    @Id
    private Integer id;
    
    @Column(nullable = false, length = 200)
    private String ten;
    
    @Column(name = "synced_at")
    private Long syncedAt = System.currentTimeMillis();
}
