package ru.flish1.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Заявки на обслуживание
 * customer_id - строка (ID клиента, может быть phoneNumber или другой идентификатор)
 * Заказ отправляется в 1C, но не хранится в БД
 */
@Entity
@Table(name = "requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id", nullable = false, insertable = true, updatable = true)
    private String customerId; // ID клиента (строка, не FK - phoneNumber или другой идентификатор)

    @Column(name = "address")
    private String address;

    @Column(name = "status", nullable = false)
    private String status; // new, assigned, in_progress, completed, canceled

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id")
    @JsonIgnore
    private User engineer;

    /**
     * Тип оборудования
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_id")
    @JsonIgnore
    private EquipmentType equipmentType;

    /**
     * Пользовательский тип оборудования (используется когда выбран тип "Другое")
     */
    @Column(name = "custom_equipment_type")
    private String customEquipmentType;

    /**
     * Описание проблемы клиента
     */
    @Column(name = "problem_description", columnDefinition = "TEXT")
    private String problemDescription;

    /**
     * Способ оплаты (cash - наличные, card - карта, transfer - перевод)
     */
    @Column(name = "payment_method")
    private String paymentMethod;

    /**
     * ID документа в 1С (для проверки оплаты)
     */
    @Column(name = "document_1c_id")
    private String document1cId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Request() {
    }

    public Request(Integer id, String customerId, String address, String status, User engineer,
                   EquipmentType equipmentType, String customEquipmentType, String problemDescription,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.address = address;
        this.status = status;
        this.engineer = engineer;
        this.equipmentType = equipmentType;
        this.customEquipmentType = customEquipmentType;
        this.problemDescription = problemDescription;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getEngineer() {
        return engineer;
    }

    public void setEngineer(User engineer) {
        this.engineer = engineer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getCustomEquipmentType() {
        return customEquipmentType;
    }

    public void setCustomEquipmentType(String customEquipmentType) {
        this.customEquipmentType = customEquipmentType;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    /**
     * Получает отображаемое название типа оборудования
     * Если выбран тип "Другое", возвращает пользовательский тип
     */
    public String getDisplayEquipmentType() {
        if (equipmentType != null && equipmentType.getIsOther() && customEquipmentType != null && !customEquipmentType.isEmpty()) {
            return customEquipmentType;
        }
        return equipmentType != null ? equipmentType.getName() : null;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDocument1cId() {
        return document1cId;
    }

    public void setDocument1cId(String document1cId) {
        this.document1cId = document1cId;
    }

    /**
     * Получает отображаемое название способа оплаты
     */
    public String getDisplayPaymentMethod() {
        if (paymentMethod == null) return null;
        return switch (paymentMethod) {
            case "cash" -> "Наличные";
            case "card" -> "Банковская карта";
            case "transfer" -> "Безналичный перевод";
            default -> paymentMethod;
        };
    }
}
