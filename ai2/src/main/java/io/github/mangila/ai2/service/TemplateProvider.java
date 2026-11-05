package io.github.mangila.ai2.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.mustache.autoconfigure.MustacheProperties;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class TemplateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProvider.class);

    private final Map<TemplateType, Template> templateRegistry;

    public TemplateProvider(Mustache.Compiler mustacheCompiler, MustacheProperties mustacheProperties) {
        this.templateRegistry = new EnumMap<>(TemplateType.class);
        for (TemplateType templateType : TemplateType.values()) {
            final Template template = loadTemplate(templateType, mustacheCompiler, mustacheProperties);
            templateRegistry.put(templateType, template);
        }
    }

    public Template get(TemplateType templateType) {
        final Template template = templateRegistry.get(templateType);
        if (template == null) {
            throw new IllegalArgumentException("Template not found for type: %s".formatted(templateType));
        }
        return template;
    }

    private Template loadTemplate(
            TemplateType templateType,
            Mustache.Compiler mustacheCompiler,
            MustacheProperties mustacheProperties) {
        final String templateName = templateType.templateName;
        LOGGER.info("Loading template: '{}{}'", templateName, mustacheProperties.getSuffix());
        return mustacheCompiler.loadTemplate(templateName);
    }

    public enum TemplateType {
        SYSTEM_PROMPT("system-prompt"),
        POKEMON_CALL_PROMPT("pokemon/call-prompt"),
        POKEMON_SYSTEM_PROMPT("pokemon/system-prompt"),
        POKEMON_RAG("pokemon/rag"),
        RICK_AND_MORTY_CALL_PROMPT("rickandmorty/call-prompt"),
        RICK_AND_MORTY_SYSTEM_PROMPT("rickandmorty/system-prompt"),
        RICK_AND_MORTY_RAG("rickandmorty/rag"),
        STAR_WARS_CALL_PROMPT("starwars/call-prompt"),
        STAR_WARS_SYSTEM_PROMPT("starwars/system-prompt"),
        STAR_WARS_RAG("starwars/rag");

        private final String templateName;

        TemplateType(String templateName) {
            this.templateName = templateName;
        }
    }
}
