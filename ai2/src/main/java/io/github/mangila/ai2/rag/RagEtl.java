package io.github.mangila.ai2.rag;

import tools.jackson.databind.node.ObjectNode;

import java.util.Map;

public interface RagEtl {

    ObjectNode extract(Map<String, Object> attributes);

    void load(Map<String, Object> templateContext);

    Map<String, Object> transform(ObjectNode objectNode);

    void run(Map<String, Object> attributes);

    Type getType();

    public enum Type {
        POKEMON,
        GAME_OF_THRONES,
        RICK_AND_MORTY,
        STAR_WARS
    }

}
