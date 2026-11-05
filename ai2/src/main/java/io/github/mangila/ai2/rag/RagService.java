package io.github.mangila.ai2.rag;

import com.samskivert.mustache.Template;
import io.github.mangila.ai2.config.ShellSessionState;
import io.github.mangila.ai2.provider.TemplateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class RagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RagService.class);

    private final ShellSessionState shellSessionState;
    private final TemplateProvider templateProvider;
    private final VectorRepository vectorRepository;

    public RagService(ShellSessionState shellSessionState,
                      TemplateProvider templateProvider,
                      VectorRepository vectorRepository) {
        this.shellSessionState = shellSessionState;
        this.templateProvider = templateProvider;
        this.vectorRepository = vectorRepository;
    }

    public void insertMarkdown(TemplateProvider.TemplateType templateType,
                               Map<String, Object> templateContext,
                               Map<String, Object> ragMetadata) {
        final Template template = templateProvider.get(templateType);
        final String templateString = template.execute(templateContext);
        final byte[] templateBytes = templateString.getBytes(StandardCharsets.UTF_8);
        final ByteArrayResource byteArrayResource = new ByteArrayResource(templateBytes);
        vectorRepository.insertMarkdown(byteArrayResource, ragMetadata);
        final boolean debug = shellSessionState.getShellConfig().isDebug();
        if (debug) {
            final Path markdownDocumentPath = shellSessionState.getShellConfig().getStorageDir();
            final String entity = ragMetadata.get("entity").toString();
            final Path out = markdownDocumentPath.resolve("entity-%s.md".formatted(entity));
            LOGGER.info("Writing markdown document to {}", out);
            try {
                Files.writeString(out, templateString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
