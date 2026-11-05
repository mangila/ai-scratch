package io.github.mangila.ai2.config;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties("shell")
@Validated
public class ShellConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellConfig.class);

    @URL(message = "Pokeapi URL must be a valid URL")
    private String pokeapiUrl;

    @NotNull(message = "Storage directory cannot be null")
    private Path storageDir;

    private boolean debug = false;

    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedString(
                "ai:> ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        );
    }

    public String getPokeapiUrl() {
        return pokeapiUrl;
    }

    public void setPokeapiUrl(String pokeapiUrl) {
        this.pokeapiUrl = pokeapiUrl;
    }

    public Path getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(Path storageDir) {
        this.storageDir = storageDir;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
