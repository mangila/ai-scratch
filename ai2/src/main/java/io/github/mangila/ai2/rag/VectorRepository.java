package io.github.mangila.ai2.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VectorRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(VectorRepository.class);

    private final VectorStore vectorStore;
    private final TokenCountEstimator estimator;
    private final MarkdownDocumentReaderConfig.Builder markdownConfig;

    public VectorRepository(VectorStore vectorStore, TokenCountEstimator estimator) {
        this.vectorStore = vectorStore;
        this.estimator = estimator;
        this.markdownConfig = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeBlockquote(true)
                .withIncludeCodeBlock(true);
    }

    public void insertMarkdown(Resource markdown, Map<String, Object> metadata) {
        final MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(
                markdown,
                markdownConfig.withAdditionalMetadata(metadata).build()
        );
        final List<Document> documents = markdownDocumentReader.get();
        for (Document document : documents) {
            final int tokenCount = estimator.estimate(document.getText());
            document.getMetadata()
                    .put("token_count", tokenCount);
            LOGGER.info("Inserted document with token count: {}", tokenCount);
        }
        vectorStore.add(documents);
    }
}
