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

    @SerializedName("property")
    private Property property;

    @SerializedName("clientNote")
    private String clientNote;

    @SerializedName("agentNote")
    private String agentNote;
    public Application() {}

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

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getClientId() { return clientId; }
    public Long getAgentId() { return agentId; }
    public String getClientName() { return clientName; }
    public String getClientPhone() { return clientPhone; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public Property getProperty() { return property; }
    public String getClientNote() { return clientNote; }
    public String getAgentNote() { return agentNote; }

    public void setId(Long id) { this.id = id; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setProperty(Property property) { this.property = property; }
    public void setClientNote(String clientNote) { this.clientNote = clientNote; }
    public void setAgentNote(String agentNote) { this.agentNote = agentNote; }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", clientName='" + clientName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}