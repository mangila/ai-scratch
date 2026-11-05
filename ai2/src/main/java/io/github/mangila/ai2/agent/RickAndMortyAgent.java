package io.github.mangila.ai2.agent;

import org.springframework.stereotype.Service;

@Service
public class RickAndMortyAgent implements Agent {

    @Override
    public String answer(String message) {
        return "";
    }

    @Override
    public Type getType() {
        return Type.RICK_AND_MORTY;
    }
}
