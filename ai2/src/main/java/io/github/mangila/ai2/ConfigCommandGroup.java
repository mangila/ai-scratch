package io.github.mangila.ai2;

import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
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
        return jsonMapper.convertValue(shellSessionState.options(), JsonNode.class)
                .toPrettyString();
    }

    @Command(
            name = "set",
            description = "Update active model or temperature"
    )
    public String set(
            @Option(shortName = 'm', longName = "model", description = "Active model name") String model,
            @Option(shortName = 't', longName = "temp", description = "Sampling temperature") Double temp
    ) {
        shellSessionState.updateOptions(builder -> {
            if (model != null && !model.isBlank()) {
                return builder.model(model);
            }
            if (temp != null) {
                return builder.temperature(temp);
            }
            return builder;
        });
        return print();
    }

    @Command(
            name = "reset",
            description = "Reset session configuration to defaults"
    )
    public String reset() {
        shellSessionState.resetOptions();
        return "Configuration reset to defaults.";
    }
}
