package ru.flish1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Услуги
 * Может синхронизироваться из 1C, но также может иметь локальные записи
 */
@Entity
@Table(name = "services")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "sku", unique = true)
    private String sku; // Артикул, может быть из 1C

    @Column(name = "price")
    private Double price; // Цена из 1C

    @Column(name = "unit")
    private String unit; // Единица измерения

    @Column(name = "nomenclature_id")
    private String nomenclatureId; // ID из 1C (для связи с номенклатурой 1C)

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ServiceEntity() {
    }

    public ServiceEntity(Integer id, String name, String sku, Double price, String unit, String nomenclatureId, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.unit = unit;
        this.nomenclatureId = nomenclatureId;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNomenclatureId() {
        return nomenclatureId;
    }

    public void setNomenclatureId(String nomenclatureId) {
        this.nomenclatureId = nomenclatureId;
    }
}

