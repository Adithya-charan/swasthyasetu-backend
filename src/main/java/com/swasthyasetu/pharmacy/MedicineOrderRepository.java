package com.swasthyasetu.pharmacy;

import com.swasthyasetu.pharmacy.entity.MedicineOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, UUID> {
    Optional<MedicineOrder> findByPrescriptionId(UUID prescriptionId);
}
