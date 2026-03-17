package com.example.orderspring.repository;

import com.example.orderspring.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {
    
    @Query("SELECT co FROM CustomerOrder co ORDER BY co.orderId")
    List<CustomerOrder> findAllOrders();
}
