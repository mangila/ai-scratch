package io.github.mangila.ai2;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

@Component
public class ShellSessionState {

    private final OllamaChatOptions initialDefaults;
    private final AtomicReference<OllamaChatOptions> activeOptions;
    private final ShellConfig shellConfig;

    public ShellSessionState(OllamaChatModel chatModel, ShellConfig shellConfig) {
        this.initialDefaults = chatModel.getOptions();
        this.activeOptions = new AtomicReference<>(chatModel.getOptions().mutate().build());
        this.shellConfig = shellConfig;
    }

    public String systemContex() {
        return shellConfig.getSystemContext();
    }

    public OllamaChatOptions options() {
        return activeOptions.get();
    }

    public void updateOptions(UnaryOperator<OllamaChatOptions.Builder> customizer) {
        activeOptions.updateAndGet(options -> customizer.apply(options.mutate()).build());
    }

    public void resetOptions() {
        activeOptions.set(initialDefaults);
    }
}
