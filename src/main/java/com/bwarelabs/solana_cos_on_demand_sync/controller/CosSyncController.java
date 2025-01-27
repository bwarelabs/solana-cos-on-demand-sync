package com.bwarelabs.solana_cos_on_demand_sync.controller;

import com.bwarelabs.solana_cos_on_demand_sync.service.CosCopierService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sync")
@Validated
public class CosSyncController {
    private final CosCopierService cosCopierService;

    public CosSyncController(CosCopierService cosCopierService) {
        this.cosCopierService = cosCopierService;
    }

    @PostMapping
    public ResponseEntity<String> startCopyProcess(
            @RequestParam @Min(0) int startBlockNumber,
            @RequestParam @Min(0) int endBlockNumber,
            @RequestParam @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,63}$") String bucketName,
            @RequestParam @NotBlank String userEmail,
            @RequestParam(required = false, defaultValue = "") String pathPrefix) {

            if (startBlockNumber > endBlockNumber) {
            throw new IllegalArgumentException("startBlockNumber cannot be greater than endBlockNumber");
        }

        cosCopierService.copyObjectsAsync(startBlockNumber, endBlockNumber, bucketName, pathPrefix, userEmail);
        return ResponseEntity.ok("Copy process started asynchronously. You will receive an email upon completion.");
    }
}
