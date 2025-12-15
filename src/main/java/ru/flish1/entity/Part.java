package ru.flish1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Запчасти (номенклатура материалов)
 * Может синхронизироваться из 1C, но также может иметь локальные записи
 */
@Entity
@Table(name = "parts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "sku", unique = true)
    private String sku; // Артикул, может быть из 1C

    @Column(name = "quantity")
    private Integer quantity; // Остаток на складе

    @Column(name = "price")
    private Double price; // Цена из 1C

    @Column(name = "unit")
    private String unit; // Единица измерения

    @Column(name = "nomenclature_id")
    private String nomenclatureId; // ID из 1C (для связи с номенклатурой 1C)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private PartType type;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Part() {
    }

    public Part(Integer id, String name, String sku, Integer quantity, Double price, String unit, String nomenclatureId, PartType type, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
        this.nomenclatureId = nomenclatureId;
        this.type = type;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
        this.type = type;
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
