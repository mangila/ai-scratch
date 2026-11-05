package io.github.mangila.ai2.command;

import io.github.mangila.ai2.service.PokemonService;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@CommandGroup(name = "Data Commands", prefix = "data")
public class DataCommandGroup {

    private final PokemonService pokemonService;

    public DataCommandGroup(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @Command(
            name = "pokemon",
            description = "Ingest some pokemon data"
    )
    public String pokemon(
            @Option(description = "The pokemon generation",
                    shortName = 'g',
                    defaultValue = "1",
                    required = false)
            Integer generation
    ) {
        pokemonService.insertGeneration(generation);
        return "OK";
    }

    @Command(
            name = "rickandmorty",
            description = "Ingest some Rick and Morty data"
    )
    public String rickAndMorty() {
        return "OK";
    }

    @Command(
            name = "swapi",
            description = "Ingest some Star Wars data"
    )
    public String starwars() {
        return "OK";
    }

}
