package com.example.imhumman.firebaseModels;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserDataModel {

    private String email;

    private String full_name;

    private String password;

    private String phone_number;

    private String user_photo;

    public UserDataModel() {
    }

    public UserDataModel(String first_name, String last_name, String email, String phone_number, String user_photo, String password) {

        this.full_name = first_name;
        this.email = email;
        this.password = password;
        this.phone_number = phone_number;
        this.user_photo = user_photo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }


    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("full_name", full_name);
        values.put("phone_number", phone_number);
        values.put("user_photo", user_photo);
        values.put("password", password);
        return values;
    }
}
