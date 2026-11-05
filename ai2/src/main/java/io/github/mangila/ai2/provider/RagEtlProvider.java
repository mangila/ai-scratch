package io.github.mangila.ai2.provider;

import io.github.mangila.ai2.rag.RagEtl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class RagEtlProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagEtlProvider.class);

    private final Map<RagEtl.Type, RagEtl> ragEtlRegistry;

    public RagEtlProvider(Map<String, RagEtl> ragEtlMap) {
        this.ragEtlRegistry = new EnumMap<>(RagEtl.Type.class);
        for (Map.Entry<String, RagEtl> entry : ragEtlMap.entrySet()) {
            RagEtl ragEtl = entry.getValue();
            ragEtlRegistry.put(ragEtl.getType(), ragEtl);
            LOGGER.info("Registered Rag ETL pipeline: {}", ragEtl.getType());
        }
    }

    public RagEtl get(RagEtl.Type type) {
        final RagEtl ragEtl = ragEtlRegistry.get(type);
        if (ragEtl == null) {
            throw new IllegalArgumentException("Rag ETL pipeline not found for type: %s".formatted(type));
        }
        return ragEtl;
    }

}
