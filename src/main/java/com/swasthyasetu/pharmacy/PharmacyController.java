package com.swasthyasetu.pharmacy;

import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.common.ResourceNotFoundException;
import com.swasthyasetu.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {

    private final MedicineRepository medicineRepository;

    public PharmacyController(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @GetMapping("/medicines")
    public ApiResponse<Page<Medicine>> getMedicines(@RequestParam(required=false) String name,
                                                    @RequestParam(required=false) String category,
                                                    @NonNull Pageable pageable) {
        return new ApiResponse<>(true, "Medicines fetched", 
                medicineRepository.searchMedicines(name, category, pageable));
    }

    @PreAuthorize("hasAnyRole('PHARMACIST', 'ADMIN')")
    @PostMapping("/medicines")
    public ApiResponse<Medicine> addMedicine(@NonNull @RequestBody Medicine medicine) {
        return new ApiResponse<>(true, "Added medicine", medicineRepository.save(medicine));
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @PutMapping("/medicines/{id}/stock")
    public ApiResponse<Medicine> adjustStock(@NonNull @PathVariable UUID id, @RequestParam int adjustment) {
        Medicine m = medicineRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        if (m.getStockQuantity() + adjustment < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        m.setStockQuantity(m.getStockQuantity() + adjustment);
        return new ApiResponse<>(true, "Stock adjusted", medicineRepository.save(m));
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @GetMapping("/medicines/low-stock")
    public ApiResponse<List<Medicine>> getLowStock() {
        return new ApiResponse<>(true, "Low stock medicines", medicineRepository.findLowStockMedicines());
    }
}
