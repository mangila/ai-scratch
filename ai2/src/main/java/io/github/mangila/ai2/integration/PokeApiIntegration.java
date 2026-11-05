package io.github.mangila.ai2.integration;

import io.github.mangila.ai2.config.ShellSessionState;
import org.hibernate.validator.constraints.URL;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.node.ObjectNode;

@Service
@Validated
public class PokeApiIntegration {

    private final RestClient pokeApiClient;

    public PokeApiIntegration(
            ShellSessionState shellSessionState,
            RestClient.Builder builder
    ) {
        this.pokeApiClient = builder
                .baseUrl(shellSessionState.getShellConfig().getPokeapiUrl())
                .build();
    }

    public ObjectNode fetch(@URL(host = "pokeapi.co") String uri) {
        return RestClient.create(uri)
                .get()
                .retrieve()
                .requiredBody(ObjectNode.class);
    }

    public ObjectNode fetchGeneration(Integer generation) {
        return pokeApiClient.get()
                .uri("/generation/{generation}", generation)
                .retrieve()
                .requiredBody(ObjectNode.class);
    }
}
