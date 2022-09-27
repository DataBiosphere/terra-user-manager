package bio.terra.user.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user.sam")
public record SamConfiguration(String basePath, String resourceId) {}
