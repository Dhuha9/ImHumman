package com.example.imhumman.firebaseModels;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class PostDataModel {

    private Long created_date;
    private String content;
    private double location_latitude;
    private double location_longitude;
    private String phone_number;
    private String post_photo;
    private String user_name;
    private String user_photo;
    private String uid;
    private String postId;

    public PostDataModel() {
    }

    public PostDataModel(String content, double location_latitude, double location_longitude, String phone_number, String post_photo) {
        // Log.i("php",phone_number);
        this.content = content;
        this.location_latitude = location_latitude;
        this.location_longitude = location_longitude;
        this.phone_number = phone_number;
        this.post_photo = post_photo;

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getCreated_date() {
        return ServerValue.TIMESTAMP;
    }


    @Exclude
    public Long getCreationDateLong() {
        return created_date;
    }

    public double getLocation_latitude() {
        return location_latitude;
    }

    public void setLocation_latitude(double location_latitude) {
        this.location_latitude = location_latitude;
    }

    public double getLocation_longitude() {
        return location_longitude;
    }

    public void setLocation_longitude(double location_longitude) {
        this.location_longitude = location_longitude;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getPost_photo() {
        return post_photo;
    }

    public void setPost_photo(String post_photo) {
        this.post_photo = post_photo;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

//    @Override
//    public boolean equals(@Nullable Object obj) {
//        PostDataModel post= (PostDataModel) obj;
//        return content.matches(post.getContent());
//    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("content", content);
        values.put("location_latitude", location_latitude);
        values.put("location_longitude", location_longitude);
        values.put("phone_number", phone_number);
        values.put("post_photo", post_photo);
        return values;
    }

    @Exclude
    public Map<String, Object> toMapUserInfo() {
        Map<String, Object> values = new HashMap<>();
        // values.put("uid",uid);
        values.put("user_name", user_name);
        values.put("user_photo", user_photo);

        return values;
    }
}
