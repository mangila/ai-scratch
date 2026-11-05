package io.github.mangila.ai2.command;

import io.github.mangila.ai2.agent.Agent;
import io.github.mangila.ai2.provider.AgentProvider;
import io.github.mangila.ai2.rag.RagEtl;
import io.github.mangila.ai2.service.PokemonService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jobrunr.jobs.JobId;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@CommandGroup(
        name = "pokemon",
        description = "Commands related to Pokemon",
        prefix = "pokemon")
public class PokemonCommandGroup {

    private final AgentProvider agentProvider;
    private final PokemonService pokemonService;

    public PokemonCommandGroup(AgentProvider agentProvider,
                               PokemonService pokemonService) {
        this.agentProvider = agentProvider;
        this.pokemonService = pokemonService;
    }

    @Command(
            name = "chat",
            alias = "c",
            description = "Chat with the Pokemon AI"
    )
    public String chat(
            @Option(description = "Message to send to the Pokemon AI",
                    longName = "message",
                    shortName = 'm',
                    required = true
            ) String message
    ) {
        final Agent agent = agentProvider.get(Agent.Type.POKEMON);
        return agent.answer(message);
    }

    @Command(
            name = "rag",
            alias = "r",
            description = "Ingest RAG data into the Pokemon AI vectorstore"
    )
    public String rag(
            @Option(description = "The pokemon generation number",
                    shortName = 'g',
                    defaultValue = "1",
                    required = false)
            @Min(1) @Max(9) Integer generation
    ) {
        final List<JobId> _ = pokemonService.enqueueEtlWithGeneration(RagEtl.Type.POKEMON, generation);
        return "OK";
    }

}
