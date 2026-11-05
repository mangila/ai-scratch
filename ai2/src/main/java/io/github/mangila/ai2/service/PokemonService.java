package io.github.mangila.ai2.service;

import com.samskivert.mustache.Template;
import io.github.mangila.ai2.config.ShellSessionState;
import io.github.mangila.ai2.integration.PokeApiIntegration;
import io.github.mangila.ai2.service.TemplateProvider.TemplateType;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Validated
public class PokemonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonService.class);

    private final JsonMapper jsonMapper;
    private final PokeApiIntegration pokeApiIntegration;
    private final VectorStore vectorStore;
    private final Template ragTemplate;
    private final MarkdownDocumentReaderConfig markdownDocumentReaderConfig;
    private final Path markdownDocumentPath;
    private final boolean debug;

    public PokemonService(
            JsonMapper jsonMapper,
            ShellSessionState shellSessionState,
            PokeApiIntegration pokeApiIntegration,
            @Qualifier("pokemonVectorStore") VectorStore vectorStore,
            TemplateProvider templateProvider
    ) {
        this.jsonMapper = jsonMapper;
        this.pokeApiIntegration = pokeApiIntegration;
        this.vectorStore = vectorStore;
        this.ragTemplate = templateProvider.get(TemplateType.POKEMON_RAG);
        this.markdownDocumentReaderConfig = MarkdownDocumentReaderConfig.builder()
                .withIncludeBlockquote(true)
                .withHorizontalRuleCreateDocument(true)
                .build();
        this.markdownDocumentPath = shellSessionState.getShellConfig().getStorageDir();
        this.debug = shellSessionState.getShellConfig().isDebug();
    }

    public void insertPokemon(@NotBlank(message = "Pokemon name cannot be blank") String pokemonName) {
        pokemonName = pokemonName.toLowerCase(Locale.ROOT);
        final ObjectNode fetchedPokemonByName = pokeApiIntegration.fetchPokemonByName(pokemonName);
        final Map<String, Object> context = createTemplateContext(fetchedPokemonByName);
        final String markdown = ragTemplate.execute(context);
        final byte[] markdownBytes = markdown.getBytes(StandardCharsets.UTF_8);
        final Resource markdownResource = new ByteArrayResource(markdownBytes);
        final MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(
                markdownResource,
                markdownDocumentReaderConfig
        );
        final List<Document> documents = markdownDocumentReader.get();
        vectorStore.add(documents);
        if (debug) {
            final Path out = markdownDocumentPath.resolve("pokemon-%s.md".formatted(pokemonName));
            LOGGER.info("Writing markdown document to {}", out);
            try {
                Files.writeString(out, markdown);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private @NonNull Map<String, Object> createTemplateContext(ObjectNode fetchedPokemonByName) {
        final Map<String, Object> context = new HashMap<>();
        final ObjectNode pokemonNode = fetchedPokemonByName.get("pokemon").asObject();
        final ObjectNode speciesNode = fetchedPokemonByName.get("species").asObject();
        sanitizeDescription(speciesNode);
        final Map<String, Object> pokemonMap = jsonMapper.convertValue(pokemonNode, Map.class);
        final Map<String, Object> speciesMap = jsonMapper.convertValue(speciesNode, Map.class);
        context.put("pokemon", pokemonMap);
        context.put("species", speciesMap);
        return context;
    }

    private void sanitizeDescription(ObjectNode speciesNode) {
        ArrayNode entries = (ArrayNode) speciesNode.path("flavor_text_entries");
        for (JsonNode entry : entries) {
            ObjectNode entryObj = (ObjectNode) entry;
            boolean isEn = "en".equals(entryObj.path("language").path("name").asText());
            entryObj.put("is_english", isEn);
            if (isEn) {
                String rawText = entryObj.path("flavor_text").asString();
                String sanitizedText = rawText.replaceAll("[\\n\\f\\r]+", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
                entryObj.put("flavor_text", sanitizedText);
            }
        }
    }

    public void insertGeneration(Integer generation) {
        final ObjectNode pokemonByGeneration = pokeApiIntegration.fetchPokemonByGeneration(generation);
        LOGGER.info("Inserting generation {}", pokemonByGeneration.get("name"));
        final ArrayNode pokemonSpecies = pokemonByGeneration.get("pokemon_species").asArray();
        pokemonSpecies
                .valueStream()
                .limit(1)
                .forEach(pokemonSpeciesNode -> {
                    LOGGER.info("Inserting pokemon {}", pokemonSpeciesNode);
                    final String pokemonName = pokemonSpeciesNode.asObject()
                            .get("name")
                            .asString();
                    insertPokemon(pokemonName);
                });
    }
}
