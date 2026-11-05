package io.github.mangila.ai2.command;

import io.github.mangila.ai2.agent.AgentDispatcher;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class ChatCommand {

    private final AgentDispatcher agentDispatcher;

    public ChatCommand(AgentDispatcher agentDispatcher) {
        this.agentDispatcher = agentDispatcher;
    }

    @Command(
            name = "chat",
            description = "Send a prompt to the active local Ollama model"
    )
    public String chat(
            @Option(description = "The message/prompt to send",
                    shortName = 'm',
                    required = true)
            String message
    ) {
        try {
            return agentDispatcher.chat(message);
        } catch (Exception e) {
            return "Error communicating with Ollama: Is your local server running? (" + e.getMessage() + ")";
        }
    }
}
