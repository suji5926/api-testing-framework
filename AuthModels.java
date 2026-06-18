package com.apitest.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Models for the /auth/login and /auth/token endpoints. */
public class AuthModels {

    // ── Login request ─────────────────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginRequest {

        @JsonProperty("username")
        private String username;

        @JsonProperty("password")
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void   setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void   setPassword(String password) { this.password = password; }
    }

    // ── Login response ────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginResponse {

        @JsonProperty("token")
        private String token;

        @JsonProperty("userId")
        private Integer userId;

        @JsonProperty("expiresIn")
        private Long expiresIn;

        @JsonProperty("tokenType")
        private String tokenType;

        public String  getToken()     { return token; }
        public Integer getUserId()    { return userId; }
        public Long    getExpiresIn() { return expiresIn; }
        public String  getTokenType() { return tokenType; }
    }

    // ── Token refresh request ─────────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefreshRequest {

        @JsonProperty("refreshToken")
        private String refreshToken;

        public RefreshRequest() {}
        public RefreshRequest(String refreshToken) { this.refreshToken = refreshToken; }

        public String getRefreshToken() { return refreshToken; }
        public void   setRefreshToken(String t) { this.refreshToken = t; }
    }

    // ── Error response ────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorResponse {

        @JsonProperty("message")
        private String message;

        @JsonProperty("status")
        private Integer status;

        @JsonProperty("error")
        private String error;

        public String  getMessage() { return message; }
        public Integer getStatus()  { return status; }
        public String  getError()   { return error; }
    }
}
