package ru.flish1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для представления номенклатуры из API 1C
 */
public class NomenclatureDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("article")
    private String article;

    @JsonProperty("type")
    private String type;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("price")
    private Double price;

    public NomenclatureDto() {
    }

    public NomenclatureDto(String id, String name, String article, String type, String unit, Double price) {
        this.id = id;
        this.name = name;
        this.article = article;
        this.type = type;
        this.unit = unit;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
