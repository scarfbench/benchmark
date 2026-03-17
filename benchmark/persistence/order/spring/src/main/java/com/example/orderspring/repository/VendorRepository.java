package com.example.orderspring.repository;

import com.example.orderspring.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {
    
    @Query("SELECT v FROM Vendor v WHERE LOCATE(:name, v.name) > 0")
    List<Vendor> findVendorsByPartialName(@Param("name") String name);
    
    @Query("SELECT DISTINCT l.vendorPart.vendor FROM CustomerOrder co, IN(co.lineItems) l WHERE co.orderId = :id ORDER BY l.vendorPart.vendor.name")
    List<Vendor> findVendorByCustomerOrder(@Param("id") Integer orderId);
}
