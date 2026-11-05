package io.github.mangila.ai2.command;

import io.github.mangila.ai2.config.ShellSessionState;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
@CommandGroup(name = "Config Commands", prefix = "config")
public class ConfigCommandGroup {

    private final ShellSessionState shellSessionState;
    private final JsonMapper jsonMapper;

    public ConfigCommandGroup(ShellSessionState shellSessionState,
                              JsonMapper jsonMapper) {
        this.shellSessionState = shellSessionState;
        this.jsonMapper = jsonMapper;
    }

    @Command(
            name = "print",
            description = "Show active session configuration"
    )
    public String print() {
        return jsonMapper.convertValue(shellSessionState, JsonNode.class)
                .toPrettyString();
    }
}
