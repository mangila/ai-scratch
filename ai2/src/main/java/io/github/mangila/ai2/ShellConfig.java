package io.github.mangila.ai2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("shell")
@Validated
public class ShellConfig {

    @NotBlank(message = "System context cannot be null")
    private String systemContext;

    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedString(
                "ai:> ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)
        );
    }

    public String getSystemContext() {
        return systemContext;
    }

    public void setSystemContext(String systemContext) {
        this.systemContext = systemContext;
    }
}
