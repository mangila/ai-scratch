package io.github.mangila.ai2.model;

import java.util.List;

public record Output(
        List<String> entities,
        List<Integer> identifiers,
        String content
) {
}
