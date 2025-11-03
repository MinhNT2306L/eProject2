package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Food;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FoodMapper implements RowMapper<Food>{
    @Override
    public Food mapRow(ResultSet rs) throws SQLException {
        return new Food(
                rs.getInt("mon_id"),
                rs.getString("ten_mon"),
                rs.getString("loai_mon"),
                rs.getDouble("gia"),
                rs.getString("mo_ta")
        );
    }
}
