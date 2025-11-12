package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodRepo extends EntityRepo<Food> {

    public FoodRepo(RowMapper<Food> mapper) {
        super(mapper, "monan");
    }

    public List<Food> findAllFoods() {
        List<Food> foodList = new ArrayList<>();
        String sql = "SELECT * FROM monan ORDER BY mon_id";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                foodList.add(this.getMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return foodList;
    }

    public Optional<Food> findById(int foodId) {
        String sql = "SELECT * FROM monan WHERE mon_id = ?";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(this.getMapper().mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }
}
