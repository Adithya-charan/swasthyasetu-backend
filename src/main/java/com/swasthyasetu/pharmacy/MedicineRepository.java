package com.swasthyasetu.pharmacy;

import com.swasthyasetu.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    @Query("SELECT m FROM Medicine m WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:category IS NULL OR m.category = :category)")
    Page<Medicine> searchMedicines(@Param("name") String name, @Param("category") String category, Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= m.reorderLevel")
    List<Medicine> findLowStockMedicines();
}
