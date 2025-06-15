package com.bcastilho.example.webserver.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {


    private final DiscoveryClient discoveryClient;
    private final Environment env;

    public WebClientConfig(DiscoveryClient discoveryClient, Environment env) {
        this.discoveryClient = discoveryClient;
        this.env = env;
    }

    @Bean
    @Qualifier("userWebClient")
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "local")
    public WebClient webClientDocker(WebClient.Builder builder) {

        String serviceUrl = env.getProperty("application.local-service-endpoint.user-admin");
        if (serviceUrl == null) {
            throw new IllegalStateException("Service Url not configured for local usage");
        }
        return builder.baseUrl(serviceUrl).build();
    }

    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "docker")
    @Qualifier("userWebClient")
    @Bean
    public WebClient webClientLocal(WebClient.Builder builder) {

        String serviceName = "USER-ADMIN";
        ServiceInstance instance = discoveryClient.getInstances(serviceName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service not found in Eureka: " + serviceName));

        String serviceUrl = instance.getUri().toString();
        return builder.baseUrl(serviceUrl).build();
    }

    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "docker")
    @Qualifier("deviceWebClient")
    @Bean
    public WebClient deviceWebClientDocker(WebClient.Builder builder) {

        String serviceName = "DEVICE-ADMIN";
        ServiceInstance instance = discoveryClient.getInstances(serviceName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Service not found in Eureka " + serviceName));

        String serviceUrl = instance.getUri().toString();
        return builder.baseUrl(serviceUrl).build();
    }

    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "local")
    @Qualifier("deviceWebClient")
    @Bean
    public WebClient deviceWebClientLocal(WebClient.Builder builder) {

        String serviceUrl = env.getProperty("application.local-service-endpoint.device-admin");
        if (serviceUrl == null) {
            throw new IllegalStateException("Service Url not configured for local usage");
        }
        return builder.baseUrl(serviceUrl).build();
    }

}