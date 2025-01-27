package com.bwarelabs.solana_cos_on_demand_sync.service;

import com.bwarelabs.solana_cos_on_demand_sync.CosUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class CosCopierService {
    private static final Logger logger = Logger.getLogger(CosCopierService.class.getName());
    private static final long INITIAL_SYNC_THRESHOLD = 285958000L;
    private static final long INITIAL_SYNC_INCREMENT = 49999L;
    private static final long LIVE_SYNC_INCREMENT = 1000L;
    final long ENTRIES_START_THRESHOLD = 263300000L;  // 000000000fb1a3a0 in hex

    private final String sourceBucket;
    private final String sourceRegion;
    private final String secretId;
    private final String secretKey;


    public CosCopierService(
            @Value("${cos.source.bucket}") String sourceBucket,
            @Value("${cos.source.region}") String sourceRegion,
            @Value("${cos.secretId}") String secretId,
            @Value("${cos.secretKey}") String secretKey
    ) {
        this.sourceBucket = sourceBucket;
        this.sourceRegion = sourceRegion;
        this.secretId = secretId;
        this.secretKey = secretKey;
    }

    @Autowired
    private EmailService emailService;  // Inject Email Service

    @Async  // Runs the task asynchronously
    public void copyObjectsAsync(int startKey, int endKey, String destinationBucketName, String destinationPrefixPath, String userEmail) {
        String taskId = UUID.randomUUID().toString(); // Unique Task ID
        logger.info("Starting async copy process with Task ID: " + taskId);

        try {
            CosUploader cosUploader = new CosUploader(this.sourceBucket, destinationBucketName, this.sourceRegion, this.secretId, this.secretKey);

            final int BATCH_SIZE = 10_000;
            long currentStart = startKey;

            while (currentStart < endKey) {
                long nextBatchEnd = Math.min(currentStart + (BATCH_SIZE * 1000), endKey);

                logger.info("Generating keys from " + currentStart + " to " + nextBatchEnd);
                List<String> objectKeys = generateObjectKeys(currentStart, nextBatchEnd);

                // print the objectKeys
//                for (String key : objectKeys) {
//                    System.out.println(key);
//                }

                logger.info("Starting copy process for " + objectKeys.size() + " objects for task ID: " + taskId);
                cosUploader.batchCopyObjects(objectKeys, destinationPrefixPath);

                logger.info("One batch completed. Moving to next batch for task ID: " + taskId);
                currentStart = nextBatchEnd; // Move to next batch
            }

            // Task completed successfully
            logger.info("Copy process completed successfully for task ID: " + taskId);
            emailService.sendEmail(userEmail, "Copy Task Completed", "Your copy process has successfully completed.");

        } catch (Exception e) {
            logger.severe("Error in async copy: " + e.getMessage() + " for task ID: " + taskId);
            emailService.sendEmail(userEmail, "Copy Task Failed", "There was an error processing your copy request.");
        }
    }

    public List<String> generateObjectKeys(long startBoundary, long endBoundary) {
        List<String> objectKeys = new ArrayList<>();

        long i = adjustStartBoundary(startBoundary);

        while (i < endBoundary) {
            String syncType;
            long increment;

            if (i < INITIAL_SYNC_THRESHOLD) {
                syncType = "initial_sync";
                increment = INITIAL_SYNC_INCREMENT;
            } else {
                syncType = "live_sync";
                increment = LIVE_SYNC_INCREMENT;
            }

            long nextBoundary = i + increment;
            String hexStart = String.format("%016x", i);
            String hexEnd = String.format("%016x", nextBoundary);

            objectKeys.add(String.format("%s/blocks/range_%s_%s/blocks.seq", syncType, hexStart, hexEnd));

            // Generate entries only if `i >= 263300000`
            if (i >= ENTRIES_START_THRESHOLD) {
                objectKeys.add(String.format("%s/entries/range_%s_%s/entries.seq", syncType, hexStart, hexEnd));
            }

            /*
              Since the initial_sync uses 0 -> 49999 keys, we need to increment by 1
            */
            if (i < INITIAL_SYNC_THRESHOLD) {
                nextBoundary += 1;
            }

            i = nextBoundary;
        }

        return objectKeys;
    }

    private long adjustStartBoundary(long startBoundary) {
        if (startBoundary < INITIAL_SYNC_THRESHOLD) {
            return (startBoundary / 50000) * 50000;
        } else {
            return (startBoundary / 1000) * 1000;
        }
    }

    @PreDestroy
    public void shutdown() {
//        transferManager.shutdownNow();
//        cosClient.shutdown();
    }
}
