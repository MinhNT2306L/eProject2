package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Employee;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeMapper implements RowMapper<Employee> {
    @Override
    public Employee mapRow(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("nv_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getObject("role_id") != null ? rs.getInt("role_id") : null
        );
    }
}

