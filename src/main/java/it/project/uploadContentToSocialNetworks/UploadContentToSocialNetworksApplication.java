package it.project.uploadContentToSocialNetworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan(basePackages = {"it.project.uploadContentToSocialNetworks.config.property"})
public class UploadContentToSocialNetworksApplication {

	static void main(String[] args) {
		SpringApplication.run(UploadContentToSocialNetworksApplication.class, args);
	}

}
