package io.github.mangila.ai2.agent;

import com.samskivert.mustache.Template;
import io.github.mangila.ai2.config.ShellSessionState;
import io.github.mangila.ai2.service.TemplateProvider;
import io.github.mangila.ai2.service.TemplateProvider.TemplateType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PokemonAgent {

    private final ShellSessionState shellSessionState;
    private final Template callTemplate;
    private final ChatClient chatClient;

    public PokemonAgent(ShellSessionState shellSessionState,
                        TemplateProvider templateProvider,
                        @Qualifier("pokemonVectorStore") VectorStore vectorStore,
                        ChatClient.Builder chatClientBuilder) {
        this.shellSessionState = shellSessionState;
        this.callTemplate = templateProvider.get(TemplateType.POKEMON_CALL_PROMPT);
        final QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .order(0)
                .build();
        final ToolCallingAdvisor toolAdvisor = ToolCallingAdvisor.builder()
                .advisorOrder(1)
                .build();
        final SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder()
                .order(2)
                .build();
        final Template pokemonSystemPrompt = templateProvider.get(TemplateType.POKEMON_SYSTEM_PROMPT);
        this.chatClient = chatClientBuilder
                .defaultSystem(pokemonSystemPrompt.execute(null))
                .defaultOptions(shellSessionState.getOllamaChatOptions().mutate())
                .defaultAdvisors(ragAdvisor, toolAdvisor, loggerAdvisor)
                .defaultToolContext(Map.of("debug", shellSessionState.getShellConfig().isDebug()))
                .build();
    }

    @Tool(name = "ask_pokemon_agent", description = """
            Ask the pokemon agent about a pokemon
            """)
    public String ask(
            @ToolParam(description = "The user query contains a pokemon name", required = true) String pokemonName) {
        final String template = callTemplate.execute(Map.of("name", pokemonName));
        return chatClient.prompt()
                .user(template)
                .call()
                .content();
    }

}