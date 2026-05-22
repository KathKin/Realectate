package com.example.laba7.model;

import com.google.gson.annotations.SerializedName;

public class User {

    private Long id;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String role;

    @SerializedName("createdAt")
    private String createdAt;

    // Пустой конструктор для Retrofit/Gson
    public User() {}

    // Конструктор для регистрации
    public User(String email, String password, String fullName, String phone, String role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    // ===== Геттеры =====
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }

    // ===== Сеттеры =====
    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}