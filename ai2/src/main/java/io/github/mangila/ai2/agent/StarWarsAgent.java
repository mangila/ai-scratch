package io.github.mangila.ai2.agent;

import org.springframework.stereotype.Service;

@Service
public class StarWarsAgent implements Agent {

    @Override
    public String answer(String message) {
        return "";
    }

    @Override
    public Type getType() {
        return Type.STAR_WARS;
    }
}
