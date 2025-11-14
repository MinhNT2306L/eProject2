package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Ingredient;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class IngredientMapper implements RowMapper<Ingredient> {
    @Override
    public Ingredient mapRow(ResultSet rs) throws SQLException {
        Date importDateSql = rs.getDate("ngay_nhap");
        Date expiryDateSql = rs.getDate("ngay_het_han");
        
        LocalDate importDate = importDateSql != null ? importDateSql.toLocalDate() : null;
        LocalDate expiryDate = expiryDateSql != null ? expiryDateSql.toLocalDate() : null;
        
        return new Ingredient(
                rs.getInt("nl_id"),
                rs.getString("ten_nguyen_lieu"),
                rs.getDouble("so_luong"),
                rs.getString("don_vi"),
                rs.getString("nha_cung_cap"),
                importDate,
                expiryDate,
                rs.getString("trang_thai")
        );
    }
}

