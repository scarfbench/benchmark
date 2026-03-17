package com.example.orderspring.repository;

import com.example.orderspring.entity.LineItem;
import com.example.orderspring.entity.LineItemKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineItemRepository extends JpaRepository<LineItem, LineItemKey> {
    
    @Query("SELECT l FROM LineItem l")
    List<LineItem> findAllLineItems();
    
    @Query("SELECT l FROM LineItem l WHERE l.customerOrder.orderId = :orderId ORDER BY l.itemId")
    List<LineItem> findLineItemsByOrderId(@Param("orderId") Integer orderId);
    
    @Query("SELECT DISTINCT l FROM LineItem l WHERE l.itemId = :itemId AND l.customerOrder.orderId = :orderId")
    LineItem findLineItemById(@Param("itemId") int itemId, @Param("orderId") Integer orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LineItem l WHERE l.customerOrder.orderId = :orderId")
    void deleteAllByOrderId(@Param("orderId") Integer orderId);

}
