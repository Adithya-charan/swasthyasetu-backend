package com.swasthyasetu.record;

import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.record.entity.MedicalRecord;
import com.swasthyasetu.record.entity.RecordType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    private final StorageService storageService;
    private final MedicalRecordRepository recordRepository;

    public RecordController(StorageService storageService, MedicalRecordRepository recordRepository) {
        this.storageService = storageService;
        this.recordRepository = recordRepository;
    }

    private UUID getUserId() {
        return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @PostMapping
    public ApiResponse<MedicalRecord> uploadRecord(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("patientId") UUID patientId,
                                                   @RequestParam("type") String type,
                                                   @RequestParam(required=false) String description) {
        String url = storageService.storeFile(file);
        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patientId);
        record.setUploadedByUserId(getUserId());
        record.setType(RecordType.valueOf(type));
        record.setFileName(file.getOriginalFilename());
        record.setFileSizeBytes(file.getSize());
        record.setFileUrl(url);
        record.setDescription(description);
        return new ApiResponse<>(true, "Uploaded successfully", recordRepository.save(record));
    }

    @GetMapping("/my")
    public ApiResponse<List<MedicalRecord>> getMyRecords() {
        return new ApiResponse<>(true, "Records", recordRepository.findByPatientIdOrderByUploadedAtDesc(getUserId()));
    }
}
