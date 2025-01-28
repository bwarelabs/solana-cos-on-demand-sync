#!/bin/bash

sudo docker build -t solana-cos-on-demand-sync-development-image -f development.Dockerfile .

sudo docker run --rm --name solana-cos-on-demand-sync-development-container -d \
	-p 444:443 \
  -v "$(pwd)/pom.xml:/app/pom.xml" \
	-v "$(pwd)/src:/app/src" \
  -v "$HOME/.m2:/root/.m2" \
	-it solana-cos-on-demand-sync-development-image /bin/bash
