package io.github.mangila.ai2.agent;

import org.springframework.stereotype.Service;

@Service
public class GameOfThronesAgent implements Agent {

    @Override
    public String answer(String message) {
        return "";
    }

    @Override
    public Type getType() {
        return Type.GAME_OF_THRONES;
    }
}
