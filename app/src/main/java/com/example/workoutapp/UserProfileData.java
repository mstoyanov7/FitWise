package com.example.workoutapp;

public class UserProfileData {
    public String name;
    public String age;
    public String weight;
    public String sex;
    public String avatarUri;

    public UserProfileData() {}

    public UserProfileData(String name, String age, String weight, String sex, String avatarUri) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.sex = sex;
        this.avatarUri = avatarUri;
    }
}