package io.github.mangila.ai2.service;

import io.github.mangila.ai2.config.ShellSessionState;
import io.github.mangila.ai2.integration.PokeApiIntegration;
import io.github.mangila.ai2.provider.RagEtlProvider;
import io.github.mangila.ai2.rag.RagEtl;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Validated
public class PokemonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PokemonService.class);

    private final ShellSessionState shellSessionState;
    private final JobScheduler jobScheduler;
    private final RagEtlProvider ragEtlProvider;
    private final PokeApiIntegration pokeApiIntegration;

    public PokemonService(
            ShellSessionState shellSessionState,
            JobScheduler jobScheduler,
            RagEtlProvider ragEtlProvider,
            PokeApiIntegration pokeApiIntegration
    ) {
        this.shellSessionState = shellSessionState;
        this.jobScheduler = jobScheduler;
        this.ragEtlProvider = ragEtlProvider;
        this.pokeApiIntegration = pokeApiIntegration;
    }

    public List<JobId> enqueueEtlWithGeneration(RagEtl.Type type, Integer generation) {
        final boolean debug = shellSessionState.getShellConfig().isDebug();
        final Stream<JsonNode> stream = pokeApiIntegration.fetchGeneration(generation)
                .map(jsonNode -> jsonNode.get("pokemon_species"))
                .valueStream();
        final List<String> urls = (debug ? stream.limit(3) : stream)
                .map(JsonNode::asObject)
                .map(pokemonSpeciesNode -> pokemonSpeciesNode.path("url").asString())
                .toList();
        final RagEtl ragEtl = ragEtlProvider.get(type);
        final List<JobId> jobIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            LOGGER.info("Enqueued RAG ETL: {}", url);
            final JobId jobId = jobScheduler.enqueue(() -> {
                final Map<String, Object> attributes = Map.of("url", url);
                ragEtl.run(attributes);
            });
            jobIds.add(jobId);
        }
        return jobIds;
    }
}
