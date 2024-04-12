package gov.hhs.cdc.trustedintermediary.etor.messagelink;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class represents a link between messages. Each link has a unique ID and is associated with a
 * set of message IDs to link.
 */
public final class MessageLink {
    private UUID linkId;
    private Set<String> messageIds;

    public MessageLink() {
        this.messageIds = new HashSet<>();
    }

    public MessageLink(UUID linkId, String messageId) {
        this.linkId = linkId;
        this.messageIds = new HashSet<>(Collections.singleton(messageId));
    }

    public MessageLink(UUID linkId, Set<String> messageIds) {
        this.linkId = linkId;
        this.messageIds = new HashSet<>(messageIds);
    }

    public void setLinkId(UUID linkId) {
        this.linkId = linkId;
    }

    public UUID getLinkId() {
        return linkId;
    }

    public void setMessageIds(Set<String> messageIds) {
        this.messageIds = new HashSet<>(messageIds);
    }

    public Set<String> getMessageIds() {
        return Collections.unmodifiableSet(messageIds);
    }

    public void addMessageId(String messageId) {
        this.messageIds.add(messageId);
    }

    public void addMessageIds(Set<String> messageIds) {
        this.messageIds.addAll(messageIds);
    }
}
