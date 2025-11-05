package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.OrderItem;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderItemRepo extends EntityRepo<OrderItem> {

    public OrderItemRepo(RowMapper<OrderItem> mapper) {
        super(mapper, "order_chitiet");
    }

    public OrderItemRepo(RowMapper<OrderItem> mapper, Connection connection) {
        super(mapper, "order_chitiet", connection);
    }

    public void insertItemsBatch(List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_chitiet (order_id, mon_id, so_luong, don_gia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = this.getConn().prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setInt(1, item.getOrderId());
                if (item.getFoodId() == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, item.getFoodId());
                }
                ps.setInt(3, item.getQuantity());
                ps.setBigDecimal(4, item.getUnitPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
