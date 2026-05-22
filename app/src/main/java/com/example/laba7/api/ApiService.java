package com.example.laba7.api;

import com.example.laba7.model.Property;
import com.example.laba7.model.User;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ============= Auth =============
    @POST("api/users/register")
    Call<AuthResponse> register(@Body UserRegisterRequest request);

    @POST("api/users/login")
    Call<AuthResponse> login(@Body UserLoginRequest request);

    // ============= Properties =============
    @GET("api/properties")
    Call<List<Property>> getAllProperties();

    @GET("api/properties/{id}")
    Call<Property> getPropertyById(@Path("id") Long id);

    // Внутренние классы для запросов
    class UserRegisterRequest {
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private String role;

        public UserRegisterRequest(String email, String password, String fullName, String phone, String role) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
        }
    }

    class UserLoginRequest {
        private String email;
        private String password;

        public UserLoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class AuthResponse {
        private User user;
        private String token;
        private String message;

        public User getUser() { return user; }
        public String getToken() { return token; }
        public String getMessage() { return message; }
    }
}