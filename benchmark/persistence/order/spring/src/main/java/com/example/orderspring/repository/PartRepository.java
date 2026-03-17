package com.example.orderspring.repository;

import com.example.orderspring.entity.Part;
import com.example.orderspring.entity.PartKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, PartKey> {
    
    @Query("SELECT p FROM Part p ORDER BY p.partNumber")
    List<Part> findAllParts();
    
    Optional<Part> findByPartNumberAndRevision(String partNumber, int revision);
}
