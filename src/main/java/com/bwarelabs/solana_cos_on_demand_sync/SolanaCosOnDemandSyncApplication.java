package com.bwarelabs.solana_cos_on_demand_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SolanaCosOnDemandSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolanaCosOnDemandSyncApplication.class, args);
	}

}
