package io.github.mangila.ai2.rag;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PokemonRagMapper {

    private final JsonMapper jsonMapper;

    public PokemonRagMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public Map<String, Object> map(ObjectNode objectNode) {
        final Map<String, Object> context = new HashMap<>();
        final ObjectNode pokemonNode = objectNode.get("pokemon").asObject();
        final ObjectNode speciesNode = objectNode.get("species").asObject();
        final ObjectNode evolutionChain = objectNode.get("evolutionChain").asObject();
        sanitizeDescription(speciesNode);
        final List<Object> evolutions = parseEvolutionChain(evolutionChain);
        final Map<String, Object> pokemonMap = jsonMapper.convertValue(pokemonNode, Map.class);
        final Map<String, Object> speciesMap = jsonMapper.convertValue(speciesNode, Map.class);
        context.put("entity", pokemonNode.get("name").asString());
        context.put("identifier", pokemonNode.get("id").asInt());
        context.put("pokemon", pokemonMap);
        context.put("species", speciesMap);
        context.put("evolutions", evolutions);
        return context;
    }

    private void sanitizeDescription(ObjectNode speciesNode) {
        speciesNode.path("flavor_text_entries")
                .valueStream()
                .map(JsonNode::asObject)
                .limit(3)
                .forEach(objectNode -> {
                    boolean isEn = "en".equals(objectNode.path("language").path("name").asText());
                    objectNode.put("is_english", isEn);
                    if (isEn) {
                        String rawText = objectNode.path("flavor_text").asString();
                        String sanitizedText = rawText.replaceAll("[\\n\\f\\r]+", " ")
                                .replaceAll("\\s+", " ")
                                .trim();
                        objectNode.put("flavor_text", sanitizedText);
                    }
                });
    }

    private List<Object> parseEvolutionChain(ObjectNode evolutionChain) {
        JsonNode chainNode = evolutionChain.get("chain");
        ArrayNode evolutions = jsonMapper.createArrayNode();
        int index = 1;
        while (true) {
            String name = chainNode.get("species").get("name").asString();
            String trigger = chainNode.get("evolution_details").get(0).get("trigger").get("name").asText();
            evolutions.add(jsonMapper.createObjectNode()
                    .put("index", index)
                    .put("name", name));
            ArrayNode next = chainNode.get("evolves_to").asArray();
            if (next.isEmpty()) {
                break;
            }
            index++;
            chainNode = next.get(0);
        }
        return jsonMapper.convertValue(evolutions, List.class);
    }
}
