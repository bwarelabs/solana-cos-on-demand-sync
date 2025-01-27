sudo docker build -t solana-syncer .

sudo docker run --rm --name solana-syncer-job \
	-e JVM_ARGS="-Xmx8g" \
	-v "$(pwd)/config.properties:/app/config.properties" \
	-v "$(pwd)/src:/app/src" \
	-it solana-syncer /bin/bash