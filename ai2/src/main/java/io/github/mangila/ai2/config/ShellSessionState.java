package io.github.mangila.ai2.config;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ShellSessionState {

    private final OllamaChatOptions ollamaChatOptions;
    private final ShellConfig shellConfig;
    private final String conversationId;

    public ShellSessionState(OllamaChatModel chatModel,
                             ShellConfig shellConfig) {
        this.ollamaChatOptions = chatModel.getOptions();
        this.shellConfig = shellConfig;
        this.conversationId = UUID.randomUUID().toString();
    }

    public ShellConfig getShellConfig() {
        return shellConfig;
    }

    public OllamaChatOptions getOllamaChatOptions() {
        return ollamaChatOptions;
    }

    public String getConversationId() {
        return conversationId;
    }

}
