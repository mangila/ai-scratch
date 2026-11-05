package io.github.mangila.ai2;

import org.intellij.lang.annotations.Language;
import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionProperties;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
@CommandGroup(name = "Ollama Commands", prefix = "ollama")
public class OllamaCommandGroup {

    private final ShellSessionState shellSessionState;
    private final RestClient ollamaClient;

    public OllamaCommandGroup(ShellSessionState shellSessionState,
                              OllamaConnectionProperties ollamaConnectionProperties,
                              RestClient.Builder restClientBuilder
    ) {
        this.shellSessionState = shellSessionState;
        this.ollamaClient = restClientBuilder
                .baseUrl(ollamaConnectionProperties.getBaseUrl())
                .build();
    }

    @Command(name = "info",
            description = "Get current model info",
            help = "A command that sends HTTP request to '/api/show' with the current active model")
    public String info() {
        @Language("JSON") final String payload = """
                {
                "model" : "%s"
                }
                """.formatted(shellSessionState.options().getModel());
        final JsonNode response = ollamaClient.post()
                .uri("/api/show")
                .body(payload)
                .retrieve()
                .requiredBody(JsonNode.class);
        return response.toPrettyString();
    }
}
