package io.github.mangila.ai2;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class ChatCommand {

    private final ShellSessionState shellSessionState;
    private final ChatClient chatClient;

    public ChatCommand(ShellSessionState shellSessionState,
                       ChatClient.Builder builder
    ) {
        this.shellSessionState = shellSessionState;
        this.chatClient = builder.build();
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
            return chatClient.prompt()
                    .system(shellSessionState.systemContex())
                    .options(shellSessionState.options().mutate())
                    .user(message)
                    .call()
                    .content();
        } catch (Exception e) {
            return "Error communicating with Ollama: Is your local server running? (" + e.getMessage() + ")";
        }
    }
}
