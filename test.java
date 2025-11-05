package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodRepo extends EntityRepo<Food> {

    public FoodRepo(RowMapper<Food> mapper) {
        super(mapper, "monan");
    }

    public List<Food> findAllFoods() {
        List<Food> foodList = new ArrayList<>();
        String sql = "SELECT * FROM monan";

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

    public boolean addFood(Food food) {
        String sql = "INSERT INTO monan (tenmon, gia, loaimon, trangthai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setString(1, food.getTenmon());
            stmt.setDouble(2, food.getGia());
            stmt.setString(3, food.getLoaimon());
            stmt.setString(4, food.getTrangthai());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateFood(Food food) {
        String sql = "UPDATE monan SET tenmon=?, gia=?, loaimon=?, trangthai=? WHERE id=?";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setString(1, food.getTenmon());
            stmt.setDouble(2, food.getGia());
            stmt.setString(3, food.getLoaimon());
            stmt.setString(4, food.getTrangthai());
            stmt.setInt(5, food.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteFood(int id) {
        String sql = "DELETE FROM monan WHERE id=?";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Food findById(int id) {
        String sql = "SELECT * FROM monan WHERE id=?";
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
}
