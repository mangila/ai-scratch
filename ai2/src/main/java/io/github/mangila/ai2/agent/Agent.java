package io.github.mangila.ai2.agent;

public interface Agent {

    String answer(String message);

    Type getType();

    enum Type {
        FILE_SYSTEM,
        GAME_OF_THRONES,
        GENERAL_PURPOSE,
        POKEMON,
        RICK_AND_MORTY,
        STAR_WARS
    }
}
