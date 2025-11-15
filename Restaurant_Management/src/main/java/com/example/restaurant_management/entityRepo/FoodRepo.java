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

    /** Lấy tất cả món ăn */
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

    /** Thêm món ăn mới */
    public boolean addFood(Food food) {
        String sql = "INSERT INTO monan (ten_mon, gia, loai_mon, trang_thai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setString(1, food.getFoodName());
            stmt.setDouble(2, food.getPrice());
            stmt.setString(3, food.getFoodCategory());
            stmt.setString(4, food.getStatus());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Cập nhật món ăn */
    public boolean updateFood(Food food) {
        String sql = "UPDATE monan SET ten_mon=?, gia=?, loai_mon=?, trang_thai=? WHERE mon_id=?";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setString(1, food.getFoodName());
            stmt.setDouble(2, food.getPrice());
            stmt.setString(3, food.getFoodCategory());
            stmt.setString(4, food.getStatus());
            stmt.setInt(5, food.getFoodId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Xóa món ăn */
    public boolean deleteFood(int id) {
        String sql = "DELETE FROM monan WHERE mon_id=?";
        try (PreparedStatement stmt = this.getConn().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Tìm món ăn theo ID */
    public Food findById(int id) {
        String sql = "SELECT * FROM monan WHERE mon_id=?";
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
