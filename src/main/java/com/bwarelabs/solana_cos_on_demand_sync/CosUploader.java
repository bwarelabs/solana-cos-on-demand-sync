package com.bwarelabs.solana_cos_on_demand_sync;

import com.bwarelabs.solana_cos_on_demand_sync.exceptions.InternalErrorException;
import com.bwarelabs.solana_cos_on_demand_sync.exceptions.UserTypeException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.CopyObjectRequest;
import com.qcloud.cos.transfer.Copy;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class CosUploader {
    private static final Logger logger = Logger.getLogger(CosUploader.class.getName());
    private static final int THREAD_COUNT = 32;
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private final String sourceBucket;
    private final String destinationBucket;
    private final Region sourceRegion;
    private final TransferManager transferManager;

    public CosUploader(String sourceBucket, String destinationBucket,
            String sourceRegion, String secretId, String secretKey) {
        this.sourceBucket = sourceBucket;
        this.destinationBucket = destinationBucket;

        System.out.println("secretId: " + secretId);
        System.out.println("secretKey: " + secretKey);

        this.sourceRegion = new Region(sourceRegion);

        COSClient cosClient = createCOSClient(secretId, secretKey, this.sourceRegion);

        ExecutorService transferManagerExecutorService = Executors.newFixedThreadPool(THREAD_COUNT);
        this.transferManager = new TransferManager(cosClient, transferManagerExecutorService);
    }

    private COSClient createCOSClient(String secretId, String secretKey, Region region) {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(region);
        return new COSClient(credentials, clientConfig);
    }

    public void batchCopyObjects(List<String> objectKeys, String destinationPrefix) throws Exception {
        int totalObjects = objectKeys.size();
        logger.info("Starting batch copy for " + totalObjects + " objects...");

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executorService);

        int submittedTasks = 0;
        int completedTasks = 0;
        List<String> userTypeErrors = new ArrayList<>();
        List<String> internalErrors = new ArrayList<>();

        for (; submittedTasks < Math.min(BATCH_SIZE, totalObjects); submittedTasks++) {
            String key = objectKeys.get(submittedTasks);
            String destinationKey = destinationPrefix + key;
            completionService.submit(() -> {
                try {
                    copyByKey(key, destinationKey);
                } catch (UserTypeException e) {
                    logger.severe("User error: " + e.getMessage());
                    synchronized (userTypeErrors) {
                        userTypeErrors.add(e.getMessage());
                    }
                } catch (Exception e) {
                    logger.severe("Internal error copying " + key + ": " + e.getMessage());
                    synchronized (internalErrors) {
                        internalErrors.add("Failed to copy " + key + ": " + e.getMessage());
                    }
                }
                return null;
            });
        }

        while (completedTasks < totalObjects) {
            try {
                completionService.take().get();
                completedTasks++;

                if (submittedTasks < totalObjects) {
                    String key = objectKeys.get(submittedTasks);
                    String destinationKey = destinationPrefix + key;
                    completionService.submit(() -> {
                        try {
                            copyByKey(key, destinationKey);
                        } catch (UserTypeException e) {
                            logger.severe("User error: " + e.getMessage());
                            synchronized (userTypeErrors) {
                                userTypeErrors.add(e.getMessage());
                            }
                        } catch (Exception e) {
                            logger.severe("Internal error copying " + key + ": " + e.getMessage());
                            synchronized (internalErrors) {
                                internalErrors.add("Failed to copy " + key + ": " + e.getMessage());
                            }
                        }
                        return null;
                    });
                    submittedTasks++;
                    logger.info("Submitted new task for " + key);
                }

                logger.info("Batch copy progress: " + completedTasks + " out of " + totalObjects + " objects copied");

            } catch (Exception e) {
                logger.severe("Error waiting for copy task completion: " + e.getMessage());
            }
        }

        executorService.shutdown();
        logger.info("Batch copy completed");

        if (!userTypeErrors.isEmpty() || !internalErrors.isEmpty()) {
            String errorMessage = "";

            if (!userTypeErrors.isEmpty()) {
                errorMessage +=  String.join("\n", userTypeErrors) + "\n";
            }
            if (!internalErrors.isEmpty()) {
                errorMessage += String.join("\n", internalErrors);
            }

            if (!userTypeErrors.isEmpty()) {
                throw new UserTypeException(errorMessage); // **Throw UserTypeException if user errors exist**
            } else {
                throw new InternalErrorException(errorMessage, null); // **Otherwise, throw InternalErrorException**
            }
        }
    }

    /**
     * Copies a single object from the source COS bucket to the destination COS
     * bucket.
     */
    private void copyByKey(String sourceKey, String destinationKey) throws Exception {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                CopyObjectRequest copyRequest = new CopyObjectRequest(sourceRegion, sourceBucket, sourceKey,
                        destinationBucket, destinationKey);
                Copy copy = transferManager.copy(copyRequest);
                copy.waitForCopyResult();
                logger.info("Successfully copied " + sourceKey + " to " + destinationKey);
                return;
            } catch (CosServiceException e) {
                if (e.getStatusCode() == 404) {
                    logger.severe("Object not found: " + sourceKey);
                    throw new UserTypeException("Error: The object '" + sourceKey + "' does not exist.");
                }
                logger.severe(
                        "COS Service Exception copying " + sourceKey + " to " + destinationKey + ": " + e.getMessage());
            } catch (Exception e) {
                logger.severe("Retrying " + sourceKey + " (attempt " + (attempts + 1) + " of " + MAX_RETRIES + ")");
            }

            attempts++;
            if (attempts < MAX_RETRIES) {
                int backoff = (int) Math.pow(2, attempts) * RETRY_DELAY_MS;
                logger.info("Retrying " + sourceKey + " in " + backoff + " ms");
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new InternalErrorException("Max retries reached for " + sourceKey, null);
    }
}
