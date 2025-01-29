## Solana COS On-Demand Sync API

### Overview
The **Solana COS On-Demand Sync API** allows users to copy blockchain archive data from Solana blockchain into their own Tencent Cloud Object Storage (COS) buckets.

## Getting Started

### 1. Grant Write Access to Our Service
To use the sync service, you must first grant write access to our system’s root ID on your Tencent Cloud Object Storage (COS) bucket.

#### Steps to Grant Write Access on Tencent COS:
1. Log in to your [Tencent Cloud Console](https://cloud.tencent.com/).
2. Navigate to **Cloud Object Storage (COS)** and select your bucket.
3. Open the **Permissions** settings -> Bucket ACL
4. Add our **Root ID** with **write access** to the bucket. **Root ID:** `TODO-ADD-ID`
5. Save the changes to apply the permissions.

> **Note:** If your bucket has public write access, no further action is required.

![bucket-acl](https://github.com/user-attachments/assets/20d9567d-6b25-4f66-bdb2-e7580437e38f)

### 2. Initiate a Data Sync Request
Once the necessary permissions are in place, you can initiate a data sync by specifying the desired **start and end block numbers** along with your **Tencent COS bucket name** and **email address** for notifications.

#### Example Request using cURL
```sh
curl -X POST "https://getsolana-api.bwarelabs.com" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "startBlockNumber=1000" \
     -d "endBlockNumber=2000" \
     -d "userEmail=your@email.com" \
     -d "bucketName=test-bucket-6546465464"
```

### 3. Receive Notification
After the sync process completes, you will receive an email confirmation with details about the copied data. Ensure that the provided email address is valid and accessible.

## GDPR Compliance Disclaimer
By providing your email address to initiate the sync process, you acknowledge and accept that we may send you transactional emails related to the completion status of your request. We do not use your email for marketing purposes or share it with third parties. We do not store your email.



