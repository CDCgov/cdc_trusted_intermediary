package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using local files. */
public class FilePartnerMetadataStorage implements PartnerMetadataStorage {

    private static final FilePartnerMetadataStorage INSTANCE = new FilePartnerMetadataStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    private static final Path metadataTempDirectory;

    static {
        try {
            metadataTempDirectory = Files.createTempDirectory("metadata");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FilePartnerMetadataStorage() {}

    public static FilePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public PartnerMetadata readMetadata(final String uniqueId) {
        return null;
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        Path metadataFilePath = getFilePath(metadata.uniqueId());
        try {
            Files.writeString(metadataFilePath, formatter.convertToJsonString(metadata));
            logger.logInfo("Saved metadata for " + metadata.uniqueId() + " to " + metadataFilePath);
        } catch (IOException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Error saving metadata for " + metadata.uniqueId(), e);
        }
    }

    private Path getFilePath(String uniqueId) {
        return metadataTempDirectory.resolve(uniqueId + ".json");
    }
}
