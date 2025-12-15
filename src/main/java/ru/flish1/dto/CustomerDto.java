package ru.flish1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для представления клиента (физ лица) из API 1C
 * В 1C хранится только ФИО и номер телефона
 */
public class CustomerDto {
    @JsonProperty("name")
    private String name; // ФИО клиента

    @JsonProperty("phoneNumber")
    private String phoneNumber; // Номер телефона

    public CustomerDto() {
    }

    public CustomerDto(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
