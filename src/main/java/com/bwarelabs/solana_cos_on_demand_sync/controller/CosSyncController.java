package com.bwarelabs.solana_cos_on_demand_sync.controller;

import com.bwarelabs.solana_cos_on_demand_sync.service.CosCopierService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    public String copyObjects(
            @RequestParam @Min(0) int startBlockNumber, // Ensures non-negative values
            @RequestParam @Min(0) int endBlockNumber, // Ensures non-negative values
            @RequestParam @NotBlank String bucketName, // Ensures non-empty value
            @RequestParam(required = false) String pathPrefix) { // Ensures non-empty value

        if (startBlockNumber > endBlockNumber) {
            throw new IllegalArgumentException("startBlockNumber cannot be greater than endBlockNumber");
        }

        cosCopierService.copyObjects(startBlockNumber, endBlockNumber, bucketName, pathPrefix);
        return "Copy process started from block " + startBlockNumber + " to " + endBlockNumber +
                " in bucket " + bucketName + (pathPrefix != null ? " under " + pathPrefix : " at root path");
    }
}
