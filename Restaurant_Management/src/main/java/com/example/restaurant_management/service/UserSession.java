package com.example.restaurant_management.service;

import com.example.restaurant_management.entity.Employee;

public class UserSession {
    private static Employee currentEmployee;

    public static void setCurrentEmployee(Employee employee) {
        currentEmployee = employee;
    }

    public static Employee getCurrentEmployee() {
        return currentEmployee;
    }

    public static Integer getCurrentEmployeeId() {
        return currentEmployee != null ? currentEmployee.getNvId() : null;
    }

    public static void clear() {
        currentEmployee = null;
    }
}

