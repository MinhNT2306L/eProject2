package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntityRepo<T> {
    private final Connection conn;
    private final RowMapper<T> mapper;
    private final String tableName;



    public EntityRepo(RowMapper<T> mapper, String tableName) {
        this(ConnectDB.getConnection(), mapper, tableName);
    }

    public EntityRepo(Connection conn, RowMapper<T> mapper, String tableName) {
        this.conn = conn;
        this.mapper = mapper;
        this.tableName = tableName;
    }

    public Connection getConn() {
        return conn;
    }

    public RowMapper<T> getMapper() {
        return mapper;
    }

    public String getTableName() {
        return tableName;
    }

    public List<T> getAll(){
        try{
            List<T> list = new ArrayList<>();
            String sql = "SELECT * FROM " + tableName;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                list.add(mapper.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public T findByID(String id){
        try{
            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                return mapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int insert(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }
}
