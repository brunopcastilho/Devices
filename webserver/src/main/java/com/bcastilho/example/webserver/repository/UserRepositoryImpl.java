package com.bcastilho.example.webserver.repository;


import com.bcastilho.example.webserver.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final WebClient webClient;

    public UserRepositoryImpl(@Qualifier("userWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<User> getUser(Integer id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/user").queryParam("id", id).build())
                .retrieve()
                .bodyToMono(User.class);
    }

    @Override
    public Mono<User> createUser(User user) {
        return webClient.post()
                .uri("/v1/user")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(User.class);
    }

    @Override
    public Mono<List<User>> getUsers() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/user/all").build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<User>>() {
                });

    }
}