package com.swasthyasetu.pharmacy;

import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.common.ResourceNotFoundException;
import com.swasthyasetu.pharmacy.entity.MedicineOrder;
import com.swasthyasetu.pharmacy.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.util.UUID;

@RestController
@RequestMapping("/api/pharmacy/orders")
@PreAuthorize("hasRole('PHARMACIST')")
public class OrderController {
    
    private final MedicineOrderRepository orderRepository;

    public OrderController(MedicineOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private @NonNull UUID getUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Objects.requireNonNull(UUID.fromString(principal));
    }

    @PostMapping
    public ApiResponse<MedicineOrder> createOrder(@NonNull @RequestBody MedicineOrder order) {
        order.setPharmacistId(getUserId());
        return new ApiResponse<>(true, "Order created", orderRepository.save(order));
    }

    @GetMapping
    public ApiResponse<Page<MedicineOrder>> getOrders(@NonNull Pageable pageable) {
        return new ApiResponse<>(true, "Orders fetching", orderRepository.findAll(pageable));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<MedicineOrder> updateStatus(@NonNull @PathVariable UUID id, @RequestParam String status) {
        MedicineOrder order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        OrderStatus newStatus = OrderStatus.valueOf(status);
        if (order.getStatus() == OrderStatus.PENDING && newStatus == OrderStatus.PROCESSING) order.setStatus(newStatus);
        else if (order.getStatus() == OrderStatus.PROCESSING && newStatus == OrderStatus.READY) order.setStatus(newStatus);
        else if (order.getStatus() == OrderStatus.READY && newStatus == OrderStatus.DISPENSED) order.setStatus(newStatus);
        else throw new IllegalStateException("Illegal status transition from " + order.getStatus() + " to " + newStatus);
        
        return new ApiResponse<>(true, "Status updated", orderRepository.save(order));
    }

    @GetMapping("/prescription/{prescriptionId}")
    public ApiResponse<MedicineOrder> getOrderByPrescription(@NonNull @PathVariable UUID prescriptionId) {
        MedicineOrder order = orderRepository.findByPrescriptionId(prescriptionId).orElse(null);
        return new ApiResponse<>(true, "Check completed", order);
    }
}
