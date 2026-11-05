package io.github.mangila.ai2.agent;

import com.samskivert.mustache.Template;
import io.github.mangila.ai2.config.ShellSessionState;
import io.github.mangila.ai2.model.Output;
import io.github.mangila.ai2.provider.TemplateProvider;
import io.github.mangila.ai2.provider.TemplateProvider.TemplateType;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PokemonAgent implements Agent {

    private final ShellSessionState shellSessionState;
    private final TemplateProvider templateProvider;
    private final FilterExpressionBuilder filterExpressionBuilder;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public PokemonAgent(ShellSessionState shellSessionState,
                        TemplateProvider templateProvider,
                        VectorStore vectorStore,
                        ChatClient.Builder chatClientBuilder) {
        this.shellSessionState = shellSessionState;
        this.templateProvider = templateProvider;
        this.vectorStore = vectorStore;
        final SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder()
                .order(0)
                .build();
        final Template pokemonSystemPrompt = templateProvider.get(TemplateType.POKEMON_SYSTEM_PROMPT);
        this.chatClient = chatClientBuilder
                .defaultSystem(pokemonSystemPrompt.execute(null))
                .defaultOptions(shellSessionState.getOllamaChatOptions().mutate()
                        .format("json")
                )
                .defaultAdvisors(loggerAdvisor)
                .build();
        this.filterExpressionBuilder = new FilterExpressionBuilder();
    }

    @Override
    public String answer(String message) {
        final List<Document> documents = doSearch(message);
        if (documents.isEmpty()) {
            return "I don't know the answer to that question.";
        }
        final Map<String, Object> context = Map.of(
                "rag", documents,
                "query", message
        );
        final Template template = templateProvider.get(TemplateType.POKEMON_CALL_PROMPT);
        final String templateString = template.execute(context);
        Output out = chatClient.prompt()
                .user(templateString)
                .call()
                .entity(Output.class, spec -> spec.useProviderStructuredOutput()
                        .validateSchema());
        return out.content();
    }

    @Override
    public Type getType() {
        return Type.POKEMON;
    }

    private Filter.Expression getFilterExpression() {
        return filterExpressionBuilder.and(
                filterExpressionBuilder.eq("domain", "pokemon"),
                filterExpressionBuilder.eq("source", "pokeapi")
        ).build();
    }

    private List<Document> doSearch(String query) {
        final var expression = getFilterExpression();
        final SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(6)
                .filterExpression(expression)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}