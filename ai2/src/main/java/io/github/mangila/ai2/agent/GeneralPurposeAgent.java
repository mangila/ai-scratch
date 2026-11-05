package io.github.mangila.ai2.agent;

import com.samskivert.mustache.Template;
import io.github.mangila.ai2.config.ShellSessionState;
import io.github.mangila.ai2.provider.TemplateProvider;
import io.github.mangila.ai2.tool.DateTimeTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GeneralPurposeAgent implements Agent {

    private final ChatClient chatClient;

    public GeneralPurposeAgent(
            TemplateProvider templateProvider,
            ShellSessionState shellSessionState,
            ChatMemory shellChatMemory,
            SyncMcpToolCallbackProvider mcpToolCallbackProvider,
            DateTimeTool dateTimeTool,
            ChatClient.Builder builder
    ) {
        final Template systemPrompt = templateProvider.get(TemplateProvider.TemplateType.SYSTEM_PROMPT);
        shellChatMemory.add(shellSessionState.getConversationId(), new ArrayList<>());
        final MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(shellChatMemory)
                .order(0)
                .build();
        final ToolCallingAdvisor toolAdvisor = ToolCallingAdvisor.builder()
                .advisorOrder(1)
                .build();
        final SimpleLoggerAdvisor loggerAdvisor = SimpleLoggerAdvisor.builder()
                .order(2)
                .build();
        this.chatClient = builder
                .defaultSystem(systemPrompt.execute(null))
                .defaultAdvisors(toolAdvisor, loggerAdvisor)
                .defaultOptions(shellSessionState.getOllamaChatOptions().mutate())
                .defaultTools(dateTimeTool, mcpToolCallbackProvider.getToolCallbacks())
                .build();
    }

    public String chat(String text) {
        var s = chatClient.prompt()
                .user(text)
                .call()
                .chatResponse();
        if (s.hasToolCalls()) {
            IO.println("asdf");
        }
        return s.getResult().getOutput().getText();
    }

    @Override
    public String answer(String message) {
        return "";
    }

    @Override
    public Type getType() {
        return Type.GENERAL_PURPOSE;
    }
}
