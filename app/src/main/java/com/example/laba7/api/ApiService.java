package com.example.laba7.api;

import com.example.laba7.model.Application;
import com.example.laba7.model.Property;
import com.example.laba7.model.User;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import okhttp3.ResponseBody;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/users/register")
    Call<AuthResponse> register(@Body UserRegisterRequest request);
    @POST("api/users/login")
    Call<AuthResponse> login(@Body UserLoginRequest request);
    @GET("api/properties")
    Call<List<Property>> getAllProperties();
    @GET("api/properties/{id}")
    Call<Property> getPropertyById(@Path("id") Long id);

    @GET("api/users/check-realtor")
    Call<Map<String, Object>> checkRealtorStatus(@Query("email") String email);
    @POST("api/applications")
    Call<Application> submitApplication(@Body Application application);
    @GET("api/applications/agent/{agentId}")
    Call<List<Application>> getAgentApplications(@Path("agentId") Long agentId);

    @Multipart
    @POST("api/files/upload")
    Call<ResponseBody> uploadFile(@Part MultipartBody.Part file);
    @POST("api/properties")
    Call<com.example.laba7.model.Property> createProperty(@Body com.example.laba7.model.Property property);
    @DELETE("api/properties/{id}")
    Call<ResponseBody> deleteProperty(@Path("id") Long id);
    @GET("/api/applications/client/{clientId}")
    Call<List<Application>> getApplicationsByClientId(@Path("clientId") Long clientId);
    @PUT("/api/applications/{id}/note")
    Call<Application> updateApplicationNote(@Path("id") Long id, @Body Map<String, String> noteBody);
    @DELETE("/api/applications/{id}")
    Call<Void> deleteApplication(@Path("id") Long id);
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