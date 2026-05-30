package com.example.laba7.dto;

import com.google.gson.annotations.SerializedName;

public class PropertyWithApplicationsDto {

    @SerializedName("propertyId")
    private Long propertyId;

    @SerializedName("propertyTitle")
    private String propertyTitle;

    @SerializedName("propertyImageUrl")
    private String propertyImageUrl;

    @SerializedName("applicationsCount")
    private long applicationsCount;

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public String getPropertyImageUrl() {
        return propertyImageUrl;
    }

    public void setPropertyImageUrl(String propertyImageUrl) {
        this.propertyImageUrl = propertyImageUrl;
    }

    public long getApplicationsCount() {
        return applicationsCount;
    }

    public void setApplicationsCount(long applicationsCount) {
        this.applicationsCount = applicationsCount;
    }
}
