package com.example.laba7.model;

import com.google.gson.annotations.SerializedName;

public class Application {

    private Long id;

    @SerializedName("propertyId")
    private Long propertyId;

    @SerializedName("clientId")
    private Long clientId;

    @SerializedName("agentId")
    private Long agentId;

    @SerializedName("clientName")
    private String clientName;

    @SerializedName("clientPhone")
    private String clientPhone;

    private String message;
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    // Пустой конструктор (обязателен для Retrofit/Gson)
    public Application() {}

    // Конструктор для отправки заявки
    public Application(Long propertyId, Long clientId, Long agentId,
                       String clientName, String clientPhone, String message) {
        this.propertyId = propertyId;
        this.clientId = clientId;
        this.agentId = agentId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.message = message;
        this.status = "NEW";
    }

    // ===== Геттеры =====
    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getClientId() { return clientId; }
    public Long getAgentId() { return agentId; }
    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    // ===== Сеттеры =====
    public void setId(Long id) { this.id = id; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Для отладки
    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}