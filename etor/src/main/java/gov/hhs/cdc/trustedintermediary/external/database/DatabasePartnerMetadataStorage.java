package gov.hhs.cdc.trustedintermediary.external.database;

import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadata;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataException;
import gov.hhs.cdc.trustedintermediary.etor.metadata.partner.PartnerMetadataStorage;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/** Implements the {@link PartnerMetadataStorage} using a database. */
public class DatabasePartnerMetadataStorage implements PartnerMetadataStorage {

    @Inject DbDao dao;

    @Inject Logger logger;
    private static final DatabasePartnerMetadataStorage INSTANCE =
            new DatabasePartnerMetadataStorage();

    private DatabasePartnerMetadataStorage() {}

    public static DatabasePartnerMetadataStorage getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<PartnerMetadata> readMetadata(final String uniqueId)
            throws PartnerMetadataException {
        try {
            PartnerMetadata data = (PartnerMetadata) dao.fetchMetadata(uniqueId);
            return Optional.ofNullable(data);
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving metadata", e);
        }
    }

    @Override
    public void saveMetadata(final PartnerMetadata metadata) throws PartnerMetadataException {
        logger.logInfo("saving the metadata");
        try {
            dao.upsertMetadata(
                    metadata.receivedSubmissionId(),
                    metadata.sentSubmissionId(),
                    metadata.sender(),
                    metadata.receiver(),
                    metadata.hash(),
                    metadata.timeReceived(),
                    metadata.deliveryStatus(),
                    metadata.failureReason());
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error saving metadata", e);
        }
    }

    @Override
    public Map<String, String> readConsolidatedMetadata(String sender)
            throws PartnerMetadataException {
        Map<String, String> consolidatedMetadata;
        try {
            consolidatedMetadata = dao.fetchConsolidatedMetadata(sender);
        } catch (SQLException e) {
            throw new PartnerMetadataException("Error retrieving consolidated metadata", e);
        }
        return consolidatedMetadata;
    }
}
