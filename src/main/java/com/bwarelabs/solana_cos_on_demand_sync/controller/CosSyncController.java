package com.bwarelabs.solana_cos_on_demand_sync.controller;

import com.bwarelabs.solana_cos_on_demand_sync.service.CosCopierService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@Validated
public class CosSyncController {
    private final CosCopierService cosCopierService;

    public CosSyncController(CosCopierService cosCopierService) {
        this.cosCopierService = cosCopierService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> startCopyProcess(
            @RequestParam @Min(0) int startBlockNumber,
            @RequestParam @Min(0) int endBlockNumber,
            @RequestParam @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,63}$") String bucketName,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false, defaultValue = "") String pathPrefix) {

        if (startBlockNumber > endBlockNumber) {
            throw new IllegalArgumentException("startBlockNumber cannot be greater than endBlockNumber");
        }

        if (!cosCopierService.bucketExists(bucketName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bucket does not exist",
                    "message", "The specified bucket does not exist or cannot be accessed."));
        }

        // Validate write access
        if (!cosCopierService.canWriteToBucket(bucketName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Write access denied", "message", "Insufficient permissions to write to the specified bucket."));
        }

        cosCopierService.copyObjectsAsync(startBlockNumber, endBlockNumber, bucketName, pathPrefix, userEmail);
        return ResponseEntity.ok(Map.of("message", "Copy process started asynchronously.", "details", "You will receive an email upon completion."));
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the Solana COS Sync API.");
        response.put("usage", "To start a sync process, send a POST request to '/' with the required parameters.");
        response.put("method", "POST");
        response.put("endpoint", "/");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("startBlockNumber", "Integer - The starting block number.");
        parameters.put("endBlockNumber", "Integer - The ending block number (must be greater than startBlockNumber).");
        parameters.put("bucketName",
                "String (3-63 characters, lowercase letters, numbers, and hyphens only) - The destination bucket.");
        parameters.put("userEmail", "String (Optional) (Valid email) - The email to receive notifications.");
        parameters.put("pathPrefix", "String (Optional) - Prefix for object storage.");

        response.put("parameters", parameters);

        return ResponseEntity.ok(response);
    }
}
