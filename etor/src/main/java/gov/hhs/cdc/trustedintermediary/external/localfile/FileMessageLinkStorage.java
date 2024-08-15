package gov.hhs.cdc.trustedintermediary.external.localfile;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLink;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkException;
import gov.hhs.cdc.trustedintermediary.etor.messagelink.MessageLinkStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

/** Implements the {@link MessageLinkStorage} using local files. */
public class FileMessageLinkStorage implements MessageLinkStorage {

    private static final FileMessageLinkStorage INSTANCE = new FileMessageLinkStorage();

    @Inject Formatter formatter;
    @Inject Logger logger;

    static final String MESSAGE_LINK_FILE_NAME = "cdctimetadata.json";
    static final Path MESSAGE_LINK_FILE_PATH;

    static {
        try {
            MESSAGE_LINK_FILE_PATH = ApplicationContext.createTempFile(MESSAGE_LINK_FILE_NAME);
            Files.writeString(
                    MESSAGE_LINK_FILE_PATH,
                    "[]",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FileMessageLinkStorage() {}

    public static FileMessageLinkStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized Optional<MessageLink> getMessageLink(String messageId)
            throws MessageLinkException {
        try {
            Set<MessageLink> messageLinks = readMessageLinks();
            List<MessageLink> foundMessageLinks =
                    messageLinks.stream()
                            .filter(link -> link.getMessageIds().contains(messageId))
                            .toList();

            if (foundMessageLinks.size() > 1) {
                logger.logWarning("More than one message link found for messageId: {}", messageId);
            }

            return foundMessageLinks.stream().findFirst();
        } catch (IOException | FormatterProcessingException e) {
            throw new MessageLinkException("Error retrieving message links", e);
        }
    }

    @Override
    public synchronized void saveMessageLink(MessageLink messageLink) throws MessageLinkException {
        try {
            Set<MessageLink> messageLinks = readMessageLinks();
            Optional<MessageLink> existingLink =
                    messageLinks.stream()
                            .filter(
                                    link ->
                                            Objects.equals(
                                                    link.getLinkId(), messageLink.getLinkId()))
                            .findFirst();
            if (existingLink.isPresent()) {
                MessageLink existing = existingLink.get();
                existing.addMessageIds(messageLink.getMessageIds());
            } else {
                messageLinks.add(messageLink);
            }
            writeMessageLinks(messageLinks);
        } catch (IOException | FormatterProcessingException e) {
            throw new MessageLinkException("Error saving message links", e);
        }
    }

    private Set<MessageLink> readMessageLinks() throws IOException, FormatterProcessingException {
        String messageLinkContent = Files.readString(MESSAGE_LINK_FILE_PATH);
        Set<MessageLink> messageLinks =
                formatter.convertJsonToObject(messageLinkContent, new TypeReference<>() {});
        return messageLinks != null ? messageLinks : new HashSet<>();
    }

    private void writeMessageLinks(Set<MessageLink> messageLinks)
            throws IOException, FormatterProcessingException {
        String json = formatter.convertToJsonString(messageLinks);
        Files.writeString(MESSAGE_LINK_FILE_PATH, json, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
