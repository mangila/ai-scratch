package io.github.mangila.ai2.provider;

import io.github.mangila.ai2.agent.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class AgentProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentProvider.class);

    private final Map<Agent.Type, Agent> agentRegistry;

    public AgentProvider(Map<String, Agent> agentMap) {
        this.agentRegistry = new EnumMap<>(Agent.Type.class);
        for (Map.Entry<String, Agent> entry : agentMap.entrySet()) {
            Agent agent = entry.getValue();
            agentRegistry.put(agent.getType(), agent);
            LOGGER.info("Registered agent: {}", agent.getType());
        }
    }

    public Agent get(Agent.Type type) {
        final Agent agent = agentRegistry.get(type);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found for type: %s".formatted(type));
        }
        return agent;
    }

}
