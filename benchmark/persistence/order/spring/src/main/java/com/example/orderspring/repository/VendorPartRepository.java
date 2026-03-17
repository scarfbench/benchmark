package com.example.orderspring.repository;

import com.example.orderspring.entity.VendorPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorPartRepository extends JpaRepository<VendorPart, Long> {
    
    @Query("SELECT AVG(vp.price) FROM VendorPart vp")
    Double findAverageVendorPartPrice();
    
    @Query("SELECT SUM(vp.price) FROM VendorPart vp WHERE vp.vendor.vendorId = :id")
    Double findTotalVendorPartPricePerVendor(@Param("id") int vendorId);
    
    @Query("SELECT vp FROM VendorPart vp ORDER BY vp.vendorPartNumber")
    List<VendorPart> findAllVendorParts();
}
