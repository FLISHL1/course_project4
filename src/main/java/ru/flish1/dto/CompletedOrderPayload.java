package ru.flish1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO для отправки данных о выполненном заказе в 1C
 */
public class CompletedOrderPayload {
    @JsonProperty("sourceOrderId")
    private String sourceOrderId;

    @JsonProperty("completionDate")
    private String completionDate;

    @JsonProperty("customerTaxId")
    private String customerTaxId;

    @JsonProperty("services")
    private List<CompletedOrderItem> services;

    @JsonProperty("materials")
    private List<CompletedOrderItem> materials;

    @JsonProperty("paymentMethod")
    private String paymentMethod;

    public CompletedOrderPayload() {
    }

    public CompletedOrderPayload(String sourceOrderId, String completionDate, String customerTaxId,
                                 List<CompletedOrderItem> services, List<CompletedOrderItem> materials,
                                 String paymentMethod) {
        this.sourceOrderId = sourceOrderId;
        this.completionDate = completionDate;
        this.customerTaxId = customerTaxId;
        this.services = services;
        this.materials = materials;
        this.paymentMethod = paymentMethod;
    }

    public String getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(String sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getCustomerTaxId() {
        return customerTaxId;
    }

    public void setCustomerTaxId(String customerTaxId) {
        this.customerTaxId = customerTaxId;
    }

    public List<CompletedOrderItem> getServices() {
        return services;
    }

    public void setServices(List<CompletedOrderItem> services) {
        this.services = services;
    }

    public List<CompletedOrderItem> getMaterials() {
        return materials;
    }

    public void setMaterials(List<CompletedOrderItem> materials) {
        this.materials = materials;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
