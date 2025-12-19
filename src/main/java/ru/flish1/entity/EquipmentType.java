package ru.flish1.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Типы оборудования для заявок на ремонт
 */
@Entity
@Table(name = "equipment_types")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EquipmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Флаг, указывающий что это тип "Другое" - позволяет вводить пользовательский тип
     */
    @Column(name = "is_other", nullable = false)
    private Boolean isOther = false;

    public EquipmentType() {
    }

    public EquipmentType(Integer id, String name, Boolean isOther) {
        this.id = id;
        this.name = name;
        this.isOther = isOther;
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

    public Boolean getIsOther() {
        return isOther;
    }

    public void setIsOther(Boolean isOther) {
        this.isOther = isOther;
    }
}
