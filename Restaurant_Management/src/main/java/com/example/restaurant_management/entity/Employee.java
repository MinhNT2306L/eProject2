package com.example.restaurant_management.entity;

public class Employee {
    private int nvId;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private Integer roleId;

    public Employee() {
    }

    public Employee(int nvId, String username, String password, String fullName, String phone, String email, Integer roleId) {
        this.nvId = nvId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.roleId = roleId;
    }

    public int getNvId() {
        return nvId;
    }

    public void setNvId(int nvId) {
        this.nvId = nvId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
}

