package com.example.laba7.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Property {

    private Long id;
    private String title;
    private String description;
    private String city;
    private String address;
    private Number price;
    private Integer rooms;
    private Double area;
    private String imageUrl;
    private String type;
    private Long agentId;

    @SerializedName("agent")
    private User agent;

    @SerializedName("createdAt")
    private String createdAt;

    public Property() {}

    public Property(String title, String description, String city, String address,
                    BigDecimal price, Integer rooms, Double area, String type) {
        this.title = title;
        this.description = description;
        this.city = city;
        this.address = address;
        this.price = price;
        this.rooms = rooms;
        this.area = area;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public Double getPrice() { return price != null ? price.doubleValue() : 0.0; }
    public Integer getRooms() { return rooms; }
    public Double getArea() { return area; }
    public String getImageUrl() { return imageUrl; }
    public String getType() { return type; }
    public User getAgent() { return agent; }
    public String getCreatedAt() { return createdAt; }
    public Long getAgentId() { return agentId; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCity(String city) { this.city = city; }
    public void setAddress(String address) { this.address = address; }
    public void setPrice(Number price) { this.price = price; }
    public void setRooms(Integer rooms) { this.rooms = rooms; }
    public void setArea(Double area) { this.area = area; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setType(String type) { this.type = type; }
    public void setAgent(User agent) { this.agent = agent; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
}