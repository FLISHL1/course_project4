package ru.flish1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для элемента выполненного заказа (услуга или материал)
 */
public class CompletedOrderItem {
    @JsonProperty("nomenclatureId")
    private String nomenclatureId;

    @JsonProperty("quantity")
    private double quantity;

    @JsonProperty("pricePerUnit")
    private double pricePerUnit;

    @JsonProperty("totalPrice")
    private double totalPrice;

    public CompletedOrderItem() {
    }

    public CompletedOrderItem(String nomenclatureId, double quantity, double pricePerUnit, double totalPrice) {
        this.nomenclatureId = nomenclatureId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.totalPrice = totalPrice;
    }

    public String getNomenclatureId() {
        return nomenclatureId;
    }

    public void setNomenclatureId(String nomenclatureId) {
        this.nomenclatureId = nomenclatureId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
