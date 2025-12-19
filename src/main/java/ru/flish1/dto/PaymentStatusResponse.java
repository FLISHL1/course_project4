package ru.flish1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO для ответа проверки статуса оплаты документа в 1С
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusResponse {
    private String documentId;
    private Boolean isPaid;
    private String paidAt;
    private String paymentMethod;
    private String message;

    public PaymentStatusResponse() {
    }

    public PaymentStatusResponse(String documentId, Boolean isPaid, String paidAt, String paymentMethod, String message) {
        this.documentId = documentId;
        this.isPaid = isPaid;
        this.paidAt = paidAt;
        this.paymentMethod = paymentMethod;
        this.message = message;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PaymentStatusResponse{" +
                "documentId='" + documentId + '\'' +
                ", isPaid=" + isPaid +
                ", paidAt='" + paidAt + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
