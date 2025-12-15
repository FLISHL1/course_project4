package ru.flish1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для успешного ответа от API 1C
 */
public class SuccessResponse {
    @JsonProperty("document1cId")
    private String document1cId;

    @JsonProperty("document1cNumber")
    private String document1cNumber;

    @JsonProperty("message")
    private String message;

    public SuccessResponse() {
    }

    public SuccessResponse(String document1cId, String document1cNumber, String message) {
        this.document1cId = document1cId;
        this.document1cNumber = document1cNumber;
        this.message = message;
    }

    public String getDocument1cId() {
        return document1cId;
    }

    public void setDocument1cId(String document1cId) {
        this.document1cId = document1cId;
    }

    public String getDocument1cNumber() {
        return document1cNumber;
    }

    public void setDocument1cNumber(String document1cNumber) {
        this.document1cNumber = document1cNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
