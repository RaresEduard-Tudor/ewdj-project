package com.worldcup.dto;

import com.worldcup.validation.ValidEmail;
import com.worldcup.validation.ValidPasswords;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@ValidPasswords
public class RegistrationDto {

    @NotBlank(message = "{validation.username.blank}")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "{validation.username.pattern}")
    private String username;

    @ValidEmail
    private String email;

    @Size(min = 8, message = "{validation.password.size}")
    private String password;

    private String confirmPassword;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
