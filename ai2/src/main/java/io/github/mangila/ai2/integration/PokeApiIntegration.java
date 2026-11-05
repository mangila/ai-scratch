package io.github.mangila.ai2.integration;

import io.github.mangila.ai2.config.ShellSessionState;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

@Service
@Validated
public class PokeApiIntegration {

    private final JsonMapper jsonMapper;
    private final RestClient pokeApiClient;

    public PokeApiIntegration(
            JsonMapper jsonMapper,
            ShellSessionState shellSessionState,
            RestClient.Builder builder
    ) {
        this.jsonMapper = jsonMapper;
        this.pokeApiClient = builder
                .baseUrl(shellSessionState.getShellConfig().getPokeapiUrl())
                .build();
    }

    public ObjectNode fetchPokemonByName(@NotBlank(message = "Pokemon name cannot be blank") String pokemonName) {
        ObjectNode pokemonNode =  pokeApiClient.get()
                .uri("/pokemon/{name}", pokemonName)
                .retrieve()
                .requiredBody(ObjectNode.class);
        ObjectNode speciesNode = pokeApiClient.get()
                .uri("/pokemon-species/{name}", pokemonName)
                .retrieve()
                .requiredBody(ObjectNode.class);
        ObjectNode node = jsonMapper.createObjectNode();
        node.putIfAbsent("pokemon", pokemonNode);
        node.putIfAbsent("species", speciesNode);
        return node;
    }

    public ObjectNode fetchPokemonByGeneration(Integer generation) {
        return pokeApiClient.get()
                .uri("/generation/{generation}", generation)
                .retrieve()
                .requiredBody(ObjectNode.class);
    }
}
