package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoodRepo extends EntityRepo<Food>{

    public FoodRepo(RowMapper<Food> mapper, String tableName) {
        super(mapper, tableName);
    }

    public List<Food> findFood(String category){
        try{
            List<Food> foodList = new ArrayList<>();
            String sql = "SELECT * FROM " + this.getTableName() + " WHERE `loai_mon` = " + category;
            PreparedStatement stmt = this.getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                foodList.add(this.getMapper().mapRow(rs));
            }
            return foodList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
