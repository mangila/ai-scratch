package io.github.mangila.ai2.rag;

import io.github.mangila.ai2.integration.PokeApiIntegration;
import io.github.mangila.ai2.provider.TemplateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class PokemonRagEtl implements RagEtl {

    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonRagEtl.class);

    private final JsonMapper jsonMapper;
    private final PokeApiIntegration pokeApiIntegration;
    private final RagService ragService;
    private final PokemonRagMapper pokemonRagMapper;

    public PokemonRagEtl(JsonMapper jsonMapper,
                         PokeApiIntegration pokeApiIntegration,
                         RagService ragService,
                         PokemonRagMapper pokemonRagMapper) {
        this.jsonMapper = jsonMapper;
        this.pokeApiIntegration = pokeApiIntegration;
        this.ragService = ragService;
        this.pokemonRagMapper = pokemonRagMapper;
    }

    @Override
    public ObjectNode extract(Map<String, Object> attributes) {
        String url = attributes.get("url").toString();
        final ObjectNode speciesNode = pokeApiIntegration.fetch(url);
        final String evolutionChainUrl = speciesNode.get("evolution_chain")
                .get("url")
                .asStringOpt()
                .orElseThrow();
        final ObjectNode evolutionChainNode = pokeApiIntegration.fetch(evolutionChainUrl);
        final String pokemonUrl = speciesNode.get("varieties")
                .valueStream()
                .filter(jsonNode -> jsonNode.get("is_default").asBoolean())
                .map(jsonNode -> jsonNode.get("pokemon"))
                .map(jsonNode -> jsonNode.get("url").asString())
                .findFirst()
                .orElseThrow();
        final ObjectNode pokemonNode = pokeApiIntegration.fetch(pokemonUrl);
        ObjectNode node = jsonMapper.createObjectNode();
        node.putIfAbsent("pokemon", pokemonNode);
        node.putIfAbsent("species", speciesNode);
        node.putIfAbsent("evolutionChain", evolutionChainNode);
        return node;
    }

    @Override
    public void load(Map<String, Object> templateContext) {
        final Map<String, Object> ragMetadata = new HashMap<>();
        final Object entity = templateContext.get("entity");
        if (entity == null) {
            throw new IllegalArgumentException("entity is required");
        }
        final Object identifier = templateContext.get("identifier");
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is required");
        }
        ragMetadata.put("domain", "pokemon");
        ragMetadata.put("source", "pokeapi");
        ragMetadata.put("entity", entity);
        ragMetadata.put("identifier", identifier);
        ragService.insertMarkdown(TemplateProvider.TemplateType.POKEMON_RAG, templateContext, ragMetadata);
    }

    @Override
    public Map<String, Object> transform(ObjectNode objectNode) {
        return pokemonRagMapper.map(objectNode);
    }

    @Override
    public void run(Map<String, Object> attributes) {
        LOGGER.info("Running: {} with attributes: {}", getType(), attributes);
        final ObjectNode extract = this.extract(attributes);
        final Map<String, Object> transform = this.transform(extract);
        this.load(transform);
    }

    @Override
    public Type getType() {
        return Type.POKEMON;
    }
}
