#!/bin/bash
set -e  # Exit immediately if any command fails

echo "Building Docker image..."
sudo docker build -t solana-syncer .

echo "Starting Solana Syncer API container..."
sudo docker run --rm --name solana-syncer-job -d \
  -p 443:443 \
  solana-syncer



#sudo docker build -t solana-syncer .
#
#sudo docker run --rm --name solana-syncer-job -d \
#	-p 443:443 \
#  -v "$(pwd)/pom.xml:/app/pom.xml" \
#	-v "$(pwd)/src:/app/src" \
#  -v "$HOME/.m2:/root/.m2" \
#	-it solana-syncer /bin/bash
#
##	-it solana-syncer /bin/bash