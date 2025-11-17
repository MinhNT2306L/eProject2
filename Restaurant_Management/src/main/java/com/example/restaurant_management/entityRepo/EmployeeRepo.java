package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.mapper.EmployeeMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeRepo {
    private final Connection conn;
    private final EmployeeMapper mapper;

    public EmployeeRepo() {
        try {
            this.conn = ConnectDB.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối đến database: " + e.getMessage(), e);
        }
        this.mapper = new EmployeeMapper();
    }

    public Employee findByUsername(String username) {
        String sql = "SELECT * FROM nhanvien WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Employee findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM nhanvien WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public java.util.List<Employee> getAll() {
        java.util.List<Employee> employees = new java.util.ArrayList<>();
        String sql = "SELECT * FROM nhanvien ORDER BY nv_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return employees;
    }

    public Employee findById(int nvId) {
        String sql = "SELECT * FROM nhanvien WHERE nv_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nvId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean insert(Employee employee) {
        String sql = "INSERT INTO nhanvien (username, password, full_name, phone, email, role_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employee.getUsername());
            ps.setString(2, employee.getPassword());
            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getPhone());
            ps.setString(5, employee.getEmail());
            if (employee.getRoleId() != null) {
                ps.setInt(6, employee.getRoleId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Employee employee) {
        String sql = "UPDATE nhanvien SET username = ?, password = ?, full_name = ?, phone = ?, email = ?, role_id = ? WHERE nv_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employee.getUsername());
            ps.setString(2, employee.getPassword());
            ps.setString(3, employee.getFullName());
            ps.setString(4, employee.getPhone());
            ps.setString(5, employee.getEmail());
            if (employee.getRoleId() != null) {
                ps.setInt(6, employee.getRoleId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setInt(7, employee.getNvId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int nvId) {
        String sql = "DELETE FROM nhanvien WHERE nv_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nvId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<java.util.Map<String, Object>> getAllRoles() {
        java.util.List<java.util.Map<String, Object>> roles = new java.util.ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> role = new java.util.HashMap<>();
                    role.put("roleId", rs.getInt("role_id"));
                    role.put("roleName", rs.getString("role_name"));
                    roles.add(role);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return roles;
    }
}

