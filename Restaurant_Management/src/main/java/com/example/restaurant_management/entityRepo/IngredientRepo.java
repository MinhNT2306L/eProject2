package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Ingredient;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientRepo extends EntityRepo<Ingredient> {

    public IngredientRepo(RowMapper<Ingredient> mapper) {
        super(mapper, "nguyenlieu");
    }

    public List<Ingredient> findAllIngredients() {
        List<Ingredient> ingredientList = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu ORDER BY nl_id DESC";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ingredientList.add(this.getMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredientList;
    }

    public Ingredient findById(int id) {
        String sql = "SELECT * FROM nguyenlieu WHERE nl_id = ?";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return this.getMapper().mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public int insert(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO nguyenlieu (ten_nguyen_lieu, so_luong, don_vi, nha_cung_cap, ngay_nhap, ngay_het_han, trang_thai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Date importDate = ingredient.getImportDate() != null ? Date.valueOf(ingredient.getImportDate()) : null;
        Date expiryDate = ingredient.getExpiryDate() != null ? Date.valueOf(ingredient.getExpiryDate()) : null;
        
        return this.insert(sql, 
            ingredient.getIngredientName(),
            ingredient.getQuantity(),
            ingredient.getUnit(),
            ingredient.getSupplier(),
            importDate,
            expiryDate,
            ingredient.getStatus()
        );
    }

    public int update(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE nguyenlieu SET ten_nguyen_lieu = ?, so_luong = ?, don_vi = ?, " +
                     "nha_cung_cap = ?, ngay_nhap = ?, ngay_het_han = ?, trang_thai = ? " +
                     "WHERE nl_id = ?";
        Date importDate = ingredient.getImportDate() != null ? Date.valueOf(ingredient.getImportDate()) : null;
        Date expiryDate = ingredient.getExpiryDate() != null ? Date.valueOf(ingredient.getExpiryDate()) : null;
        
        return this.insert(sql,
            ingredient.getIngredientName(),
            ingredient.getQuantity(),
            ingredient.getUnit(),
            ingredient.getSupplier(),
            importDate,
            expiryDate,
            ingredient.getStatus(),
            ingredient.getIngredientId()
        );
    }

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM nguyenlieu WHERE nl_id = ?";
        return this.insert(sql, id);
    }

    public List<Ingredient> findByStatus(String status) {
        List<Ingredient> ingredientList = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu WHERE trang_thai = ? ORDER BY nl_id DESC";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ingredientList.add(this.getMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredientList;
    }
}

