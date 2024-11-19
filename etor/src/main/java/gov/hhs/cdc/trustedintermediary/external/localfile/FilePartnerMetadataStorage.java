package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using local files. */
public class FilePartnerMetadataStorage implements PartnerMetadataStorage {

    private static final FilePartnerMetadataStorage INSTANCE = new FilePartnerMetadataStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    static final String SUBDIRECTORY_NAME = "cdctimetadata";
    static final Path METADATA_DIRECTORY;

    static {
        try {
            METADATA_DIRECTORY = ApplicationContext.createTempDirectory(SUBDIRECTORY_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FilePartnerMetadataStorage() {}

    public static FilePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<PartnerMetadata> readMetadata(final String submissionId)
            throws PartnerMetadataException {
        try {
            Path filePath = searchFilePath(submissionId);
            if (filePath == null || !Files.exists(filePath)) {
                logger.logWarning("Metadata file not found: {}", filePath);
                return Optional.empty();
            }
            String content = Files.readString(filePath);
            PartnerMetadata metadata =
                    formatter.convertJsonToObject(content, new TypeReference<>() {});
            return Optional.ofNullable(metadata);
        } catch (IOException | FormatterProcessingException e) {
            throw new PartnerMetadataException("Unable to read the metadata file", e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        try {
            Path previousMetadataFilePath = searchFilePath(metadata.outboundMessageId());
            if (previousMetadataFilePath != null) {
                // delete the pre-existing metadata file so that we don't find the old file when we
                // search for a given metadata ID
                Files.delete(previousMetadataFilePath);
            }
        } catch (IOException e) {
            throw new PartnerMetadataException(
                    "Error deleting previous metadata file for " + metadata.outboundMessageId(), e);
        }

        Path metadataFilePath =
                getFilePath(metadata.outboundMessageId() + "-" + metadata.inboundMessageId());
        try {
            String content = formatter.convertToJsonString(metadata);
            Files.writeString(metadataFilePath, content);
            logger.logInfo(
                    "Saved metadata for {} to {}", metadata.outboundMessageId(), metadataFilePath);
        } catch (IOException | FormatterProcessingException e) {
            throw new PartnerMetadataException(
                    "Error saving metadata for " + metadata.outboundMessageId(), e);
        }
    }

    @Override
    public Set<PartnerMetadata> readMetadataForSender(String senderUniversalId)
            throws PartnerMetadataException {
        try {
            return getPartnerMetadata().stream()
                    .filter(
                            metadata ->
                                    metadata.sendingFacilityDetails()
                                            .universalId()
                                            .equals(senderUniversalId))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new PartnerMetadataException(
                    "Failed reading metadata for sender: " + senderUniversalId, e);
        }
    }

    @Override
    public Set<String> readMetadataForMessageLinking(String outboundMessageId)
            throws PartnerMetadataException {
        try {
            Set<PartnerMetadata> existingMetadata = getPartnerMetadata();
            PartnerMetadata match =
                    existingMetadata.stream()
                            .filter(
                                    metadata ->
                                            metadata.outboundMessageId().equals(outboundMessageId))
                            .findFirst()
                            .orElse(null);

            if (match == null) {
                logger.logWarning(
                        "Matching metadata not found for outboundMessageId: {}", outboundMessageId);
                return Set.of();
            }

            return existingMetadata.stream()
                    .filter(
                            metadata ->
                                    metadata.placerOrderNumber().equals(match.placerOrderNumber())
                                            && (metadata.sendingFacilityDetails()
                                                            .equals(match.sendingFacilityDetails())
                                                    || metadata.sendingFacilityDetails()
                                                            .equals(
                                                                    match
                                                                            .receivingFacilityDetails()))
                                            && !metadata.outboundMessageId()
                                                    .equals(outboundMessageId))
                    .map(PartnerMetadata::outboundMessageId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new PartnerMetadataException(
                    "Failed reading metadata for messageId: " + outboundMessageId, e);
        }
    }

    private Set<PartnerMetadata> getPartnerMetadata() throws IOException {
        try (Stream<Path> fileList = Files.list(METADATA_DIRECTORY)) {
            return fileList.map(
                            fileName -> {
                                try {
                                    return Files.readString(fileName);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    .map(
                            metadataContent -> {
                                try {
                                    return formatter.convertJsonToObject(
                                            metadataContent,
                                            new TypeReference<PartnerMetadata>() {});
                                } catch (FormatterProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    .collect(Collectors.toSet());
        }
    }

    private Path getFilePath(String metadataId) {
        return METADATA_DIRECTORY.resolve(metadataId + ".json");
    }

    private Path searchFilePath(String metadataId) throws IOException {

        Path path = null;

        try (Stream<Path> fileList = Files.list(METADATA_DIRECTORY)) {
            path =
                    fileList.filter(
                                    metadataPath -> {
                                        String fileName = metadataPath.getFileName().toString();
                                        return fileName.startsWith(metadataId)
                                                || fileName.endsWith(metadataId + ".json");
                                    })
                            .findFirst()
                            .orElse(null);
        }

        return path;
    }
}
