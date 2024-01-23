package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStatus;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/** Interface for accessing the database for metadata */
public interface DbDao {
    void upsertMetadata(
            String receivedId,
            String sentId,
            String sender,
            String receiver,
            String hash,
            Instant timeReceived,
            PartnerMetadataStatus deliveryStatus,
            String failureReason)
            throws SQLException;

    Map<String, String> fetchConsolidatedMetadata(String sender) throws SQLException;

    Object fetchMetadata(String uniqueId) throws SQLException;
}
