#!/bin/bash
set -e  # Exit immediately if any command fails

echo "Building Docker image..."
sudo docker build -t solana-cos-on-demand-sync-image .

echo "Starting Solana Syncer API container..."
sudo docker run --rm --name solana-cos-on-demand-sync-container -d \
  -p 443:443 \
  solana-cos-on-demand-sync-image
