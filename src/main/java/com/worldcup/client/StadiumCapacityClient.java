package com.worldcup.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class StadiumCapacityClient {

    private final WebClient webClient;

    public StadiumCapacityClient(@Value("${stadium.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public Integer fetchCapacity(String stadiumCode) {
        try {
            return webClient.get()
                .uri("/api/matches/stadiums/{code}/capacity", stadiumCode)
                .retrieve()
                .bodyToMono(Integer.class)
                .block();
        } catch (Exception e) {
            log.warn("Could not fetch capacity for stadiumCode {}: {}", stadiumCode, e.getMessage());
            return null;
        }
    }
}
